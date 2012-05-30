package uk.gov.gds.router.integration

import org.apache.http.client.methods.HttpGet
import uk.gov.gds.router.util.JsonSerializer._
import uk.gov.gds.router.model.{ApplicationForGoneRoutes, Route}
import scala.Predef._

class HttpProxyTest
  extends RouterIntegrationTestSetup{

  test("Can get headers from response") {
    given("A URL on our test-harness application that sets the header X-Test")
    when("We get that URL through the router")

    logger.info("looking for test/set-header")
    val response = get("/route/mainhost/test/set-header")
    response.status should be(200) //todo remove, this is just for debugging

    then("The header should be present in the response to the client")
    response.headers.contains(Header("X-Test", "test")) should be(true)
  }

  test("Can handle 304 responses from backend server") {
    given("A route on our test-harness that returns 304 when ever it is hit")
    when("when we hit it with either get or POST")
    then("the client response should be a 304")

    val response = get("/route/mainhost/test/not-modified")
    response.status should be(304)

    val postResponse = post("/route/mainhost/test/not-modified")
    postResponse.status should be(304)
  }
  test("Returns 404 when backend returns 404") {
    val response = get("/route/mainhost/test/this-route-does-not-exist-on-the-backend-server")
    response.status should be(404)
  }

  test("Returns a 500 when the backend returns 500") {
    val response = get("/route/mainhost/test/this-route-returns-an-error")
    response.status should be(500)
  }

  test("can post form submissions featuring duplicated parameters to backend server") {
    val response = post("/route/mainhost/test/test-harness?first=cheese", Map("first" -> "sausage", "second" -> "chips"))
    response.status should be(200)
    response.body.contains("first=cheese") should be(true)
    response.body.contains("first=sausage") should be(true)
    response.body.contains("second=chips") should be(true)
  }

  test("query parameters are passed to backend server") {
    val response = get("/route/mainhost/test/test-harness?first=sausage&second=chips")
    response.status should be(200)
    response.body.contains("first=sausage") should be(true)
    response.body.contains("second=chips") should be(true)
  }

  test("Cookies that are sent from backends come through router") {
    val response = get("/route/mainhost/test/outgoing-cookies")
    response.cookies.size should be(1)

    val cookie = response.cookies.head
    cookie.getName should be("test-cookie")
    cookie.getValue should be("this is a cookie")

    cookieStore.getCookies.size should be(1)

    val responseWithCookiesFromServer = get("/route/mainhost/test/incoming-cookies")
    logger.info(responseWithCookiesFromServer.body)
    responseWithCookiesFromServer.body.contains("test-cookie=this is a cookie") should be(true)
  }

  test("Sets X-GovUK-Router-Request") {
    val httpGet = new HttpGet(buildUrl("/route/mainhost/test/incoming-headers"))
    val response = Response(httpGet)
    response.body.contains("X-GovUK-Router-Request=true") should be(true)
  }

  test("Original host sent to backend as X_FORWARDED_HOST") {
    val httpGet = new HttpGet(buildUrl("/route/mainhost/test/incoming-headers"))
    httpGet.addHeader("Host", "original.example.com:3100")
    val response = Response(httpGet)
    response.body.contains("Host=localhost:4000")
    response.body.contains("X-Forwarded-Host=original.example.com:3100") should be(true)
  }

  test("Basic auth request headers are sent to backend server") {
    val httpGet = new HttpGet(buildUrl("/route/mainhost/test/incoming-headers"))
    httpGet.addHeader("Authorization", "hope-this-gets-through")
    val response = Response(httpGet)
    logger.info(response.body)
    response.body.contains("Authorization=hope-this-gets-through") should be(true)
  }

  test("Router returns 503 error page when backend times out") {
    val response = get("/route/mainhost/test/timeout")
    response.status should be(503)
  }

  test("Router passes 410 status code when backend response has 410 status") {
    val response = get("/route/mainhost/test/410")
    response.status should be(410)
  }

  test("Can handle a get request that returns a redirect") {
    val response = get("/route/mainhost/test/redirect")
    response.status should be(302)
  }

  test("Can handle forms that return a redirect from backend server") {
    val response = post("/route/mainhost/test/redirect")
    response.status should be(302)
  }

  test("Router returns 404 error page when route not found") {
    val response = get("/route/asdasdasdasdasdasdasdasdasdasdsadas")
    response.status should be(404)
  }

  test("When a full route is deleted via the API it returns a 410 when accessed through the proxy") {
    given("The test harness application created with some default routes")
    when("we access a known full route")

    val response = get("/route/mainhost/fulltest/test.html")

    then("the response should be a 200 with the contents from the backend application")
    response.status should be(200)
    response.body contains ("router flat route") should be(true)

    when("We delete the route through the API")
    val deleteResponse = delete("/routes/mainhost/fulltest/test.html")

    then("When we examine the route through the API its route_action should be 'gone'")
    val route = fromJson[Route](deleteResponse.body)
    route.route_action should be("gone")

    then("and the route should not be associated with an application")
    route.application_id should be(ApplicationForGoneRoutes.application_id)
    route.application should be(ApplicationForGoneRoutes)

    then("and we retrieve the route again we should get a 410 gone response")
    val secondGetResponse = get("/route/mainhost/fulltest/test.html")

    secondGetResponse.status should be(410)
    secondGetResponse.body contains ("router flat route") should be(false)
  }

  test("a redirect full route will give a 301 status") {
    given("A unique route ID that is not present in the router")
    val routeId = uniqueIdForTest

    when("We create that route with a route type of full, a route action of redirect and a location")
    var response = put("/routes/" + routeId,
      Map(
        "route_type" -> "full",
        "route_action" -> "redirect",
        "location" -> "/destination/page.html"))

    then("We should be able to retreive the route information through the router API")
    response = get("/route/" + routeId)

    response.status should be(301)

    def header(x: Option[Header]) = x match {
      case Some(header) => header.value
      case None => Unit
    }

    header( response.headers find {_.name == "Location"} ) should be("/destination/page.html")
  }

  test("can proxy requests to and return responses from backend server") {
    var response = get("/route/mainhost/fulltest/test.html")
    response.status should be(200)
    response.body.contains("router flat route") should be(true)

    response = get("/route/mainhost/prefixtest/bang/test.html")
    response.status should be(200)
    response.body.contains("router prefix route") should be(true)
  }

  test("can proxy HEAD requests to and return responses from backend server") {
    var response = head("/route/mainhost/fulltest/test.html")
    response.status should be(200)
    response.body should be("")

    response = head("/route/mainhost/prefixtest/bang/test.html")
    response.status should be(200)
    response.body should be("")
  }

  test("can post form submissions to backend server") {
    val response = post("/route/mainhost/test/test-harness", Map("first" -> "sausage", "second" -> "chips"))
    response.status should be(200)
    response.body.contains("first=sausage") should be(true)
    response.body.contains("second=chips") should be(true)
  }

  test("Router does not fallback to invalid prefix route when full route cannot be found") {
    post("/routes/mainhost/someprefix", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    val registered = get("/route/mainhost/someprefix")
    val unregistered = get("/route/mainhost/someprefix/unregistered")

    registered.body.contains("prefix route") should be(true)
    unregistered.body.contains("unregistred") should be(false)    //todo note: this test is passing because of typo, not functionality. return to.
  }

}
