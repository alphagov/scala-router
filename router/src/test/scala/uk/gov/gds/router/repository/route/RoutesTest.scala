package uk.gov.gds.router.repository.route

import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.router.mongodb.MongoDatabase._
import uk.gov.gds.router.repository.application.Applications
import uk.gov.gds.router.repository.route.Routes._
import uk.gov.gds.router.model._
import uk.gov.gds.router.repository.{Deleted, Conflict, NewlyCreated}
import uk.gov.gds.router.MongoDatabaseBackedTest

class RoutesTest extends MongoDatabaseBackedTest with ShouldMatchers {

  val testApplication = Application("unit-tests", "test.backend.server")

  val testFullRoute = Route(
    application_id = testApplication.application_id,
    route_type = "full",
    incoming_path = "overridden")

  val testPrefixRoute = Route(
    application_id = testApplication.application_id,
    route_type = "prefix",
    incoming_path = "overridden")

  override protected def beforeEach() = {
    super.beforeEach()
    Applications.store(testApplication)
  }

  test("can create and retrieve full routes") {
    onSameDatabaseServer {
      store(testFullRoute.copy(incoming_path = "foo/bar")) should be(NewlyCreated)

      load("foo/bar") match {
        case None => fail("Should have found route in database")

        case Some(loadedRoute) =>
          loadedRoute.route_type should be("full")
          loadedRoute.proxyType should be(FullRoute)
          loadedRoute.application should be(testApplication)
          loadedRoute.incoming_path should be("foo/bar")
      }

      store(Route(application_id = "unit-tests", route_type= "full", incoming_path = "foo/bar")) should be(Conflict)
    }
  }

  test("can create and retrieve prefix routes") {
    onSameDatabaseServer {
      store(testPrefixRoute.copy(incoming_path = "foo")) should be(NewlyCreated)

      load("foo/bar/baz") match {
        case None => fail("Should have found route in database")

        case Some(loadedRoute) =>
          loadedRoute.route_type should be("prefix")
          loadedRoute.proxyType should be(PrefixRoute)
          loadedRoute.application should be(testApplication)
          loadedRoute.incoming_path should be("foo")
      }

      store(Route(application_id = "unit-tests", route_type = "prefix", incoming_path = "foo")) should be(Conflict)
    }
  }

  test("can match exact prefix route") {
    onSameDatabaseServer {
      store(testPrefixRoute.copy(incoming_path = "foo")) should be(NewlyCreated)

      load("foo") match {
        case None => fail("Should have found route in database")
        case _ =>
      }
    }
  }

  test("can match prefix route with trailing slash") {
    onSameDatabaseServer {
      store(testPrefixRoute.copy(incoming_path = "foo")) should be(NewlyCreated)

      load("foo/") match {
        case None => fail("Should have found route in database")
        case Some(loadedRoute) =>
      }
    }
  }

  test("can delete routes for application") {
    onSameDatabaseServer {
      store(testFullRoute.copy(incoming_path = "sausages"))
      store(testFullRoute.copy(incoming_path = "cheese"))

      load("sausages").isDefined should be(true)
      load("cheese").isDefined should be(true)

      deleteAllRoutesForApplication(testApplication.application_id)

      load("sausages").isDefined should be(false)
      load("cheese").isDefined should be(false)
    }
  }

  test("can delete routes") {
    onSameDatabaseServer {
      store(testFullRoute.copy(incoming_path = "cheese"))

      load("cheese").isDefined should be(true)
      delete("cheese") should be(Deleted)
      load("cheese").isDefined should be(false)
    }
  }
}