package uk.gov.gds.router.integration

import gov.uk.gds.router.ApplicationsUnderTest
import uk.gov.gds.router.HttpTestInterface
import uk.gov.gds.router.mongodb.MongoDatabase.database
import uk.gov.gds.router.util.JsonSerializer._
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.router.model.{Route, Application}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class RouterIntegrationTest extends FunSuite with ShouldMatchers with BeforeAndAfterEach with HttpTestInterface {
  private val apiRoot = "http://localhost:8080/router"
  private val sholdCleanOutDatabaseAfterEachTest = true
  private val backendUrl = "localhost:8080/router"
  private var applicationId: String = ""

  override protected def beforeEach() {
    ApplicationsUnderTest.start()
    applicationId = createTestApplication
  }

  override protected def afterEach() {
    ApplicationsUnderTest.stopUnlessSomeoneCallsStartAgainSoon()
    CookieStore.requestCookies.clear()

    if (sholdCleanOutDatabaseAfterEachTest) {
      database("applications").drop()
      database("routes").drop()
    }
  }

  private def uniqueIdForTest = "integration-test-" + System.currentTimeMillis()

  override def buildUrl(path: String) = apiRoot + path

  test("can create and delete applications") {
    //create
    val applicationId = uniqueIdForTest

    var response = post("/applications/" + applicationId, Map("backend_url" -> backendUrl))
    response.status should be(201)

    var application = fromJson[Application](response.body)
    application.application_id should be(applicationId)
    application.backend_url should be(backendUrl)

    // re-create
    response = post("/applications/" + applicationId, Map("backend_url" -> backendUrl))
    response.status should be(409)

    //get
    response = get("/applications/" + applicationId)
    response.status should be(200)
    application = fromJson[Application](response.body)
    application.application_id should be(applicationId)
    application.backend_url should be(backendUrl)

    // update
    response = put("/applications/" + applicationId, Map("backend_url" -> "new_backend_url"))
    response.status should be(200)
    application = fromJson[Application](response.body)
    application.application_id should be(applicationId)
    application.backend_url should be("new_backend_url")

    // delete
    response = delete("/applications/" + applicationId)
    response.status should be(204)

    // check it's gone
    response = get("/applications/" + applicationId)
    response.status should be(404)
  }

  test("canot create route on application that does not exist") {
    val response = put("/applications/this-app-does-not-exist", Map("backend_url" -> "foo"))
    response.status should be(404)
  }

  test("Can create prefix routes") {
    val routeId = uniqueIdForTest

    // create our route
    var response = post("/routes/" + routeId,
      Map(
        "application_id" -> applicationId,
        "route_type" -> "full"))

    // check it
    response.status should be(201)
    var route = fromJson[Route](response.body)
    route.application.application_id should be(applicationId)
    route.incoming_path should be(routeId)

    // get it
    response = get("/route-for/" + routeId)
    response.status should be(200)
    route = fromJson[Route](response.body)
    route.application.application_id should be(applicationId)
    route.incoming_path should be(routeId)

    val newApplicationId = createTestApplication("update-application")

    // update
    response = put("/routes/" + routeId,
      Map(
        "application.application_id" -> newApplicationId,
        "route_type" -> "full"))

    response.status should be(200)
    route = fromJson[Route](response.body)
    route.application.application_id should be(newApplicationId)
    route.incoming_path should be(routeId)

    // delete
    response = delete("/routes/" + route.incoming_path)
    response.status should be(204)

    // check it's gone
    response = get("/route-for/" + routeId)
    response.status should be(404)
  }

  test("can proxy files from backend server") {
    var response = get("/route/fulltest/test.html")
    response.status should be(200)
    response.body.contains("router flat route") should be(true)

    response = get("/route/prefixtest/bang/test.html")
    response.status should be(200)
    response.body.contains("router prefix route") should be(true)
  }

  test("can post form submissions to backend server") {
    val response = post("/route/test/test-harness", Map("first" -> "sausage", "second" -> "chips"))
    response.status should be(200)
    response.body.contains("first=sausage") should be(true)
    response.body.contains("second=chips") should be(true)
  }

  test("can post form submissions featuring duplicated parameters to backend server") {

  }

  test("query parameters are passed to backend server") {
    val response = get("/route/test/test-harness?first=sausage&second=chips")
    response.status should be(200)
    response.body.contains("first=sausage") should be(true)
    response.body.contains("second=chips") should be(true)
  }

  test("Cannot create prefix routes with more than one path element") {
    val response = post("/routes/invalid/prefix/route", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    response.status should be(500)
  }

  test("Can create full routes with more than one path element") {
    val response = post("/routes/valid/full/route", Map("application_id" -> applicationId, "route_type" -> "full"))
    response.status should be(201)
  }

  test("cannot create a full route that conficts with an existing prefix route") {
    val creationResponse = post("/routes/a-prefix-route", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    creationResponse.status should be(201)

    val confictedResponse = post("/routes/a-prefix-route/foo/bar", Map("application_id" -> applicationId, "route_type" -> "full"))
    confictedResponse.status should be(409)
    val conflictedRoute = fromJson[Route](confictedResponse.body)

    conflictedRoute.incoming_path should be("a-prefix-route")
  }

  test("Cannot create a full route that conflicts with an existing full route") {
    createRoute(routePath = "foo/bar", applicationId = applicationId, routeType = "full")

    val confictedResponse = createRoute(routePath = "foo/bar", routeType = "full", applicationId = applicationId)
    confictedResponse.status should be(409)
    val conflictedRoute = fromJson[Route](confictedResponse.body)

    conflictedRoute.incoming_path should be("foo/bar")
  }

  test("Overlapping prefix routes should be possible and should map to the correct application") {
    val fooApplicationId = createTestApplication
    val footballApplicationId = createTestApplication

    createRoute(routeType = "prefix", routePath = "foo", applicationId = fooApplicationId)

    var response = createRoute(routeType = "full", routePath = "football", applicationId = footballApplicationId)
    response.status should be(201)

    response = get("/route/foo")
    response.body.contains("fooOnly") should be(true)

    response = get("/route/foo/")
    response.body.contains("fooOnly") should be(true)

    response = get("/route/foo/bar")
    response.body.contains("fooOnly") should be(true)

    response = get("/route/football")
    response.body.contains("football") should be(true)
  }

  private def createRoute(applicationId: String, routePath: String, routeType: String) =
    post("/routes/" + routePath, Map("application_id" -> applicationId, "route_type" -> routeType))

  private def createTestApplication: String = {
    val applicationId = uniqueIdForTest
    createTestApplication(applicationId)
    applicationId
  }

  private def createTestApplication(applicationId: String): String = {
    post("/applications/" + applicationId, Map("backend_url" -> backendUrl))
    post("/routes/prefixtest", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    post("/routes/fulltest/test.html", Map("application_id" -> applicationId, "route_type" -> "full"))
    post("/routes/test/test-harness", Map("application_id" -> applicationId, "route_type" -> "full"))
    applicationId
  }

}