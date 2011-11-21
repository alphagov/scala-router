package uk.gov.gds.router.repository.route

import uk.gov.gds.router.model._
import uk.gov.gds.router.repository.MongoRepository
import com.mongodb.casbah.Imports._

object Routes extends MongoRepository[Route]("routes", "incoming_path") {

  override def load(id: String) = super.load(id) match {
    case None => collection.findOne(MongoDBObject("incoming_path" -> id.split("/").take(1).mkString("/")))
    case Some(route) => Some(route)
  }

  private[repository] def deleteAllRoutesForApplication(id: String) {
    collection -= MongoDBObject("application.application_id" -> id)
  }
}