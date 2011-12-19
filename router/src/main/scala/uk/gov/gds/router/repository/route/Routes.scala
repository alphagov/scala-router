package uk.gov.gds.router.repository.route

import uk.gov.gds.router.model._
import com.mongodb.casbah.Imports._
import uk.gov.gds.router.repository.{Conflict, NewlyCreated, MongoRepository}

object Routes extends MongoRepository[Route]("routes", "incoming_path") {

  override def load(id: String) = super.load(id) match {
    case None =>
      val prefixPath = id.split("/").take(1).mkString("/")
      collection.findOne(MongoDBObject("incoming_path" -> prefixPath, "route_type" -> "prefix"))
    case Some(route) => Some(route)
  }


  override def store(obj: Route) = super.load(obj.id) match {
    case Some(route) if (obj.route_type == "prefix") => Conflict
    case Some(route) if (obj.incoming_path == route.incoming_path) => Conflict
    case None =>
      collection += obj
      NewlyCreated
  }

  private[repository] def deleteAllRoutesForApplication(id: String) {
    collection -= MongoDBObject("application_id" -> id)
  }
}