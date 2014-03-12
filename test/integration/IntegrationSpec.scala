package integration

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class IntegrationSpec extends Specification {

  "Application" should {

    "work from within a browser" in {
      running(TestServer(9000), HTMLUNIT) {
        browser =>
          browser.goTo("http://localhost:9000/")
          browser.$("title").getText must equalTo("Traces")
      }
    }

    "display 'Add a trace'" in {
      running(TestServer(9000), HTMLUNIT) {
        browser =>
          browser.goTo("http://localhost:9000/traces/new")
          browser.$("body").getText must contain("Add a trace")
      }
    }

  }
}