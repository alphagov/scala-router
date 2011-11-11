package uk.gov.gds.router.controller

import org.scalatra.ScalatraFilter
import runtime.BoxedUnit
import uk.gov.gds.router.util.{JsonSerializer, Logging}

abstract class ControllerBase extends ScalatraFilter with Logging {

  override def renderResponseBody(actionResult: Any) {
    actionResult match {
      case _: BoxedUnit =>
      case None => status(404)
      case result: AnyRef => {
        response.setContentType("application/json")
        response.getWriter.print(JsonSerializer.toJson(result))
      }
    }
  }

  protected implicit def requestInfo = RequestInfo(request, params, multiParams)
}