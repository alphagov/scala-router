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
  Applications.store(testApplication)

  val fullRouteTemplate = Route(
    application_id = testApplication.application_id,
    route_type = "full",
    incoming_path = "overridden")

  val prefixRouteTemplate = Route(
    application_id = testApplication.application_id,
    route_type = "prefix",
    incoming_path = "overridden")

  override protected def beforeEach() = {
    super.beforeEach()
    Applications.store(testApplication)
  }

  test("can create and retrieve full routes") {
    onSameDatabaseServer {
      store(fullRouteTemplate.copy(incoming_path = "foo/bar")) should be(NewlyCreated)

      load("foo/bar") match {
        case None => fail("Should have found route in database")

        case Some(loadedRoute) =>
          loadedRoute.route_type should be("full")
          loadedRoute.proxyType should be(FullRoute)
          loadedRoute.application should be(testApplication)
          loadedRoute.incoming_path should be("foo/bar")
      }

      store(Route(application_id = "unit-tests", route_type = "full", incoming_path = "foo/bar")) should be(Conflict)
    }
  }

  test("can create and retrieve prefix routes") {
    onSameDatabaseServer {
      store(prefixRouteTemplate.copy(incoming_path = "foo")) should be(NewlyCreated)

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
      store(prefixRouteTemplate.copy(incoming_path = "foo")) should be(NewlyCreated)

      load("foo") match {
        case None => fail("Should have found route in database")
        case _ =>
      }
    }
  }

  test("can match prefix route with trailing slash") {
    onSameDatabaseServer {
      store(prefixRouteTemplate.copy(incoming_path = "foo")) should be(NewlyCreated)

      load("foo/") match {
        case None => fail("Should have found route in database")
        case Some(loadedRoute) =>
      }
    }
  }

  test("Falls back to prefix route if full route cannot be found") {
    onSameDatabaseServer {
      store(prefixRouteTemplate.copy(incoming_path = "someprefix"))

      load("someprefix/unregistered") match {
        case None => fail("Should have found prefix route /someprefix")
        case Some(route) if (route.incoming_path.equals("someprefix")) => // Good
        case Some(route) => fail("Should not have found route " + route)
      }
    }
  }

  test("Can create full route within prefix route when prefix route is created first") {
    overrideRouteTest {
      val fullApplication = Application("full-route-app", "some.backend.server")
      Applications.store(fullApplication)

      val prefixRoute = prefixRouteTemplate.copy(incoming_path = "foo")
      val fullRoute = Route(incoming_path = "foo/bar", route_type = "full", application_id = fullApplication.id)
      store(prefixRoute) should be(NewlyCreated)
      store(fullRoute) should be(NewlyCreated)

      (fullRoute, prefixRoute)
    }
  }

  test("Can create full route within prefix route when full route is created first") {
    overrideRouteTest {
      val fullApplication = Application("full-route-app", "some.backend.server")
      Applications.store(fullApplication)

      val prefixRoute = prefixRouteTemplate.copy(incoming_path = "foo")
      val fullRoute = Route(incoming_path = "foo/bar", route_type = "full", application_id = fullApplication.id)
      store(fullRoute) should be(NewlyCreated)
      store(prefixRoute) should be(NewlyCreated)

      (fullRoute, prefixRoute)
    }
  }

  test("Does not fall back to full route if requested full route cannot be found") {
    onSameDatabaseServer {
      store(fullRouteTemplate.copy(incoming_path = "/foo/bar"))

      load("/foo/bar/bang") match {
        case Some(route) => fail("Should not have resolved route" + route)
        case None => // OK
      }
    }
  }

  test("can delete routes for application") {
    onSameDatabaseServer {
      store(fullRouteTemplate.copy(incoming_path = "sausages"))
      store(fullRouteTemplate.copy(incoming_path = "cheese"))

      load("sausages").isDefined should be(true)
      load("cheese").isDefined should be(true)

      deleteAllRoutesForApplication(testApplication.application_id)

      load("sausages").isDefined should be(false)
      load("cheese").isDefined should be(false)
    }
  }

  test("can delete routes") {
    onSameDatabaseServer {
      store(fullRouteTemplate.copy(incoming_path = "cheese"))

      load("cheese").isDefined should be(true)
      delete("cheese") should be(Deleted)
      load("cheese").isDefined should be(false)
    }
  }

  private def overrideRouteTest(block: => (Route, Route)) {
    onSameDatabaseServer {
      val routes = block
      val fullRoute = routes._1
      val prefixRoute = routes._2

      load("foo") match {
        case Some(route) => route should be(prefixRoute)
        case None => fail("Should have found " + prefixRoute)
      }

      load("foo/bar") match {
        case Some(route) => route should be(fullRoute)
        case _ => fail("Should have found " + fullRoute)
      }
    }
  }
}