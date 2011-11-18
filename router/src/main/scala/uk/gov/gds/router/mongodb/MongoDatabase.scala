package uk.gov.gds.router.mongodb

import util.DynamicVariable
import com.mongodb._
import com.mongodb.casbah.MongoConnection
import uk.gov.gds.router.configuration.RouterConfig

object MongoDatabase {

  private lazy val inOperation = new DynamicVariable[Boolean](false)
  private val mongoConnection = MongoConnection(RouterConfig.databaseHosts.map(new ServerAddress(_)))

  val database = mongoConnection("router-dev");
  database.setWriteConcern(WriteConcern.SAFE)

  def getCollection(collectionName: String) = {
    val collection = database(collectionName)
    collection.slaveOk()
    collection
  }

  def onSameDatabaseServer[A](operation: => A) = try {
    if (!inOperation.value) {
      database.requestStart()
      database.requestEnsureConnection()
      inOperation.value_=(true)
    }
    else {
      throw new RuntimeException("Detected nested database operation. Not starting new one.")
    }
    operation
  } finally {
    database.requestDone()
    inOperation.value_=(false)
  }
}

