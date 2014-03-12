import org.scalamock._
import org.scalatest.mock
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import org.scalatest.FunSuite
import org.scalamock.scalatest.MockFactory

import play.api.test._
import play.api.test.FakeApplication
import play.api.test.Helpers._

import reactivemongo.api.collections.default.BSONCollection
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification with Mock{

  "Traces" should {

    "display the create form" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
//
        //val col = mock[BSONCollection]
        val result = controllers.Traces.create(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain("<label for=\"name\">name</label>")

      }
    }

    "add trace" in {
      running(FakeApplication()) {

        //val col = mock[BSONCollection]

        val traceJsonExample = "{\"id\":{\"$oid\":\"531a07ef990000990078f1b9\"},\"name\":\"1212\",\"desc\":\"1212\"}"
        val result = controllers.Traces.create(FakeRequest().withHeaders("Content-Type", "application/json").withJsonBody(traceJsonExample))

        status(result) must equalTo(OK)
        contentAsString(result) must contain("<label for=\"name\">name</label>")

      }
    }

//    "be retrieved by id" in {
//      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
//
////         val tracesMock = mock[controllers.Traces]
////         tracesMock.delete();
//
////        val Some(macintosh) = Computer.findById(21)
////
////        macintosh.name must equalTo("Macintosh")
////        macintosh.introduced must beSome.which(dateIs(_, "1984-01-24"))
//
//      }
//    }
  }


   // @RunWith(classOf[JUnitRunner])
//    class ModelSpec extends Specification {
//
//      import models._
//
//      // -- Date helpers
//
//      def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str
//
//      // --
//
//      "Computer model" should {
//
//        "be retrieved by id" in {
//          running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
//
//            val Some(macintosh) = Computer.findById(21)
//
//            macintosh.name must equalTo("Macintosh")
//            macintosh.introduced must beSome.which(dateIs(_, "1984-01-24"))
//
//          }
//        }
//
//        "be listed along its companies" in {
//          running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
//
//            val computers = Computer.list()
//
//            computers.total must equalTo(574)
//            computers.items must have length(10)
//
//          }
//        }
//
//        "be updated if needed" in {
//          running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
//
//            Computer.update(21, Computer(name="The Macintosh", introduced=None, discontinued=None, companyId=Some(1)))
//
//            val Some(macintosh) = Computer.findById(21)
//
//            macintosh.name must equalTo("The Macintosh")
//            macintosh.introduced must beNone
//
//          }
//        }
//
//      }
//
//    }
//  }
}