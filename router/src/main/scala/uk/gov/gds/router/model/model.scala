package uk.gov.gds.router.model

import uk.gov.gds.router.repository.application.Applications

case class Application(application_id: String,
                       backend_url: String)

case class Route(application: Application,
                 incoming_path: String,
                 route_type: String) {
  def proxyType = route_type match {
    case "full" => FullRoute
    case "prefix" => PrefixRoute
    case _ => throw new Exception("Unknown route type")
  }
}

object Route {
  def apply(applicationId: String, routeType: String, incomingPath: String): Route = {
    if ("prefix" == routeType && 1 != incomingPath.split("/").length)
      throw new RuntimeException("Invalid route: prefix routes may only have one segment")

    Route(
      application = Applications.load(applicationId).getOrElse(throw new Exception("Can't find application for route")),
      incoming_path = incomingPath,
      route_type = routeType)
  }
}

sealed abstract class RouteType
case object FullRoute extends RouteType
case object PrefixRoute extends RouteType

sealed abstract class PersistenceStatus(httpStatusCode: Int) {
  def statusCode = httpStatusCode
}

case object NewlyCreated extends PersistenceStatus(201)
case object Conflict extends PersistenceStatus(409)
case object Deleted extends PersistenceStatus(204)
case object NotFound extends PersistenceStatus(404)
case object Updated extends PersistenceStatus(200)


