package uk.gov.gds.router.repository

sealed abstract class PersistenceStatus(httpStatusCode: Int) {
  def statusCode = httpStatusCode
}

case object NewlyCreated extends PersistenceStatus(201)

case object Conflict extends PersistenceStatus(409)

case object Deleted extends PersistenceStatus(204)

case object NotFound extends PersistenceStatus(404)

case object Updated extends PersistenceStatus(200)
