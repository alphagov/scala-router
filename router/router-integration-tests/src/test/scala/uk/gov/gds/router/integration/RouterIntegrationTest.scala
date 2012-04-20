package uk.gov.gds.router.integration

import uk.gov.gds.router.util.JsonSerializer._
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.router.model.{Route, Application}
import org.apache.http.client.methods.HttpGet
import uk.gov.gds.router.util.JsonSerializer
import uk.gov.gds.router.MongoDatabaseBackedTest
import org.scalatest.GivenWhenThen

class RouterIntegrationTest
  extends MongoDatabaseBackedTest
  with ShouldMatchers
  with GivenWhenThen
  with HttpTestInterface {

  private val apiRoot = "http://localhost:4000/router"
  private val backendUrl = "localhost:4001/router-test-harness"
  private var applicationId: String = ""

  test("can create and delete applications") {
    given("A new application ID that does not exist in the router database")
    val applicationId = uniqueIdForTest

    when("We create an application with the new ID pointing at our test harness application")
    var response = post("/applications/" + applicationId, Map("backend_url" -> backendUrl))

    then("We should get a 201 (created) response that contains a JSON representation of our application")

    response.status should be(201)
    var application = fromJson[Application](response.body)
    application.application_id should be(applicationId)
    application.backend_url should be(backendUrl)

    when("We attempt to re-create the same application")
    response = post("/applications/" + applicationId, Map("backend_url" -> backendUrl))

    then("we should get a 409 (conflict) response")
    response.status should be(409)

    when("We attempt to load the application by issuing a GET to its API url")
    response = get("/applications/" + applicationId)

    then("We should get a 200 response that contains a JSON representation of our application ")
    response.status should be(200)
    application = fromJson[Application](response.body)
    application.application_id should be(applicationId)
    application.backend_url should be(backendUrl)

    // update
    when("We issue a PUT request to our applications URL that updates its backend URL to a new URL")
    response = put("/applications/" + applicationId, Map("backend_url" -> "new_backend_url"))

    then("We should get a 200 response that contains a JSON representation of our application ")
    response.status should be(200)
    application = fromJson[Application](response.body)
    application.application_id should be(applicationId)
    application.backend_url should be("new_backend_url")

    when("We attempt to delete the application")
    response = delete("/applications/" + applicationId)

    then("We should get a 204 response and the application should be gone")
    response.status should be(204)

    // check it's gone
    response = get("/applications/" + applicationId)
    response.status should be(404)
  }

  test("Can create application using put") {
    given("A unique application ID")

    val applicationId = uniqueIdForTest

    when("We attempt to create an application using PUT")

    val response = put("/applications/" + applicationId, Map("backend_url" -> backendUrl))

    then("We should get a 201 response signifying succesful creation")
    response.status should be(201)
  }

  test("Can get headers from response") {
    given("A URL on our test-harness application that sets the header X-Test")
    when("We get that URL through the router")
    val response = get("/route/test/set-header")

    then("The header should be present in the response to the client")
    response.headers.contains(Header("X-Test", "test")) should be(true)
  }

  test("Can create routes using put") {
    when("We create a route to our backend application with a PUT")
    val response = put("/routes/route-created-with-put", Map("application_id" -> applicationId, "route_type" -> "full"))

    then("We should get a 201 response signifying sucessful creation")
    response.status should be(201)
  }

  test("Can handle 304 responses from backend server") {
    given("A route on our test-harness that returns 304 when ever it is hit")
    when("when we hit it with either get or POST")
    then("the client response should be a 304")

    val response = get("/route/test/not-modified")
    response.status should be(304)

    val postResponse = post("/route/test/not-modified")
    postResponse.status should be(304)
  }

  test("Application metrics are created when application is created") {
    given("A freshly reset / re-deployed router with empty monitoring statistics")
    get("/management/status/reset")

    when("We make a request to the statistics API")
    val testApplicationMetrics = JsonSerializer.fromJson[List[Map[String, String]]](get("/management/status").body)

    then("All of the stats should be 0")
    testApplicationMetrics.map {
      metric =>
        logger.info("Checking metric " + metric("name"))
        metric("count") should be("0")
        metric("totalTime") should be("0")
    }
  }

  test("Routing GET traffic through an application increments the counter") {
    given("A freshly reset / re-deployed router with empty monitoring statistics")
    get("/management/status/reset")

    when("We make a GET request through the router to an arbitrary page")
    get("/route/fulltest/test.html")

    then("The hit should be recorded in the statistics for the router")
    val testApplicationMetrics = JsonSerializer.fromJson[List[Map[String, String]]](get("/management/status").body)
    val requestCounter = testApplicationMetrics.filter(metric => metric("name") == "router-requests").head

    requestCounter("count") should be("1")
    requestCounter("totalTime") should not be ("0")
  }

  test("Routing POST traffic through an application increments the counter") {
    given("A freshly reset / re-deployed router with empty monitoring statistics")
    get("/management/status/reset")

    when("We make a POST request through the router to an arbitrary page")
    post("/route/fulltest/test.html")

    then("The hit should be recorded in the statistics for the router")
    val testApplicationMetrics: List[Map[String, String]] = JsonSerializer.fromJson[List[Map[String, String]]](get("/management/status").body)
    val applicationCounter = testApplicationMetrics.filter(_("name") == applicationId).head

    applicationCounter("count") should be("1")
    applicationCounter("totalTime") should not be ("0")
  }

  test("canot create route on application that does not exist") {
    when("We attempt to create a route for a backend application that does not exists")
    val response = put("/routes/this-route-does-not-exist", Map("application_id" -> "this-app-does-not-exist", "incoming_path" -> "foo", "route_type" -> "foo"))

    then("We should fail with a server error")
    response.status should be(500)
  }

  test("Can create full routes") {
    given("A unique router ID that is not present in the router")
    val routeId = uniqueIdForTest

    when("We create that route with a route type of full")
    // create our route
    var response = post("/routes/" + routeId,
      Map(
        "application_id" -> applicationId,
        "route_type" -> "full"))

    then("We should get a 201 response with JSON representing the created router")
    // check it
    response.status should be(201)
    var route = fromJson[Route](response.body)
    route.application_id should be(applicationId)
    route.incoming_path should be(routeId)

    then("We should be able to retreive the route information through the router API")
    // get it
    response = get("/routes/" + routeId)
    response.status should be(200)
    route = fromJson[Route](response.body)
    route.application_id should be(applicationId)
    route.incoming_path should be(routeId)

    given("A newly created application")
    val newApplicationId = createTestApplication("update-application")

    when("We attempt to update the previously created route to point to this new application")
    // update
    response = put("/routes/" + routeId,
      Map(
        "application_id" -> newApplicationId,
        "route_type" -> "full"))

    then("We should get a response signifiying that the route has been updated")
    response.status should be(200)
    route = fromJson[Route](response.body)
    route.application_id should be(newApplicationId)
    route.incoming_path should be(routeId)

    when("We deleter the route")
    // delete
    response = delete("/routes/" + route.incoming_path)

    then("The route should be gone")
    response.status should be(204)

    when("We try to reload the route")
    // check it's gone
    response = get("/routes/" + routeId)

    then("the route still should be gone")
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