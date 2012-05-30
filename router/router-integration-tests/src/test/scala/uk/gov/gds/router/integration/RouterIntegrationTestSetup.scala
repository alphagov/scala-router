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
  protected val backendUrl = "localhost:4001/router-test-harness-main-host"
  protected var applicationId: String = ""

  override protected def beforeEach() {
    super.beforeEach()
    RouterTestHarnessContainer.start()
    RouterContainer.start()
    applicationId = createMainTestApplication()
    cookieStore.clear()
  }

  override protected def afterEach() {
    super.afterEach()
    RouterTestHarnessContainer.stopUnlessSomeoneCallsStartAgainSoon()
    RouterContainer.stopUnlessSomeoneCallsStartAgainSoon()
  }

  protected def uniqueIdForTest = "integration-test-" + System.currentTimeMillis()

  override def buildUrl(path: String) = apiRoot + path

  protected def createRoute(applicationId: String, routeId: String, routeType: String) =
    post("/routes/" + routeId, Map("application_id" -> applicationId, "route_type" -> routeType))

  protected def createMainTestApplication(): String = {
    val applicationId = uniqueIdForTest
    createMainTestApplication(applicationId)
    applicationId
  }

  protected def createMainTestApplication(applicationId: String): String = {
    post("/applications/" + applicationId, Map("backend_url" -> backendUrl))
    post("/routes/mainhost/fulltest/test.html", Map("application_id" -> applicationId, "route_type" -> "full"))
    post("/routes/mainhost/prefixtest", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    post("/routes/mainhost/test", Map("application_id" -> applicationId, "route_type" -> "prefix"))

    applicationId
  }
}
