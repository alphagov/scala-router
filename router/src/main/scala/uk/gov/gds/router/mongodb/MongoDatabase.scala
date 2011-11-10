package uk.gov.gds.router.mongodb

import util.DynamicVariable
import com.mongodb._
import com.mongodb.casbah.MongoConnection

object MongoDatabase {

  lazy val mongoConnection = MongoConnection(List(
    new ServerAddress("127.0.0.1", 27017)))

  val database = mongoConnection("router-dev");

  database.setWriteConcern(WriteConcern.SAFE)

  def onSameDatabaseServer[A](operation: => A) = try {
    DatabaseOperation.begin
    operation
  } finally {
    DatabaseOperation.end
  }

  private object DatabaseOperation {
    private lazy val operation = new DynamicVariable[Boolean](false)

    def begin {
      if (!isNested) {
        database.requestStart()
        database.requestEnsureConnection()
        operation.value_=(true)
      }
      else {
        throw new RuntimeException("Detected nested database operation. Not starting new one.")
      }
    }

    def end {
      database.requestDone()
      operation.value_=(false)
    }

    private def isNested = operation.value
  }

}