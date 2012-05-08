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

  val allowedRouteUpdateParams = List("application_id", "incoming_path", "route_type", "route_action")
  val allowedApplicationUpdateParams = List("application_id", "backend_url")

  before() {
    response.setContentType("application/json")
  }

  post("/routes/*") {
    val incomingPath = requestInfo.pathParameter
    val applicationId = params("application_id")
    val routeType = params("route_type")

    val action = params.getOrElse("route_action", "proxy")
    val location = params.get("location")

    if (action == "redirect" && location.isEmpty) {
      logger.error("You must provide a location for a redirect route.")
      halt(500)
    } else if ((action == "proxy" || action == "gone")  && !location.isEmpty){
      logger.error("A location must not be provided if the route is not a redirect route.")
      halt(500)
    }

    def properties(location: Option[String]): Map[String,String] =
      location match {
        case None => Map.empty
        case Some(location) => Map("location" -> location)
      }

    onSameDatabaseServer {
      val persistenceStatus = Routes.store(
        Route(
          application_id = applicationId,
          route_type = routeType,
          incoming_path = incomingPath,
          route_action = action,
          properties = properties(location))
          )

      status(persistenceStatus)
      Routes.load(incomingPath)
    }
  }

  put("/routes/*") {
    checkRequestParametersContainOnly(allowedRouteUpdateParams)

    onSameDatabaseServer {
      val returnCode = Routes.simpleAtomicUpdate(requestInfo.pathParameter, requestInfo.requestParameters) match {
        case NotFound => Routes.store(Route(
          application_id = params("application_id"),
          route_type = params("route_type"),
          incoming_path = requestInfo.pathParameter
        ))
        case ps@_ => ps
      }

      status(returnCode)
      Routes.load(requestInfo.pathParameter)
    }
  }

  get("/routes/all") {
    Routes.all
  }

  get("/applications/all") {
    Applications.all
  }

  delete("/routes/*") {
    Routes.load(requestInfo.pathParameter) match {
      case Some(route) =>
        route.proxyType match {
          case FullRoute => Routes.deactivateFullRoute(route)
          case PrefixRoute => status(Routes.delete(route.incoming_path))
        }

      case None => status(NotFound)
    }
  }

  get("/routes/*") {
    Routes.load(requestInfo.pathParameter).getOrElse(halt(404))
  }

  post("/applications/*") {
    val newApplication = Application(requestInfo.pathParameter, params("backend_url"))
    status(Applications.store(newApplication))
    newApplication
  }

  put("/applications/*") {
    checkRequestParametersContainOnly(allowedApplicationUpdateParams)

    onSameDatabaseServer {
      val returnCode = Applications.simpleAtomicUpdate(requestInfo.pathParameter, requestInfo.requestParameters) match {
        case NotFound => Applications.store(Application(requestInfo.pathParameter, params("backend_url")))
        case ps@_ => ps
      }
      status(returnCode)
      Applications.load(requestInfo.pathParameter)
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