package uk.gov.gds.router.repository.route

import uk.gov.gds.router.model._
import uk.gov.gds.router.repository._
import com.mongodb.casbah.Imports._
import uk.gov.gds.router.util.Logging
import uk.gov.gds.router.util.IncomingPath

object Routes extends MongoRepository[Route]("routes", "incoming_path") with Logging {

  override def load(incoming_path: String) = super.load(incoming_path) match {
    case Some(route) =>
      collection.findOne(route)

    case None =>
      val host = IncomingPath.host(incoming_path);
      val prefix = IncomingPath.prefix(incoming_path);

      if (host.isEmpty) {
        collection.findOne(MongoDBObject("incoming_path" -> prefix, "route_type" -> "prefix"))
      } else {
        val hosted_prefix = "host/".concat(host).concat("/").concat(prefix)
        collection.findOne(MongoDBObject("incoming_path" -> hosted_prefix, "route_type" -> "prefix")) match {
          case Some(host_route) =>
            collection.findOne(host_route)
          case None => 
            collection.findOne(MongoDBObject("incoming_path" -> prefix, "route_type" -> "prefix"))
        }
      }
  }

  override def store(toStore: Route) = super.load(toStore.incoming_path) match {
    case Some(route) if (toStore.incoming_path == route.incoming_path) => Conflict
    case None =>
      collection += toStore
      NewlyCreated
  }

  def deactivateFullRoute(route: Route) = {
    Routes.simpleAtomicUpdate(route.id, Map(
      "application_id" -> ApplicationForGoneRoutes.application_id,
      "route_action" -> "gone")
    )

    route.copy(
      application_id = ApplicationForGoneRoutes.application_id,
      route_action = "gone"
    )
  }

  private[repository] def deactivateAllRoutesForApplication(id: String) {
    val routesForApp: List[Route] = collection.find(MongoDBObject("application_id" -> id)).toList //todo make implicit to convert Seq here to List

    routesForApp.foreach {
      route =>
        route.proxyType match {
          case FullRoute => deactivateFullRoute(route)
          case PrefixRoute => collection -= MongoDBObject("incoming_path" -> route.incoming_path)
        }
    }
  }
}