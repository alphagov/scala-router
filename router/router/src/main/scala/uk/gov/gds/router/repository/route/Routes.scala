package uk.gov.gds.router.repository.route

import uk.gov.gds.router.model._
import uk.gov.gds.router.repository._
import com.mongodb.casbah.Imports._

object Routes extends MongoRepository[Route]("routes", "route_id") {

  override def load(id: String) = super.load(id) match {
    case None =>
      val prefixPath = id.split("/").take(1).mkString("/")

      collection.findOne(MongoDBObject("route_id" -> prefixPath, "route_type" -> "prefix"))

    case Some(route) =>
      Some(route)
  }

  override def store(toStore: Route) = super.load(toStore.route_id) match {
    case Some(route) if (toStore.route_id == route.route_id) => Conflict
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

  def rename_incoming_path() = {

    //todo need to check they exist, or figure out why this query fails a second time:
    //    db.routes.find( { "incoming_path" : { $exists : true } } )
    //    collection.find( ("incoming_path", ( $exists , true ) )

    val a = collection.update(MongoDBObject(), $rename(("incoming_path","route_id")), false, true)
    logger.info("renamed incoming_path in database: " + a)
  }

  private[repository] def deactivateAllRoutesForApplication(id: String) {
    val routesForApp: List[Route] = collection.find(MongoDBObject("application_id" -> id)).toList //todo make implicit to convert Seq here to List

    routesForApp.foreach {
      route =>
        route.proxyType match {
          case FullRoute => deactivateFullRoute(route)
          case PrefixRoute => collection -= MongoDBObject("route_id" -> route.route_id)
        }
    }
  }
}