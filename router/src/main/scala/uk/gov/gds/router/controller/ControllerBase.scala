package uk.gov.gds.router.controller

import uk.gov.gds.router.util.Logging
import util.DynamicVariable
import org.scalatra.ScalatraFilter

abstract class ControllerBase extends ScalatraFilter with Logging {

  private val threadRequestInfo = new DynamicVariable[RequestInfo](null)

  after() {
    threadRequestInfo.value_=(null)
  }

  def error(code : Int) = {
    response.setHeader("Content-Type", "text/html")
    halt(code, errorDocument(code))
  }

  def errorDocument(code : Int) = {
    val errorFile = "/" + code.toString().substring(0, 1) + "00.html"
    val is = getClass().getResourceAsStream(errorFile)
    scala.io.Source.fromInputStream(is).mkString("")
  }

  protected implicit def requestInfo = {
    if (threadRequestInfo.value == null)
      threadRequestInfo.value_=(RequestInfo(request, params, multiParams))

    threadRequestInfo.value
  }

}
