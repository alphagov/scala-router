package uk.gov.gds.router.repository

trait Repository[A] {

  def store(obj: A): PersistenceStatus

  def load(id: String): Option[A]

  def delete(id: String): PersistenceStatus

  def simpleAtomicUpdate(id: String, params: Map[String, Any]): PersistenceStatus

  def all: Seq[A]
}