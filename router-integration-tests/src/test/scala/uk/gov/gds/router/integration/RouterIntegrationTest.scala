package uk.gov.gds.router.integration

import uk.gov.gds.router.util.JsonSerializer._
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.router.model.{Route, Application}
import org.apache.http.client.methods.HttpGet
import uk.gov.gds.router.util.JsonSerializer
import uk.gov.gds.router.{MongoDatabaseBackedTest}

class RouterIntegrationTest extends MongoDatabaseBackedTest with ShouldMatchers with HttpTestInterface {

  private val apiRoot = "http://localhost:4000/router"
  private val backendUrl = "localhost:4001/router-test-harness"
  private var applicationId: String = ""

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

  test("Can create application using put") {
    val applicationId = uniqueIdForTest

    val response = put("/applications/" + applicationId, Map("backend_url" -> backendUrl))
    response.status should be(201)
  }

  test("Can get headers from response") {
    val response = get("/route/test/set-header")
    response.headers.contains(Header("X-Test", "test")) should be(true)
  }

  test("Can create routes using put") {
    val response = put("/routes/route-created-with-put", Map("application_id" -> applicationId, "route_type" -> "full"))
    response.status should be(201)
  }

  test("Can handle 304 responses from backend server") {
    val response = get("/route/test/not-modified")
    response.status should be(304)

    val postResponse = post("/route/test/not-modified")
    postResponse.status should be(304)
  }

  test("Application metrics are created when application is created") {
    get("/management/status/reset")
    val testApplicationMetrics = JsonSerializer.fromJson[List[Map[String, String]]](get("/management/status").body)

    testApplicationMetrics.map {
      metric =>
        logger.info("Checking metric " + metric("name"))
        metric("count") should be("0")
        metric("totalTime") should be("0")
    }
  }

  test("Routing GET traffic through an application increments the counter") {
    get("/management/status/reset")
    get("/route/fulltest/test.html")

    val testApplicationMetrics = JsonSerializer.fromJson[List[Map[String, String]]](get("/management/status").body)
    val requestCounter = testApplicationMetrics.filter(metric => metric("name") == "router-requests").head

    requestCounter("count") should be("1")
    requestCounter("totalTime") should not be ("0")
  }

  test("Routing POST traffic through an application increments the counter") {
    get("/management/status/reset")
    post("/route/fulltest/test.html")

    val testApplicationMetrics: List[Map[String, String]] = JsonSerializer.fromJson[List[Map[String, String]]](get("/management/status").body)
    val applicationCounter = testApplicationMetrics.filter(_("name") == applicationId).head

    applicationCounter("count") should be("1")
    applicationCounter("totalTime") should not be ("0")
  }

  test("canot create route on application that does not exist") {
    val response = put("/routes/this-route-does-not-exist", Map("application_id" -> "this-app-does-not-exist", "incoming_path" -> "foo", "route_type" -> "foo"))
    response.status should be(500)
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
    route.application_id should be(applicationId)
    route.incoming_path should be(routeId)

    // get it
    response = get("/routes/" + routeId)
    response.status should be(200)
    route = fromJson[Route](response.body)
    route.application_id should be(applicationId)
    route.incoming_path should be(routeId)

    val newApplicationId = createTestApplication("update-application")

    // update
    response = put("/routes/" + routeId,
      Map(
        "application_id" -> newApplicationId,
        "route_type" -> "full"))

    response.status should be(200)
    route = fromJson[Route](response.body)
    route.application_id should be(newApplicationId)
    route.incoming_path should be(routeId)

    // delete
    response = delete("/routes/" + route.incoming_path)
    response.status should be(204)

    // check it's gone
    response = get("/routes/" + routeId)
    response.status should be(404)
  }

  test("can proxy requests to and return responses from backend server") {
    var response = get("/route/fulltest/test.html")
    response.status should be(200)
    response.body.contains("router flat route") should be(true)

    response = get("/route/prefixtest/bang/test.html")
    response.status should be(200)
    response.body.contains("router prefix route") should be(true)
  }

  test("can proxy HEAD requests to and return responses from backend server") {
    var response = head("/route/fulltest/test.html")
    response.status should be(200)
    response.body should be("")

    response = head("/route/prefixtest/bang/test.html")
    response.status should be(200)
    response.body should be("")
  }

  test("can post form submissions to backend server") {
    val response = post("/route/test/test-harness", Map("first" -> "sausage", "second" -> "chips"))
    response.status should be(200)
    response.body.contains("first=sausage") should be(true)
    response.body.contains("second=chips") should be(true)
  }

  test("Returns 404 when backend returns 404") {
    val response = get("/route/test/this-route-does-not-exist-on-the-backend-server")
    response.status should be(404)
  }

  test("Returns a 500 when the backend returns 500") {
    val response = get("/route/test/this-route-returns-an-error")
    response.status should be(500)
  }

  test("Can handle a get request that returns a redirect") {
    val response = get("/route/test/redirect")
    response.status should be(302)
  }

