package uk.gov.gds.router.controller

import uk.gov.gds.router.util.Logging
import util.DynamicVariable
import org.scalatra.ScalatraFilter

abstract class ControllerBase extends ScalatraFilter with Logging {

  private val threadRequestInfo = new DynamicVariable[RequestInfo](null)

  after() {
    threadRequestInfo.value_=(null)
  }

  protected implicit def requestInfo = {
    if (threadRequestInfo.value == null)
      threadRequestInfo.value_=(RequestInfo(request, params, multiParams))

    threadRequestInfo.value
  }

}