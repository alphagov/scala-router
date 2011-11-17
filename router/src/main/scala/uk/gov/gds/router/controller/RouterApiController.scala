package uk.gov.gds.router.controller

import com.google.inject.Singleton
import uk.gov.gds.router.mongodb.MongoDatabase._
import uk.gov.gds.router.repository.route.Routes
import uk.gov.gds.router.repository.application.Applications
import uk.gov.gds.router.repository.PersistenceStatus
import uk.gov.gds.router.model.{Route, Application}
import uk.gov.gds.router.util.JsonSerializer
import runtime.BoxedUnit

@Singleton
class RouterApiController() extends ControllerBase {

  private implicit def persistenceStatus2httpStatus(ps: PersistenceStatus) = ps.statusCode

  val allowedRouteUpdateParams = List("application.application_id", "incoming_path", "route_type")
  val allowedApplicationUpdateParams = List("application_id", "backend_url")

  before() {
    response.setContentType("application/json")
  }

  post("/routes/*") {
    val incomingPath = requestInfo.pathParameter
    val applicationId = params("application_id")
    val routeType = params("route_type")

    onSameDatabaseServer {
      val persistenceStatus = Routes.store(
        Route(
          applicationId = applicationId,
          routeType = routeType,
          incomingPath = incomingPath))

      status(persistenceStatus)
      Routes.load(incomingPath)
    }
  }

  put("/routes/*") {
    checkRequestParametersContainOnly(allowedRouteUpdateParams)

    onSameDatabaseServer {
      status(Routes.simpleAtomicUpdate(requestInfo.pathParameter, requestInfo.requestParameters))
      Routes.load(requestInfo.pathParameter)
    }
  }

  delete("/routes/*") {
    status(Routes.delete(requestInfo.pathParameter).statusCode)
  }

  get("/route-for/*") {
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
      status(Applications.simpleAtomicUpdate(requestInfo.pathParameter, requestInfo.requestParameters))
      Applications.load(requestInfo.pathParameter)
    }
  }

  delete("/applications/:id") {
    status(Applications.delete(params("id")))
  }

  get("/applications/:id") {
    Applications.load(params("id")) getOrElse status(404)
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