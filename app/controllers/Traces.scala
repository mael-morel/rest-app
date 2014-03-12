package controllers

import org.joda.time.DateTime
import scala.concurrent.Future

import play.api.Logger
import play.api.Play.current
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{ MongoController, ReactiveMongoPlugin }

import reactivemongo.api.gridfs.GridFS
import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._

import models.Trace
import models.Trace._
import play.api.libs.json.{JsError, Json}
import java.io.PrintStream
import reactivemongo.bson.BSONString
import scala.Some
import reactivemongo.bson.BSONInteger
import reactivemongo.api.collections.default.BSONCollection
import play.api.libs.json.Json

object Traces extends Controller with MongoController {

  // get the collection 'traces'
  val collection = db[BSONCollection]("traces")
  // a GridFS store named 'attachments'
  //val gridFS = new GridFS(db, "attachments")
  val gridFS = new GridFS(db)

  // let's build an index on our gridfs chunks collection if none
  gridFS.ensureIndex().onComplete {
    case index =>
      Logger.info(s"Checked index, result is $index")
  }

  def index = Action { implicit request =>
    Ok(views.html.angularView())
  }

  // list all traces and sort them
  def list = Action.async { implicit request =>
  // get a sort document (see getSort method for more information)
    val sort = getSort(request)
    // build a selection document with an empty query and a sort subdocument ('$orderby')
    val query = BSONDocument(
      "$orderby" -> sort,
      "$query" -> BSONDocument())
    val activeSort = request.queryString.get("sort").flatMap(_.headOption).getOrElse("none")
    // the cursor of documents
    val found = collection.find(query).cursor[Trace]
    // build (asynchronously) a list containing all the traces
    found.collect[List]().map { traces =>
      //print the list in Json format
      Ok(Json.toJson(traces))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage())
    }
  }

  def showCreationForm = Action {
    Ok(views.html.editTrace(None, Trace.form, None))
  }

  def showEditForm(id: String) = Action.async {
    val objectId = new BSONObjectID(id)
    // get the documents having this id (there will be 0 or 1 result)
    val futureTrace = collection.find(BSONDocument("_id" -> objectId)).one[Trace]
    // ... so we get optionally the matching trace,  if any
    // let's use for-comprehensions to compose futures (see http://doc.akka.io/docs/akka/2.0.3/scala/futures.html#For_Comprehensions for more information)
    for {
    // get a future option of trace
      maybeTrace <- futureTrace
      // if there is some traces, return a future of result with the trace and its attachments
      result <- maybeTrace.map { trace =>
        import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader
        // search for the matching attachments
        // find(...).toList returns a future list of documents (here, a future list of ReadFileEntry)
        gridFS.find(BSONDocument("trace" -> trace.id.get)).collect[List]().map { files =>
          val filesWithId = files.map { file =>
            file.id.asInstanceOf[BSONObjectID].stringify -> file
          }
            Ok(Json.toJson(trace))
          //Ok(views.html.editTrace(Some(id), Trace.form.fill(trace), Some(filesWithId)))
        }
      }.getOrElse(Future(NotFound))
    } yield result
  }

  def create = Action(parse.json) { implicit request =>
    val traceJson = request.body
    val trace = traceJson.as[Trace]

    try{
      collection.insert(trace)
      Ok(Json.toJson("ok"))
    }catch {
      case e : IllegalArgumentException =>
        Ok(Json.toJson("ko"))
    }
  }

  def update(id: String) = Action.async(parse.json) {
    request =>
      val objectID = new BSONObjectID(id)
      val name = request.body.\("name").toString()
      val desc = request.body.\("desc").toString()
      val modifier = BSONDocument(
        "$set" -> BSONDocument(
          "name" -> name,
          "desc" -> desc))
      collection.update(BSONDocument("_id" -> objectID), modifier).map(
        _ => Ok(Json.toJson(Trace(Option(objectID), name, desc))))
  }

  def edit(id: String) = Action.async { implicit request =>
    Trace.form.bindFromRequest.fold(
      errors => Future.successful(Ok(views.html.editTrace(Some(id), errors, None))),
      trace => {
        val objectId = new BSONObjectID(id)
        // create a modifier document, ie a document that contains the update operations to run onto the documents matching the query
        val modifier = BSONDocument(
          // this modifier will set the fields 'name' and 'desc'
          "$set" -> BSONDocument(
            "name" -> BSONString(trace.name),
            "desc" -> BSONString(trace.desc)))
        // ok, let's do the update
        collection.update(BSONDocument("_id" -> objectId), modifier).map { _ =>
          Redirect(routes.Traces.index)
        }
      })
  }

  def delete(id: String) = Action.async {
    // let's collect all the attachments matching that match the trace to delete
    gridFS.find(BSONDocument("trace" -> new BSONObjectID(id))).collect[List]().flatMap { files =>
    // for each attachment, delete their chunks and then their file entry
      val deletions = files.map { file =>
        gridFS.remove(file)
      }
      Future.sequence(deletions)
    }.flatMap { _ =>
    // now, the last operation: remove the trace
      collection.remove(BSONDocument("_id" -> new BSONObjectID(id)))
    }.map(_ => Ok).recover { case _ => InternalServerError }
  }

  // save the uploaded file as an attachment of the trace with the given id
  def saveAttachment(id: String) = Action.async(gridFSBodyParser(gridFS)) { request =>
  // here is the future file!
    val futureFile = request.body.files.head.ref
    // when the upload is complete, we add the trace id to the file entry (in order to find the attachments of the trace)
    val futureUpdate = for {
      file <- futureFile
      // here, the file is completely uploaded, so it is time to update the trace
      updateResult <- {
        gridFS.files.update(
          BSONDocument("_id" -> file.id),
          BSONDocument("$set" -> BSONDocument("trace" -> BSONObjectID(id))))
      }
    } yield updateResult

    futureUpdate.map {
      case _ => Redirect(routes.Traces.showEditForm(id))
    }.recover {
      case e => InternalServerError(e.getMessage())
    }
  }

  def getAttachment(id: String) = Action.async { request =>
  // find the matching attachment, if any, and streams it to the client
    val file = gridFS.find(BSONDocument("_id" -> new BSONObjectID(id)))
    request.getQueryString("inline") match {
      case Some("true") => serve(gridFS, file, CONTENT_DISPOSITION_INLINE)
      case _            => serve(gridFS, file)
    }
  }

  def removeAttachment(id: String) = Action.async {
    gridFS.remove(new BSONObjectID(id)).map(_ => Ok).recover { case _ => InternalServerError }
  }

  private def getSort(request: Request[_]) = {
    request.queryString.get("sort").map { fields =>
      val sortBy = for {
        order <- fields.map { field =>
          if (field.startsWith("-"))
            field.drop(1) -> -1
          else field -> 1
        }
        if order._1 == "name" || order._1 == "desc"
      } yield order._1 -> BSONInteger(order._2)
      BSONDocument(sortBy)
    }
  }
}
