package uk.gov.gds.router.controller

import com.google.inject.Singleton
import uk.gov.gds.router.util.HttpProxy
import uk.gov.gds.router.repository.route.Routes

@Singleton
class RouteController() extends ControllerBase {

  get("/route/*") {
    Routes.load(requestInfo.pathParameter) match {
      case Some(route) => HttpProxy.get(route)
      case None => halt(404)
    }
  }

  post("/route/*") {
    Routes.load(requestInfo.pathParameter) match {
      case None => halt(404)
      case Some(route) => HttpProxy.post(route)
    }
  }
}
