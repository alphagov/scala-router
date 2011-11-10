package uk.gov.gds.router.controller

import org.scalatra.ScalatraFilter
import uk.gov.gds.router.util.Logging
import com.google.inject.Singleton

@Singleton
class TestHarnessController extends ScalatraFilter with Logging {

  // TODO Should not be part of the main application

  private def output(first: String, second: String) = {
    val output =
      <html>
        <head>
          <title>Test harness</title>
        </head>
        <body>
          first={first}
          second={second}
        </body>
      </html>

    logger.info("Response: {}", output)
    output
  }

  private def fooApplication = output("fooOnly", "fooOnly")

  get("/test/test-harness") {
    output(params("first"), params("second"))
  }

  get("/test/test-harness/") {
    output(params("first"), params("second"))
  }

  post("/test/test-harness") {
    output(params("first"), params("second"))
  }

  get("/foo") {
    fooApplication
  }

  get("/foo/*") {
    fooApplication
  }

  get("/football") {
    output("football", "football")
  }
}