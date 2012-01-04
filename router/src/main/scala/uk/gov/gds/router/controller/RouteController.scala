package uk.gov.gds.router.controller

import com.google.inject.Singleton
import uk.gov.gds.router.util.HttpProxy
import uk.gov.gds.router.repository.route.Routes

@Singleton
class RouteController() extends ControllerBase {
  get("/route/*") {
    try {
      Routes.load(requestInfo.pathParameter) match {
	case Some(route) => HttpProxy.get(route)
	case None => error(404)
      }
    } catch {
      case ste: java.net.SocketTimeoutException =>
        error(504)
      case e: Exception =>
        logger.error(e.toString())
        throw(e)
    }
  }

  post("/route/*") {
    try {
      Routes.load(requestInfo.pathParameter) match {
	case Some(route) => HttpProxy.post(route)
	case None => error(404)
      }
    } catch {
      case _: java.net.SocketTimeoutException =>
        error(504)
      case e: Exception =>
        logger.error(e.toString())
        throw(e)
    }
  }
}
