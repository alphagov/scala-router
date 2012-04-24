package uk.gov.gds.router.model

import uk.gov.gds.router.repository.application.Applications

trait HasIdentity {
  def id: String
}

case class Application(application_id: String,
                       backend_url: String) extends HasIdentity {
  def id = application_id
}

case class Route(application_id: String,
                 incoming_path: String,
                 route_type: String,
                 route_action: String = "proxy") extends HasIdentity {

  val application = Applications.load(application_id).getOrElse(throw new Exception("Can't find application for route"))

  if ("prefix" == route_type && 1 != incoming_path.split("/").length)
    throw new RuntimeException("Invalid route: prefix routes may only have one segment")

  def proxyType = route_type match {
    case "full" => FullRoute
    case "prefix" => PrefixRoute
    case _ => throw new Exception("Unknown route type")
  }

  def id = incoming_path
}

sealed abstract class RouteType

case object FullRoute extends RouteType

case object PrefixRoute extends RouteType




