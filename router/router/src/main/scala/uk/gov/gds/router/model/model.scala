package uk.gov.gds.router.model

import uk.gov.gds.router.repository.application.Applications

trait HasIdentity {
  def id: String
}

case class Application(application_id: String,
                       backend_url: String) extends HasIdentity {
  def id = application_id
}

case class Route(route_id: String,
                 application_id: String,
                 route_type: String,
                 route_action: String = "proxy",
                 properties: Map[String,String] = Map.empty) extends HasIdentity {

  val application = Applications.load(application_id).getOrElse(throw new Exception("Can't find application for route " + this))

//  val routePath = "/" + route_id.split("/").drop(1).mkString("/")

  if ("prefix" == route_type && route_id.split("/").drop(2).length > 0)
    throw new RuntimeException("Invalid route: prefix routes may only have two segments, e.g. /host/prefix")

  def proxyType = route_type match {
    case "full" => FullRoute
    case "prefix" => PrefixRoute
    case _ => throw new Exception("Unknown route type")
  }

  def routeAction = route_action match {
    case "proxy" => Proxy
    case "gone" => Gone
    case "redirect" => Redirect
    case _ => throw new Exception("Unknown proxy type " + route_action)
  }

  def id = route_id
}

object ApplicationForGoneRoutes extends Application("router-gone", "todo:remove this")

object ApplicationForRedirectRoutes extends Application("router-redirect", "todo:remove this")

sealed abstract class RouteType

case object FullRoute extends RouteType

case object PrefixRoute extends RouteType

sealed abstract class RouteAction

case object Proxy extends RouteAction

case object Gone extends RouteAction

case object Redirect extends RouteAction


