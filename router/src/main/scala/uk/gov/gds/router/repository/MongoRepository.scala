package uk.gov.gds.router.repository

import uk.gov.gds.router.mongodb.MongoDatabase._
import com.mongodb.DBObject
import com.novus.salat._
import com.novus.salat.global.NoTypeHints
import com.mongodb.casbah.Imports._
import uk.gov.gds.router.model._

abstract class MongoRepository[A <: CaseClass with HasIdentity](collectionName: String, idProperty: String)(implicit m: Manifest[A])
  extends Repository[A] with MongoIndexTypes {

  protected val collection = getCollection(collectionName)
  private implicit val ctx = NoTypeHints

  protected implicit def domainObj2mongoObj(o: A) = grater[A].asDBObject(o)

  protected implicit def mongoObj2DomainObj(o: Option[collection.T]) = o map (grater[A].asObject(_))

  protected def addIndex(index: DBObject, unique: Boolean, sparse: Boolean) =
    collection.underlying.ensureIndex(index, MongoDBObject(
      "unique" -> unique,
      "background" -> true,
      "sparse" -> sparse))

  override def store(obj: A) = load(obj.id) match {
    case Some(_) => Conflict
    case None =>
      collection += obj
      NewlyCreated
  }

  override def load(id: String) = collection.findOne(MongoDBObject(idProperty -> id))

  override def delete(id: String) = load(id) match {
    case Some(route) =>
      collection -= MongoDBObject(idProperty -> id)
      Deleted

    case None => NotFound
  }

  override def simpleAtomicUpdate(id: String, params: Map[String, Any]) = {
    val builder = MongoDBObject.newBuilder
    for ((k, v) <- params) builder += k -> v

    val updateResult = collection.findAndModify(
      query = MongoDBObject(idProperty -> id),
      update = MongoDBObject("$set" -> builder.result.asDBObject))

    updateResult match {
      case Some(_) => Updated
      case None => NotFound
    }
  }
}