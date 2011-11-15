package uk.gov.gds.router.repository.application

import uk.gov.gds.router.repository.MongoRepository
import uk.gov.gds.router.repository.route.Routes
import uk.gov.gds.router.model._

object Applications extends MongoRepository[Application]("applications", "application_id") {

  override def delete(applicationId: String) = {
    val result = super.delete(applicationId)
    Routes.deleteAllRoutesForApplication(applicationId)
    result
  }
}