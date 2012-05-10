package uk.gov.gds.router.integration

import uk.gov.gds.router.util.JsonSerializer
import org.apache.http.client.methods.HttpGet

class RouterIntegrationTest
  extends RouterIntegrationTestSetup {

  test("Can get headers from response") {
    given("A URL on our test-harness application that sets the header X-Test")
    when("We get that URL through the router")
    val response = get("/route/test/set-header")

    then("The header should be present in the response to the client")
    response.headers.contains(Header("X-Test", "test")) should be(true)
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

  test("Returns 404 when backend returns 404") {
    val response = get("/route/test/this-route-does-not-exist-on-the-backend-server")
    response.status should be(404)
  }

  test("Returns a 500 when the backend returns 500") {
    val response = get("/route/test/this-route-returns-an-error")
    response.status should be(500)
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

  test("Router returns 503 error page when backend times out") {
    val response = get("/route/test/timeout")
    response.status should be(503)
  }

  test("Router passes 410 status code when backend response has 410 status") {
    val response = get("/route/test/410")
    response.status should be(410)
  }

  //todo ??
  test("Can handle a get request that returns a redirect") {
    val response = get("/route/test/redirect")
    response.status should be(302)
  }

  //todo ??
  test("Can handle forms that return a redirect from backend server") {
    val response = post("/route/test/redirect")
    response.status should be(302)
  }

}
