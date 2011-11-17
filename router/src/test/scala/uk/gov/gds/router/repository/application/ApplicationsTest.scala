package uk.gov.gds.router.repository.application

import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.router.model.Application
import uk.gov.gds.router.repository.application.Applications._
import uk.gov.gds.router.mongodb.MongoDatabase.onSameDatabaseServer
import uk.gov.gds.router.repository.{Conflict, NewlyCreated}
import uk.gov.gds.router.MongoDatabaseBackedTest

class ApplicationsTest extends MongoDatabaseBackedTest with ShouldMatchers {

  test("can get all applications") {
    Applications.all.size should be(0)
    val application = Application("one", "publisher.dev.gov.uk")
    store(application)
    Applications.all.size should be(1)
    Applications.all.head should be(application)
  }

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