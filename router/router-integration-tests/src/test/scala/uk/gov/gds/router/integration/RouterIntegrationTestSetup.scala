package uk.gov.gds.router.integration

import uk.gov.gds.router.MongoDatabaseBackedTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.GivenWhenThen

trait RouterIntegrationTestSetup
  extends MongoDatabaseBackedTest
  with ShouldMatchers
  with GivenWhenThen
  with HttpTestInterface {

  protected val apiRoot = "http://localhost:4000/router"
  
  protected val mainHostBackendUrl = "localhost:4001/router-test-harness-main-host"
  protected var applicationId: String = ""

  protected val alsoSupportedHostBackendUrl = "localhost:4002/router-test-harness-also-supported-host"
  protected var alsoSupportedApplicationId: String = ""

  override protected def beforeEach() {
    super.beforeEach()
    RouterTestHarnessContainer.start()
    RouterTestHarnessAlsoSupportedContainer.start()
    RouterContainer.start()
    applicationId = createMainTestApplication()
    alsoSupportedApplicationId = createAlsoSupportedTestApplication()

    cookieStore.clear()
  }

  override protected def afterEach() {
    super.afterEach()
    RouterTestHarnessContainer.stopUnlessSomeoneCallsStartAgainSoon()
    RouterTestHarnessAlsoSupportedContainer.stopUnlessSomeoneCallsStartAgainSoon()
    RouterContainer.stopUnlessSomeoneCallsStartAgainSoon()
  }

  protected def uniqueIdForTest = "integration-test-" + System.currentTimeMillis()

  override def buildUrl(path: String) = apiRoot + path

  protected def createRoute(applicationId: String, incomingPath: String, routeType: String) =
    post("/routes/" + incomingPath, Map("application_id" -> applicationId, "route_type" -> routeType))

  protected def createMainTestApplication(): String = {
    val applicationId = uniqueIdForTest
    createMainTestApplication(applicationId)
    applicationId
  }

  protected def createMainTestApplication(applicationId: String): String = {
    post("/applications/" + applicationId, Map("backend_url" -> mainHostBackendUrl))
    post("/routes/fulltest/test.html", Map("application_id" -> applicationId, "route_type" -> "full"))
    post("/routes/prefixtest", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    post("/routes/test", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    post("/routes/test", Map("application_id" -> applicationId, "route_type" -> "prefix"))

    // route with a hostname, optionally supplied by nginx/varnish/etc
    post("/routes/host/mainTest/fulltest/test.html", Map("application_id" -> applicationId, "route_type" -> "full"))

    applicationId
  }

  protected def createAlsoSupportedTestApplication(): String = {
    val alsoSupportedApplicationId = uniqueIdForTest

    post("/applications/" + alsoSupportedApplicationId , Map("backend_url" -> alsoSupportedHostBackendUrl))

    // route with a hostname, optionally supplied by nginx/varnish/etc
    post("/routes/host/alsoSupported/fulltest/test.html", Map("application_id" -> alsoSupportedApplicationId, "route_type" -> "full"))

    alsoSupportedApplicationId
  }
}
