package uk.gov.gds.router.repository.route

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FunSuite}
import uk.gov.gds.router.mongodb.MongoDatabase._
import uk.gov.gds.router.repository.application.Applications
import uk.gov.gds.router.repository.route.Routes._
import uk.gov.gds.router.model._
import uk.gov.gds.router.repository.{Deleted, Conflict, NewlyCreated}

class RoutesTest extends FunSuite with ShouldMatchers with BeforeAndAfterEach {

  val testApplication = Application("unit-tests", "test.backend.server")
  Applications.store(testApplication)

  val testFullRoute = Route(
    application = testApplication,
    route_type = "full",
    incoming_path = "overridden")

  val testPrefixRoute = Route(
    application = testApplication,
    route_type = "prefix",
    incoming_path = "overridden")

  override protected def beforeEach() {
    deleteAllRoutesForApplication(testApplication.application_id)
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

      store(Route(applicationId = "unit-tests", routeType = "full", incomingPath = "foo/bar")) should be(Conflict)
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

      store(Route(applicationId = "unit-tests", routeType = "prefix", incomingPath = "foo")) should be(Conflict)
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