package uk.gov.gds.router.repository

trait MongoIndexTypes {

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