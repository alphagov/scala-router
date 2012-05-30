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
    case s: SocketTimeoutException =>
      logger.error("Timeout", s)
      errorDocument(503)
    case e: Exception =>
      logger.error("Exception", e)
      errorDocument(500)
  }

  protected def halt(statusCode: Int) {
    super.halt(status = statusCode, body = errorDocument(statusCode), headers = Map("Content-Type" -> "text/html"))
  }

  protected def redirect_permanently(location: String) {
    super.halt(status = 301, body = errorDocument(301), headers = Map("Content-Type" -> "text/html", "Location" -> location))
  }


  def errorDocument(code: Int) = {
    logger.debug("Serving error document with status {}", code)
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
