package uk.gov.gds.router.controller

import com.google.inject.Singleton
import uk.gov.gds.router.repository.route.Routes
import uk.gov.gds.router.util.HttpProxy

@Singleton
class RouteController() extends ControllerBase {

  get("/route/*") {

    val incomingPath = requestInfo.pathParameter
    logger.info("incoming path: " + incomingPath)

    Routes.load(incomingPath) match {
      case Some(route) if ("proxy".equals(route.route_action)) => HttpProxy.get(route)
      case Some(route) if ("gone".equals(route.route_action)) => halt(410)
      case Some(route) if ("redirect".equals(route.route_action)) => redirect_permanently(route.properties("location"))
      case None => halt(404)
    }
  }

  post("/route/*") {

    val incomingPath = requestInfo.pathParameter

    Routes.load(incomingPath) match {
      case Some(route) => HttpProxy.post(route)
      case None => halt(404)
    }
  }
}
