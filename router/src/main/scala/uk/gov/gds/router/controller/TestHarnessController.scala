package uk.gov.gds.router.controller

import org.scalatra.ScalatraFilter
import uk.gov.gds.router.util.Logging
import com.google.inject.Singleton

@Singleton
class TestHarnessController extends ScalatraFilter with Logging {

  // TODO Should not be part of the main application

  private def output(block: => String) = {
    val output =
      <html>
        <head>
          <title>Test harness</title>
        </head>
        <body>
          {block}
        </body>
      </html>

    logger.info("Response: {}", output)
    output
  }

  private def dumpParams = multiParams.map {
    case (paramName, values) => values.map(paramName + "=" + _)
  }.flatten.mkString("\n")
  
  get("/test/test-harness") {
    output(dumpParams)
  }

  get("/test/test-harness/") {
    output(dumpParams)
  }

  post("/test/test-harness") {
    output(dumpParams)
  }

  get("/foo") {
    output("fooOnly")
  }

  get("/foo/*") {
    output("fooOnly")
  }

  get("/football") {
    output("football")
  }
}