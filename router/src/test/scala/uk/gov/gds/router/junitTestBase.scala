package uk.gov.gds.router

import gov.uk.gds.router.ApplicationsUnderTest
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.casbah.commons.Logging
import org.junit.{After, Before}

@RunWith(classOf[BlockJUnit4ClassRunner])
abstract class BasicJunitTestCase extends ShouldMatchers with Logging {
  protected def uniqueIdForTest = "integration-test-" + System.currentTimeMillis()
}

abstract class TestThatRequiresRunningRouter extends BasicJunitTestCase with HttpTestInterface {

  private val apiRoot = "http://localhost:8080/router"

  @Before
  def setUp() {
    ApplicationsUnderTest.start()
  }

  @After
  def tearDown() {
    ApplicationsUnderTest.stopUnlessSomeoneCallsStartAgainSoon()
    CookieStore.requestCookies.clear()
  }

  override def buildUrl(path: String) = apiRoot + path
}