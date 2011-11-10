package uk.gov.gds.router.repository.application

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.router.util.Logging
import uk.gov.gds.router.model.{Conflict, NewlyCreated, Application}
import uk.gov.gds.router.repository.application.Applications._
import uk.gov.gds.router.mongodb.MongoDatabase.onSameDatabaseServer

@RunWith(classOf[JUnitRunner])
class ApplicationsTest extends FunSuite with ShouldMatchers with Logging {

  test("canCreateApplication") {
    val applicationName = uniqueApplicationName
    val application = Application(applicationName, "publisher.dev.gov.uk")

    store(application) should be(NewlyCreated)
    load(applicationName) should be(Some(application))
    store(application) should be(Conflict)
  }

  test("Can delete application") {
    onSameDatabaseServer {
      val applicationName = uniqueApplicationName
      val application = Application(applicationName, "publisher.dev.gov.uk")

      store(application)
      load(applicationName) should be(Some(application))

      delete(application.application_id)
      load(applicationName) should be(None)
    }
  }

  test("None is returned when attempting to load non-existent application") {
    load("this-application-does-not-exist") should be(None)
  }

  private def uniqueApplicationName = "test-application-" + System.currentTimeMillis
}