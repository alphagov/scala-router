package uk.gov.gds.router.integration

import uk.gov.gds.router.util.JsonSerializer._
import uk.gov.gds.router.model._

class RoutesLifecycleTest
  extends RouterIntegrationTestSetup {

  test("Can create routes using put") {
    when("We create a route to our backend application with a PUT")
    val response = put("/routes/route-created-with-put", Map("application_id" -> applicationId, "route_type" -> "full"))

    then("We should get a 201 response signifying sucessful creation")
    response.status should be(201)
  }

  test("canot create route on application that does not exist") {
    when("We attempt to create a route for a backend application that does not exists")
    val response = put("/routes/this-route-does-not-exist", Map("application_id" -> "this-app-does-not-exist", "incoming_path" -> "foo", "route_type" -> "foo"))

    then("We should fail with a server error")
    response.status should be(500)
  }

  test("Can create / update / delete full routes") {
    given("A unique route ID that is not present in the router")
    val incomingPath = uniqueIdForTest

    when("We create that route with a route type of full")
    var response = post("/routes/" + incomingPath,
      Map(
        "application_id" -> applicationId,
        "route_type" -> "full"))

    then("We should get a 201 response with JSON representing the created router")
    response.status should be(201)
    var route = fromJson[Route](response.body)
    route.application_id should be(applicationId)
    route.incoming_path should be(incomingPath)
    route.proxyType should be(FullRoute)
    route.route_action should be("proxy")

    then("We should be able to retrieve the route information through the router API")
    response = get("/routes/" + incomingPath)
    response.status should be(200)
    route = fromJson[Route](response.body)
    route.application_id should be(applicationId)
    route.incoming_path should be(incomingPath)

    given("A newly created application")
    val newApplicationId = createMainTestApplication("update-application")

    when("We attempt to update the previously created route to point to this new application")
    response = put("/routes/" + incomingPath,
      Map(
        "application_id" -> newApplicationId,
        "route_type" -> "full"))

    then("We should get a response signifiying that the route has been updated")
    response.status should be(200)
    route = fromJson[Route](response.body)
    route.application_id should be(newApplicationId)
    route.incoming_path should be(incomingPath)

    when("We delete the route")
    response = delete("/routes/" + route.incoming_path)

    then("The route should be gone")
    val deletedRoute = fromJson[Route](response.body)
    response.status should be(200)
    deletedRoute.route_action should be("gone")
    deletedRoute.application should be(ApplicationForGoneRoutes)

    when("We try to reload the route")
    response = get("/routes/" + incomingPath)

    then("the route still should be gone")
    fromJson[Route](response.body).route_action should be("gone")
  }

  test("Can create / update / delete prefix routes") {
    given("A unique route ID that is not present in the router")
    val incomingPath = uniqueIdForTest

    when("We create that route with a route type of prefix")
    var response = post("/routes/" + incomingPath,
      Map(
        "application_id" -> applicationId,
        "route_type" -> "prefix"))

    then("We should get a 201 response with JSON representing the created route")
    response.status should be(201)
    var route = fromJson[Route](response.body)
    route.application_id should be(applicationId)
    route.incoming_path should be(incomingPath)
    route.proxyType should be(PrefixRoute)
    route.route_action should be("proxy")

    then("We should be able to retreive the route information through the router API")
    response = get("/routes/" + incomingPath)
    response.status should be(200)
    route = fromJson[Route](response.body)
    route.application_id should be(applicationId)
    route.incoming_path should be(incomingPath)

    given("A newly created application")
    val newApplicationId = createMainTestApplication("update-application")

    when("We attempt to update the previously created route to point to this new application")
    response = put("/routes/" + incomingPath,
      Map(
        "application_id" -> newApplicationId,
        "route_type" -> "prefix"))

    then("We should get a response signifiying that the route has been updated")
    response.status should be(200)
    route = fromJson[Route](response.body)
    route.application_id should be(newApplicationId)
    route.incoming_path should be(incomingPath)

    when("We delete the route")
    response = delete("/routes/" + route.incoming_path)

    then("The route should be gone")
    response.status should be(204)

    when("We try to reload the route")
    response = get("/routes/" + incomingPath)

    then("the route still should be gone")
    response.status should be(404)
  }

  test("a redirect route is given the application id of the application for redirect routes") {
    given("A unique route ID that is not present in the router")
    val incomingPath = uniqueIdForTest

    when("We create that route with a route type of full, a route action of redirect and a location")
    val response = post("/routes/" + incomingPath,
      Map(
        "route_type" -> "full",
        "route_action" -> "redirect",
        "location" -> "/destination/page.html"))

    then("the application id should be that of the application for redirect routes")
    val route = fromJson[Route](response.body)
    route.application_id should be(ApplicationForRedirectRoutes.id)
  }

  test("a gone route is given the application id of the application for gone routes") {
    given("A unique route ID that is not present in the router")
    val incomingPath = uniqueIdForTest

    when("We create that route with a route type of full, a route action of gone")
    val response = post("/routes/" + incomingPath,
      Map(
        "route_type" -> "full",
        "route_action" -> "gone"))

    then("the application id should be that of the application for gone routes")
    val route = fromJson[Route](response.body)
    route.application_id should be(ApplicationForGoneRoutes.id)
  }

  test("a proxy route cannot have a location") {
    given("A unique route ID that is not present in the router")
    val incomingPath = uniqueIdForTest

    when("We create that route with a route type of full, no route action and a location")
    val response = post("/routes/" + incomingPath,
      Map(
        "application_id" -> ApplicationForRedirectRoutes.application_id,
        "route_type" -> "full",
        "location" -> "/destination/page.html"))

    then("the server should return an error")
    response.status should be(500)
  }

  test("a gone route cannot have a location") {
    given("A unique route ID that is not present in the router")
    val incomingPath = uniqueIdForTest

    when("We create that route with a route type of full, a route action of gone and a location")
    val response = post("/routes/" + incomingPath,
      Map(
        "application_id" -> ApplicationForRedirectRoutes.application_id,
        "route_type" -> "full",
        "route_action" -> "gone",
        "location" -> "/destination/page.html"))

    then("the server should return an error")
    response.status should be(500)
  }

  test("a redirect route must have a location") {
    given("A unique route ID that is not present in the router")
    val incomingPath = uniqueIdForTest

    when("We create that route with a route type of full, a route action of redirect and no location")
    val response = post("/routes/" + incomingPath,
      Map(
        "application_id" -> ApplicationForRedirectRoutes.application_id,
        "route_type" -> "full",
        "route_action" -> "redirect"))
    response.status should be(500)
  }

  test("a redirect route cannot have an empty string location") {
    given("A unique route ID that is not present in the router")
    val incomingPath = uniqueIdForTest

    when("We create that route with a route type of full, a route action of redirect and an empty location")
    val response = post("/routes/" + incomingPath,
      Map(
        "application_id" -> ApplicationForRedirectRoutes.application_id,
        "route_type" -> "full",
        "route_action" -> "redirect",
        "location" -> ""))
    response.status should be(500)
  }

  test("a prefix redirect route can be updated"){
    given("A unique route ID that is not present in the router")
    val incomingPath = uniqueIdForTest

    when("We create that route with a route type of prefix, a route action of redirect and a location")
    var response = put("/routes/" + incomingPath,
      Map(
        "application_id" -> ApplicationForRedirectRoutes.application_id,
        "route_type" -> "prefix",
        "route_action" -> "redirect",
        "location" -> "/redirect"))
    response.status should be(201)

    var createdRoute = fromJson[Route](response.body)
    createdRoute.properties("location") should be("/redirect")

    when("we update that route")
    response = put("/routes/" + incomingPath,
      Map(
        "application_id" -> ApplicationForRedirectRoutes.application_id,
        "route_type" -> "full",
        "route_action" -> "redirect",
        "location" -> "/another-redirect"))
    response.status should be(200)

    createdRoute = fromJson[Route](response.body)
    createdRoute.properties("location") should be("/another-redirect")
  }

  test("a full redirect route can be updated"){
    given("A unique route ID that is not present in the router")
    val incomingPath = uniqueIdForTest

    when("We create that route with a route type of full, a route action of redirect and a location")
    var response = put("/routes/" + incomingPath,
      Map(
        "application_id" -> ApplicationForRedirectRoutes.application_id,
        "route_type" -> "full",
        "route_action" -> "redirect",
        "location" -> "/redirect/route"))
    response.status should be(201)

    var createdRoute = fromJson[Route](response.body)
    createdRoute.properties("location") should be("/redirect/route")

    when("we update that route")
    response = put("/routes/" + incomingPath,
      Map(
        "application_id" -> ApplicationForRedirectRoutes.application_id,
        "route_type" -> "full",
        "route_action" -> "redirect",
        "location" -> "/redirect/another-route"))
    response.status should be(200)

    createdRoute = fromJson[Route](response.body)
    createdRoute.properties("location") should be("/redirect/another-route")
  }

  test("Cannot create prefix routes with more than one path element") {
    val response = post("/routes/invalid/prefix/route", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    response.status should be(500)
  }

  test("Can create full routes with more than one path element") {
    val response = post("/routes/valid/full/route", Map("application_id" -> applicationId, "route_type" -> "full"))
    response.status should be(201)
  }

  test("can create a full route that overrides an existing prefix route") {
    val creationResponse = post("/routes/a-prefix-route", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    creationResponse.status should be(201)

    var tempResponse = get("/route/a-prefix-route/foo")
    tempResponse.status should be(200)
    tempResponse.body.contains("foo!") should be(true)

    val fullRouteResponse = post("/routes/a-prefix-route/foo/bar", Map("application_id" -> applicationId, "route_type" -> "full"))
    fullRouteResponse.status should be(201)
    val createdRoute = fromJson[Route](fullRouteResponse.body)
    createdRoute.incoming_path should be("a-prefix-route/foo/bar")

    tempResponse = get("/route/a-prefix-route/foo")
    tempResponse.status should be(200)
    tempResponse.body.contains("foo!") should be(true)
    tempResponse.body.contains("bar!") should be(false)

    tempResponse = get("/route/a-prefix-route/foo/bar")
    tempResponse.status should be(200)
    tempResponse.body.contains("foo!") should be(false)
    tempResponse.body.contains("bar!") should be(true)
  }

  test("Cannot create a full route that conflicts with an existing full route") {
    createRoute(incomingPath = "foo/bar", applicationId = applicationId, routeType = "full")

    val conflictedResponse = createRoute(incomingPath = "foo/bar", routeType = "full", applicationId = applicationId)
    conflictedResponse.status should be(409)
    val conflictedRoute = fromJson[Route](conflictedResponse.body)

    conflictedRoute.incoming_path should be("foo/bar")
  }

  test("A full route can 'punch a hole' in a prefix route"){
    val prefixAppId = createMainTestApplication
    val fullAppId = createMainTestApplication

    createRoute(routeType = "prefix", incomingPath = "punchhole", applicationId = prefixAppId)
    createRoute(routeType = "full", incomingPath = "punchhole/full/route", applicationId = fullAppId)

    var response = get("/route/punchhole/prefix/route")
    response.status should be(200)
    response.body.contains("prefix route") should be(true)
    response.body.contains("full route") should be(false)

    response = get("/route/punchhole/full/route")
    response.status should be(200)
    response.body.contains("prefix route") should be(false)
    response.body.contains("full route") should be(true)
  }

  test("Overlapping prefix routes should be possible and should map to the correct application") {
    val fooApplicationId = createMainTestApplication
    val footballApplicationId = createMainTestApplication

    createRoute(routeType = "prefix", incomingPath = "foo", applicationId = fooApplicationId)

    var response = createRoute(routeType = "full", incomingPath = "football", applicationId = footballApplicationId)
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
}

