package uk.gov.gds.router.repository.route

import uk.gov.gds.router.model._
import uk.gov.gds.router.repository.MongoRepository
import com.mongodb.casbah.Imports._

object Routes extends MongoRepository[Route]("routes", "incoming_path") {

  addIndex(
    MongoDBObject("incoming_path" -> Ascending.order),
    Enforced.uniqueness,
    Complete.index)

  override def load(id: String) = loadFullRoute(id) match {
    case None => loadPrefixRoute(id)
    case Some(route) => Some(route)
  }

  private def loadFullRoute(id: String): Option[Route] = super.load(id)

  private def loadPrefixRoute(id: String): Option[Route] =
    collection.findOne(MongoDBObject("incoming_path" -> id.split("/").take(1).mkString("/")))

  private[repository] def deleteAllRoutesForApplication(applicationId: String) {
    collection -= MongoDBObject("application.application_id" -> applicationId)
  }
}