  test("Can handle forms that return a redirect from backend server") {
    val response = post("/route/test/redirect")
    response.status should be(302)
  }

  test("can post form submissions featuring duplicated parameters to backend server") {
    val response = post("/route/test/test-harness?first=cheese", Map("first" -> "sausage", "second" -> "chips"))
    response.status should be(200)
    response.body.contains("first=cheese") should be(true)
    response.body.contains("first=sausage") should be(true)
    response.body.contains("second=chips") should be(true)
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

  test("Cookies that are sent from backends come through router") {
    val response = get("/route/test/outgoing-cookies")
    response.cookies.size should be(1)

    val cookie = response.cookies.head
    cookie.getName should be("test-cookie")
    cookie.getValue should be("this is a cookie")

    cookieStore.getCookies.size should be(1)

    val responseWithCookiesFromServer = get("/route/test/incoming-cookies")
    logger.info(responseWithCookiesFromServer.body)
    responseWithCookiesFromServer.body.contains("test-cookie=this is a cookie") should be(true)
  }

  test("Sets X-GovUK-Router-Request") {
    val httpGet = new HttpGet(buildUrl("/route/test/incoming-headers"))
    val response = Response(httpGet)
    response.body.contains("X-GovUK-Router-Request=true") should be(true)
  }

  test("Original host sent to backend as X_FORWARDED_HOST") {
    val httpGet = new HttpGet(buildUrl("/route/test/incoming-headers"))
    httpGet.addHeader("Host", "original.example.com:3100")
    val response = Response(httpGet)
    response.body.contains("Host=localhost:4000")
    response.body.contains("X-Forwarded-Host=original.example.com:3100") should be(true)
  }

  test("Basic auth request headers are sent to backend server") {
    val httpGet = new HttpGet(buildUrl("/route/test/incoming-headers"))
    httpGet.addHeader("Authorization", "hope-this-gets-through")
    val response = Response(httpGet)
    logger.info(response.body)
    response.body.contains("Authorization=hope-this-gets-through") should be(true)
  }

  test("Router does not fallback to invalid prefix route when full route cannot be found") {
    post("/routes/someprefix", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    val registered = get("/route/someprefix")
    val unregistered = get("/route/someprefix/unregistered")

    registered.body.contains("prefix route") should be(true)
    unregistered.body.contains("unregsitered") should be(false)
  }

  test("Can create full routes with more than one path element") {
    val response = post("/routes/valid/full/route", Map("application_id" -> applicationId, "route_type" -> "full"))
    response.status should be(201)
  }

  test("can create a full route that overrides an existing prefix route") {
    val creationResponse = post("/routes/a-prefix-route", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    creationResponse.status should be(201)

    val fullRouteResponse = post("/routes/a-prefix-route/foo/bar", Map("application_id" -> applicationId, "route_type" -> "full"))
    fullRouteResponse.status should be(201)
    val createdRoute = fromJson[Route](fullRouteResponse.body)

    createdRoute.incoming_path should be("a-prefix-route/foo/bar")
  }

  test("Cannot create a full route that conflicts with an existing full route") {
    createRoute(routePath = "foo/bar", applicationId = applicationId, routeType = "full")

    val conflictedResponse = createRoute(routePath = "foo/bar", routeType = "full", applicationId = applicationId)
    conflictedResponse.status should be(409)
    val conflictedRoute = fromJson[Route](conflictedResponse.body)

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

  test("Router returns 404 error page when route not found") {
    val response = get("/route/asdasdasdasdasdasdasdasdasdasdsadas")
    response.status should be(404)
  }

  test("Router returns 503 error page when backend times out") {
    val response = get("/route/test/timeout")
    response.status should be(503)
  }

  test("Router passes 410 status code when backend response has 410 status") {
    val response = get("/route/test/410")
    response.status should be(410)
  }

  override protected def beforeEach() {
    super.beforeEach()
    RouterTestHarnessContainer.start()
    RouterContainer.start()
    applicationId = createTestApplication()
    cookieStore.clear()
  }

  override protected def afterEach() {
    super.afterEach()
    RouterTestHarnessContainer.stopUnlessSomeoneCallsStartAgainSoon()
    RouterContainer.stopUnlessSomeoneCallsStartAgainSoon()
  }

  private def uniqueIdForTest = "integration-test-" + System.currentTimeMillis()

  override def buildUrl(path: String) = apiRoot + path

  private def createRoute(applicationId: String, routePath: String, routeType: String) =
    post("/routes/" + routePath, Map("application_id" -> applicationId, "route_type" -> routeType))

  private def createTestApplication(): String = {
    val applicationId = uniqueIdForTest
    createTestApplication(applicationId)
    applicationId
  }

  private def createTestApplication(applicationId: String): String = {
    post("/applications/" + applicationId, Map("backend_url" -> backendUrl))
    post("/routes/fulltest/test.html", Map("application_id" -> applicationId, "route_type" -> "full"))
    post("/routes/prefixtest", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    post("/routes/test", Map("application_id" -> applicationId, "route_type" -> "prefix"))

    applicationId
  }
}
