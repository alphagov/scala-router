package uk.gov.gds.router.integration

import uk.gov.gds.router.MongoDatabaseBackedTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.GivenWhenThen

trait RouterIntegrationTestSetup extends MongoDatabaseBackedTest
  with ShouldMatchers
  with GivenWhenThen
  with HttpTestInterface {

  protected val apiRoot = "http://localhost:4000/router"
  protected val backendUrl = "localhost:4001/router-test-harness"
  protected var applicationId: String = ""

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

  protected def uniqueIdForTest = "integration-test-" + System.currentTimeMillis()

  override def buildUrl(path: String) = apiRoot + path

  protected def createRoute(applicationId: String, routePath: String, routeType: String) =
    post("/routes/" + routePath, Map("application_id" -> applicationId, "route_type" -> routeType))

  protected def createTestApplication(): String = {
    val applicationId = uniqueIdForTest
    createTestApplication(applicationId)
    applicationId
  }

  protected def createTestApplication(applicationId: String): String = {
    post("/applications/" + applicationId, Map("backend_url" -> backendUrl))
    post("/routes/fulltest/test.html", Map("application_id" -> applicationId, "route_type" -> "full"))
    post("/routes/prefixtest", Map("application_id" -> applicationId, "route_type" -> "prefix"))
    post("/routes/test", Map("application_id" -> applicationId, "route_type" -> "prefix"))

    applicationId
  }
}
