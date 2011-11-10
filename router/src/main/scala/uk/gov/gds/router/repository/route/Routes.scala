package uk.gov.gds.router.repository.route

import uk.gov.gds.router.model._
import uk.gov.gds.router.repository.MongoRepository
import com.mongodb.casbah.Imports._

object Routes extends MongoRepository[Route]("routes") {

  addIndex(
    MongoDBObject("incoming_path" -> Ascending.order),
    Enforced.uniqueness,
    Complete.index)

  def store(routeToStore: Route) = load(routeToStore.incoming_path) match {
    case Some(route) => Conflict
    case None =>
      collection += routeToStore
      NewlyCreated
  }

  def load(id: String) = loadFullRoute(id) match {
    case None => loadPrefixRoute(id)
    case Some(route) => Some(route)
  }

  def simpleAtomicUpdate(id: String, params: Map[String, Any]) = {
    val updateResult = collection.findAndModify(
      query = MongoDBObject("incoming_path" -> id),
      update = atomicUpdate(params))

    updateResult match {
      case Some(_) => Updated
      case None => NotFound
    }
  }

  def delete(incomingPath: String) = load(incomingPath) match {
    case Some(route) =>
      collection -= MongoDBObject("incoming_path" -> incomingPath)
      Deleted

    case None => NotFound
  }

  private def loadFullRoute(id: String): Option[Route] = collection.findOne(MongoDBObject("incoming_path" -> id))

  private def loadPrefixRoute(id: String): Option[Route] = collection.findOne(MongoDBObject("incoming_path" -> id.split("/").take(1).mkString("/")))

  private[repository] def deleteAllRoutesForApplication(applicationId: String) {
    collection -= MongoDBObject("application.application_id" -> applicationId)
  }
}