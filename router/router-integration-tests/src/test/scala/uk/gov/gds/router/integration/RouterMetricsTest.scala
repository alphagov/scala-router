package uk.gov.gds.router.integration

import uk.gov.gds.router.util.JsonSerializer
import org.apache.http.client.methods.HttpGet

class RouterMetricsTest
  extends RouterIntegrationTestSetup {

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

}
