package uk.gov.gds.router.controller

import uk.gov.gds.router.util.Logging
import util.DynamicVariable
import org.scalatra.ScalatraFilter
import java.net.SocketTimeoutException

abstract class ControllerBase extends ScalatraFilter with Logging {

  private val threadRequestInfo = new DynamicVariable[RequestInfo](null)

  after() {
    threadRequestInfo.value_=(null)
  }

  error {
    case s: SocketTimeoutException => errorDocument(500)
    case e: Exception => errorDocument(500)
  }

  protected def halt(statusCode: Int) {
    super.halt(status = statusCode, body = errorDocument(statusCode), headers = Map("Content-Type" -> "text/html"))
  }

  def errorDocument(code: Int) = {
    logger.error("Serving error document with status {}", code)
    response.setHeader("Content-Type", "text/html")
    status(code)
    "Something went wrong."
  }

  protected implicit def requestInfo = {
    if (threadRequestInfo.value == null)
      threadRequestInfo.value_=(RequestInfo(request, params, multiParams))

    threadRequestInfo.value
  }

}
