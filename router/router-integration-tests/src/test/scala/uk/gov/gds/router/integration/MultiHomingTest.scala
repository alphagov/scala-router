package uk.gov.gds.router.integration

import uk.gov.gds.router.util.JsonSerializer._
import uk.gov.gds.router.model.{ApplicationForGoneRoutes, Route}

class MultiHomingTest extends RouterIntegrationTestSetup {

  test("hostname routes to different application") {
    createAlsoSupportedTestApplication()

    val mainHomeResponse = get("/route/mainhost/fulltest/test.html")
    mainHomeResponse.status should be(200)

    mainHomeResponse.body.contains("router also supported full route") should be(false)
    mainHomeResponse.body.contains("router flat route") should be(true)

    val response = get("/route/alsosupported/fulltest/test.html")
    response.status should be(200)

    response.body.contains("router also supported full route") should be(true)
    response.body.contains("router flat route") should be(false)
  }


  test("different hosts can have the same prefix route") {

    val alsoSupportedApplicationId = createAlsoSupportedTestApplication()

    var creationResponse = post("/routes/mainhost/a-prefix-route", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    creationResponse.status should be(201)

    val mainHostResponse = get("/route/mainhost/a-prefix-route/foo")
    mainHostResponse.status should be(200)
    mainHostResponse.body.contains("foo!") should be(true)
    mainHostResponse.body.contains("oof!") should be(false)


    creationResponse = post("/routes/alsosupported/a-prefix-route", Map("application_id" -> alsoSupportedApplicationId, "route_type" -> "prefix")) //this is not creating
    creationResponse.status should be(201)

    val alsoSupportedHostResponse = get("/route/alsosupported/a-prefix-route/foo") //so this is defaulting to main? why?
    alsoSupportedHostResponse.status should be(200)
    alsoSupportedHostResponse.body.contains("foo!") should be(false)
    alsoSupportedHostResponse.body.contains("oof!") should be(true)
  }

  test("when two identical paths with different hosts exist, the correct one only is loaded / updated / deleted") {
    given("Two applications exist")
    val applicationId = createAlsoSupportedTestApplication()
    val newApplicationId = createMainTestApplication("long-john-silver")

    when("we create a route on the first host")
    var firstResponse = createRoute(applicationId, "ahoy", "proxy", "mainhost")

    then("it should be successful")
    firstResponse.status should be(201)

    var firstRoute = fromJson[Route](firstResponse.body)
    firstRoute.application_id should be(applicationId)
    firstRoute.incoming_path should be("ahoy")
    firstRoute.host should be("mainhost")

    when("we create a route on the second host")
    var secondResponse = createRoute(applicationId, "ahoy", "proxy", "alsosupported")

    then("it should be successful")
    secondResponse.status should be(201)

    var secondRoute = fromJson[Route](secondResponse.body)
    secondRoute.application_id should be(applicationId)
    secondRoute.incoming_path should be("ahoy")
    secondRoute.host should be("alsosupported")

    when("we update the first route")
    firstResponse = put("/routes/mainhost/ahoy", Map("application_id" -> newApplicationId, "route_type" -> "full"))

    then("it should be successful")
    firstResponse.status should be(200)

    firstRoute = fromJson[Route](firstResponse.body)
    firstRoute.application_id should be(newApplicationId)
    firstRoute.incoming_path should be("ahoy")
    firstRoute.host should be("mainhost")

    when("we update the second route")
    secondResponse = put("/routes/alsosupported/ahoy", Map("application_id" -> newApplicationId, "route_type" -> "full"))

    then("it should be successful")
    secondResponse.status should be(200)

    secondRoute = fromJson[Route](secondResponse.body)
    secondRoute.application_id should be(newApplicationId)
    secondRoute.incoming_path should be("ahoy")
    secondRoute.host should be("alsosupported")

    when("we delete the first route")
    firstResponse =  delete("/routes/mainhost/ahoy" )

    then("the deletion should be successful")
    firstResponse.status should be(200)

    then("the route should be 'Gone'")
    firstRoute = fromJson[Route](firstResponse.body)
    firstRoute.route_action should be("gone")
    firstRoute.application should be(ApplicationForGoneRoutes)

    then("the second route should still exist")
    secondResponse = get("route/alsosupported/ahoy")
    secondResponse.status should be(200)

    secondRoute = fromJson[Route](secondResponse.body)
    secondRoute.application_id should be(newApplicationId)
    secondRoute.incoming_path should be("ahoy")
    secondRoute.host should be("alsosupported")
  }

  test("when two identical hosts with different paths exist, the correct one only is loaded / updated / deleted") {
    given("Two applications exist")
    val applicationId = createMainTestApplication("ben-gunn")
    val newApplicationId = createMainTestApplication("jim-hawkins")

    when("we create a route on the first host")
    var firstResponse = createRoute(applicationId, "ahoy", "proxy", "mainhost")

    then("it should be successful")
    firstResponse.status should be(201)

    var firstRoute = fromJson[Route](firstResponse.body)
    firstRoute.application_id should be(applicationId)
    firstRoute.incoming_path should be("ahoy")
    firstRoute.host should be("mainhost")

    when("we create a route on the second host")
    var secondResponse = createRoute(applicationId, "arr", "proxy", "mainhost")

    then("it should be successful")
    secondResponse.status should be(201)

    var secondRoute = fromJson[Route](secondResponse.body)
    secondRoute.application_id should be(applicationId)
    secondRoute.incoming_path should be("arr")
    secondRoute.host should be("mainhost")

    when("we update the first route")
    firstResponse = put("/routes/mainhost/ahoy", Map("application_id" -> newApplicationId, "route_type" -> "full"))

    then("it should be successful")
    firstResponse.status should be(200)

    firstRoute = fromJson[Route](firstResponse.body)
    firstRoute.application_id should be(newApplicationId)
    firstRoute.incoming_path should be("ahoy")
    firstRoute.host should be("mainhost")

    when("we update the second route")
    secondResponse = put("/routes/mainhost/arr", Map("application_id" -> newApplicationId, "route_type" -> "full"))

    then("it should be successful")
    secondResponse.status should be(200)

    secondRoute = fromJson[Route](secondResponse.body)
    secondRoute.application_id should be(newApplicationId)
    secondRoute.incoming_path should be("arr")
    secondRoute.host should be("mainhost")

    when("we delete the first route")
    firstResponse =  delete("/routes/mainhost/ahoy" )

    then("the deletion should be successful")
    firstResponse.status should be(200)

    then("the route should be 'Gone'")
    firstRoute = fromJson[Route](firstResponse.body)
    firstRoute.route_action should be("gone")
    firstRoute.application should be(ApplicationForGoneRoutes)

    then("the second route should still exist")
    secondResponse = get("route/mainhost/arr")
    secondResponse.status should be(200)

    secondRoute = fromJson[Route](secondResponse.body)
    secondRoute.application_id should be(newApplicationId)
    secondRoute.incoming_path should be("arr")
    secondRoute.host should be("mainhost")
  }

  test("two identical hosts with identical paths cannot be created") {
    given("A unique route ID that is not present in the router")
    val incomingPath = uniqueIdForTest

    when("We create that route with a route type of prefix")
    var response = post("/routes/mainhost" + incomingPath,
      Map(
        "application_id" -> applicationId,
        "route_type" -> "prefix"))
    response.status should be(201)

    when("We try to create a route with the same host and same path")
    response = post("/routes/mainhost" + incomingPath,
      Map(
        "application_id" -> applicationId,
        "route_type" -> "prefix"))

    then("it should not be permitted")
    response.status should be(409)



  }

}
