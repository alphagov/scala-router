package uk.gov.gds.router.controller

import com.google.inject.Singleton
import uk.gov.gds.router.mongodb.MongoDatabase._
import uk.gov.gds.router.repository.route.Routes
import uk.gov.gds.router.repository.application.Applications
import uk.gov.gds.router.util.JsonSerializer

import runtime.BoxedUnit
import uk.gov.gds.router.repository.{Updated, NotFound, PersistenceStatus}
import uk.gov.gds.router.mongodb.MongoDatabase
import uk.gov.gds.router.model._


@Singleton
class RouterApiController() extends ControllerBase {

  private implicit def persistenceStatus2httpStatus(ps: PersistenceStatus) = ps.statusCode

  val allowedRouteUpdateParams = List("application_id", "incoming_path", "route_type", "route_action", "location")
  val allowedApplicationUpdateParams = List("application_id", "backend_url")

  before() {
    response.setContentType("application/json")
  }

  post("/routes/*") {
    val incomingPath = requestInfo.pathParameter
    val routeWithValidatedParameters = validateParametersForRoute()

    onSameDatabaseServer {
      val persistenceStatus = Routes.store(routeWithValidatedParameters)

      status(persistenceStatus)
      Routes.load(incomingPath)
    }
  }

 put("/routes/*") {
    checkRequestParametersContainOnly(allowedRouteUpdateParams)

   val incomingPath = requestInfo.pathParameter
   val routeWithValidatedParameters : Route = validateParametersForRoute()

   onSameDatabaseServer {
    val mapOfRouteParameters = Map[String, Any]("incoming_path" -> routeWithValidatedParameters.incoming_path,
           "route_type" -> routeWithValidatedParameters.route_type,
           "route_action" -> routeWithValidatedParameters.route_action,
           "application_id" -> routeWithValidatedParameters.application_id,
//           "host" -> routeWithValidatedParameters.host,
           "properties" -> routeWithValidatedParameters.properties)
     val returnCode = Routes.simpleAtomicUpdate(incomingPath, mapOfRouteParameters) match {
        case NotFound =>
          Routes.store(routeWithValidatedParameters)
        case ps@_ => ps
      }

      status(returnCode)
      Routes.load(incomingPath)
    }
  }

  get("/routes/all") {
    Routes.all
  }

  get("/applications/all") {
    Applications.all
  }

  delete("/routes/*") {
    val incomingPath = requestInfo.pathParameter
    Routes.load(incomingPath) match {
      case Some(route) =>
        route.proxyType match {
          case FullRoute => Routes.deactivateFullRoute(route)
          case PrefixRoute => status(Routes.delete(route.incoming_path))
        }

      case None => status(NotFound)
    }
  }

  get("/routes/*") {
    val incomingPath = requestInfo.pathParameter
    Routes.load(incomingPath).getOrElse(halt(404))
  }

  post("/applications/*") {
    val incomingPath = requestInfo.pathParameter
    val newApplication = Application(incomingPath, params("backend_url"))
    status(Applications.store(newApplication))
    newApplication
  }

  put("/applications/*") {
    checkRequestParametersContainOnly(allowedApplicationUpdateParams)

    onSameDatabaseServer {
      val incomingPath = requestInfo.pathParameter
      val returnCode = Applications.simpleAtomicUpdate(incomingPath, requestInfo.requestParameters) match {
        case NotFound => Applications.store(Application(incomingPath, params("backend_url")))
        case ps@_ => ps
      }
      status(returnCode)
      Applications.load(incomingPath)
    }
  }

  delete("/applications/:id") {
    status(Applications.delete(params("id")))
  }

  get("/applications/:id") {
    Applications.load(params("id")) getOrElse status(404)
  }

  get("/reinitialise") {
    MongoDatabase.initialiseMongo()
  }

  private def validateParametersForRoute() : Route = {

    //val requestedPath = requestInfo.pathParameter
    //val host = requestedPath.split("/").take(2).mkString("/")
    //val incomingPath = requestedPath.substring(host.length(), requestedPath.length());

    val incomingPath = requestInfo.pathParameter

    val routeType = params("route_type")

    val action = params.getOrElse("route_action", "proxy")

    val applicationId = action match {
      case "proxy" => params("application_id")
      case "redirect" => ApplicationForRedirectRoutes.id
      case "gone" => ApplicationForGoneRoutes.id
    }

    val location = params.get("location")

    location match {
      case Some(str) if (action == "redirect" && str.equals("")) =>
        halt(500, "You must provide a location for a redirect route.")
      case Some(_) if (action == "proxy" || action == "gone") =>
        halt(500, "A location must not be provided if the route is not a redirect route.")
      case None if (action == "redirect") =>
        halt(500, "You must provide a location for a redirect route.")
      case _ =>
    }

    def properties(location: Option[String]): Map[String, String] =
      location match {
        case None => Map.empty
        case Some(location) => Map("location" -> location)
      }

    val route = Route(incomingPath, applicationId, routeType, action, properties(location))
    route
  }

  private def checkRequestParametersContainOnly(validParams: List[String]) = {
    requestInfo.requestParameters map {
      case (key, _) if (!validParams.contains(key)) => throw new InvalidParameterException(key, validParams)
      case _ =>
    }
  }

  override def renderResponseBody(actionResult: Any) {
    actionResult match {
      case _: BoxedUnit =>
      case None => status(404)
      case result: AnyRef => response.getWriter.print(JsonSerializer.toJson(result))
    }
  }

  class InvalidParameterException(invalidParam: String, validParams: List[String])
    extends RuntimeException("Parameter " + invalidParam + " is invalid for this operation. Valid keys are: " + validParams.mkString(" "))

}