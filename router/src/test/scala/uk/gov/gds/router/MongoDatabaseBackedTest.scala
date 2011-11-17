package uk.gov.gds.router

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import util.Logging
import uk.gov.gds.router.mongodb.MongoDatabase.database

abstract class MongoDatabaseBackedTest extends FunSuite with BeforeAndAfterEach with Logging {

  protected val cleanOutDatabaseBeforeEachTest = true

  override protected def beforeEach() {
    if (cleanOutDatabaseBeforeEachTest) {
      database("applications").drop()
      database("routes").drop()
    }
  }
}