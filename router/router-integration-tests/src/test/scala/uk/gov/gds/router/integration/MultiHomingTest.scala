package uk.gov.gds.router.integration

class MultiHomingTest extends RouterIntegrationTestSetup {

  test("hostname routes to different application"){
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


  test("different hosts can have the same prefix route"){

  }

}
