package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.data.format.Formats._

import play.api.libs.json.{JsValue, Json}
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONObjectIDIdentity
import reactivemongo.bson.BSONStringHandler
import reactivemongo.bson.Producer.nameValue2Producer
import play.modules.reactivemongo.json.BSONFormats._
import play.api.libs.json

/**
 * Created by mael on 3/5/14.
 */
case class Trace (

  id: Option[BSONObjectID],
  name: String,
  desc: String
)
  object Trace{

    implicit val traceFormat = Json.format[Trace]
    implicit val traceReads = Json.reads[Trace]

    implicit object TraceBSONWriter extends BSONDocumentWriter[Trace] {
      def write(trace: Trace): BSONDocument =
        BSONDocument(
          "_id" -> trace.id.getOrElse(BSONObjectID.generate),
          "name" -> trace.name,
          "desc" -> trace.desc)
    }

    implicit object TraceBSONReader extends BSONDocumentReader[Trace] {
      def read(doc: BSONDocument): Trace =
        Trace(
          doc.getAs[BSONObjectID]("_id"),
          doc.getAsTry[String]("name").get,
          doc.getAsTry[String]("desc").get)
    }



    val form = Form(
      mapping(
        "id" -> optional(of[String] verifying pattern(
          """[a-fA-F0-9]{24}""".r,
          "constraint.objectId",
          "error.objectId")),
        "name" -> nonEmptyText,
        "desc" -> text) { (id, name, desc) =>
        Trace(
          id.map(new BSONObjectID(_)),
          name,
          desc)
      } { trace =>
        Some(
          (trace.id.map(_.stringify),
            trace.name,
            trace.desc))
      })
}
