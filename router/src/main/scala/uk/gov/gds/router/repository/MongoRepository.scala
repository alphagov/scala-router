package uk.gov.gds.router.repository

import uk.gov.gds.router.mongodb.MongoDatabase._
import com.mongodb.DBObject
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._

abstract class MongoRepository[A <: CaseClass](collectionName: String) extends Repository[A] {

  protected val collection = database(collectionName)
  collection.slaveOk()

  protected implicit def domainObject2mongoObject(o: A)(implicit m: Manifest[A]) = grater[A].asDBObject(o)

  protected implicit def mongoObjectOption2domainObject(o: Option[collection.T])(implicit m: Manifest[A]) = o map (grater[A].asObject(_))

  protected def addIndex(index: DBObject, unique: Boolean, indexing: Boolean) =
    collection.underlying.ensureIndex(index, MongoDBObject(
      "unique" -> unique,
      "background" -> true,
      "sparse" -> indexing))

  protected def atomicUpdate(params: Map[String, Any]) = {
    val bldr = MongoDBObject.newBuilder
    for ((k, v) <- params) bldr += k -> v
    MongoDBObject("$set" -> bldr.result.asDBObject)
  }

  sealed abstract class Order(val order: Int)
  case object Ascending extends Order(1)
  case object Descending extends Order(-1)

  sealed abstract class IndexType(val index: Boolean)
  case object Sparse extends IndexType(true)
  case object Complete extends IndexType(false)

  sealed abstract class Uniqueness(val uniqueness: Boolean)
  case object Enforced extends Uniqueness(true)
  case object Unenforced extends Uniqueness(false)
}