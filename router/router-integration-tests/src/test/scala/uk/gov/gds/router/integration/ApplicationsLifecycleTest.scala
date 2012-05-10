package uk.gov.gds.router.integration

import uk.gov.gds.router.util.JsonSerializer._
import uk.gov.gds.router.model._

class ApplicationsLifecycleTest
  extends RouterIntegrationTestSetup {

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

    response = get("/applications/" + applicationId)
    response.status should be(404)
  }

  test("Default application is created to handle 410 gone routes"){
    val response = get("/applications/router-gone")
    logger.info("app " + response.body)
    val application = fromJson[Application](response.body)

    application.application_id should be(ApplicationForGoneRoutes.application_id)
  }

  test("When an application is deleted full routes continue to exist but are 'Gone' and prefix routes do not exist") {

    given("The test application has been created before the test")
    get("/applications/" + applicationId).status should be(200)

    when("The application is deleted")
    var response = delete("/applications/" + applicationId)
    logger.info(response.body)
    response.status should be(204)

    then("The application no longer exists")
    get("/applications/" + applicationId).status should be(404)

    then("The full route returns a 410 Gone")
    response = get("/route/fulltest/test.html")
    response.status should be(410)

    then("The prefix routes return 404")
    response = get("/route/prefixtest")
    response.status should be(404)

    response = get("/route/test")
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

}
