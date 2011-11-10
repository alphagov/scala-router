package uk.gov.gds.router.repository.application

import uk.gov.gds.router.repository.MongoRepository
import uk.gov.gds.router.repository.route.Routes
import com.mongodb.casbah.Imports._
import uk.gov.gds.router.model._

object Applications extends MongoRepository[Application]("applications") {

  addIndex(
    MongoDBObject("application_id" -> Ascending.order),
    Enforced.uniqueness,
    Complete.index)

  def store(application: Application) = load(application.application_id) match {
    case Some(application) =>
      Conflict
    case None =>
      collection += application
      NewlyCreated
  }

  def load(id: String): Option[Application] = collection.findOne(MongoDBObject("application_id" -> id))

  def delete(applicationId: String) = load(applicationId) match {
    case None =>
      NotFound
    case Some(application) =>
      collection -= MongoDBObject("application_id" -> applicationId)
      Routes.deleteAllRoutesForApplication(applicationId)
      Deleted
  }

  def simpleAtomicUpdate(id: String, params: Map[String, Any]) = {
    val updateResult = collection.findAndModify(
      query = MongoDBObject("application_id" -> id),
      update = atomicUpdate(params))

    updateResult match {
      case Some(_) => Updated
      case None => NotFound
    }
  }


}