package uk.gov.gds.router.controller

import com.google.inject.Singleton
import uk.gov.gds.router.util.HttpProxy
import uk.gov.gds.router.repository.route.Routes

@Singleton
class RouteController() extends ControllerBase {

  get("/route/*") {
    val requestInfo = getRequestInfo

    Routes.load(requestInfo.pathParameter) match {
      case Some(route) => HttpProxy.proxyGet(route, requestInfo, response)
      case None => halt(404)
    }
  }

  post("/route/*") {
    val requestInfo = getRequestInfo

    Routes.load(requestInfo.pathParameter) match {
      case Some(route) => HttpProxy.proxyPost(route, requestInfo, response)
      case None => halt(404)
    }
  }
}