package uk.gov.gds.router.repository.application

import uk.gov.gds.router.repository.MongoRepository
import uk.gov.gds.router.repository.route.Routes
import com.mongodb.casbah.Imports._
import uk.gov.gds.router.model._

object Applications extends MongoRepository[Application]("applications", "application_id") {

  addIndex(
    MongoDBObject("application_id" -> Ascending.order),
    Enforced.uniqueness,
    Complete.index)

  override def delete(applicationId: String) = {
    val result = super.delete(applicationId)
    Routes.deleteAllRoutesForApplication(applicationId)
    result
  }
}