package uk.gov.gds.router.controller

import org.scalatra.ScalatraFilter
import uk.gov.gds.router.util.Logging
import util.DynamicVariable

abstract class ControllerBase extends ScalatraFilter with Logging {

  private val thisThreadRequestInfo = new DynamicVariable[RequestInfo](null)

  after() {
    thisThreadRequestInfo.value_=(null)
  }

  protected implicit def requestInfo = {
    if (thisThreadRequestInfo.value == null)
      thisThreadRequestInfo.value_=(RequestInfo(request, params, multiParams))

    thisThreadRequestInfo.value
  }
}