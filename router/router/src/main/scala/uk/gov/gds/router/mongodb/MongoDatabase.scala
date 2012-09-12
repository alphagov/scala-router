package uk.gov.gds.router.mongodb

import util.DynamicVariable
import com.mongodb._
import com.mongodb.casbah.MongoConnection
import uk.gov.gds.router.configuration.RouterConfig
import uk.gov.gds.router.repository.application.Applications
import uk.gov.gds.router.util.Logging
import uk.gov.gds.router.model.{ApplicationForRedirectRoutes, ApplicationForGoneRoutes}

object MongoDatabase extends Logging {

  private lazy val inOperation = new DynamicVariable[Boolean](false)
  private val mongoConnection = MongoConnection(RouterConfig.databaseHosts.map(new ServerAddress(_)))
  private val applicationsToCreateOnStartup = List(ApplicationForGoneRoutes, ApplicationForRedirectRoutes)

  val database = mongoConnection(RouterConfig.databaseName);
  database.setWriteConcern(WriteConcern.SAFE)

  try {
    initialiseMongo()
  }
  catch {
    case e: Exception =>
      logger.error("Error initialising connection to mongo database, is it configured correctly?")
      logger.error("Router is trying to connect to " + RouterConfig.databaseHosts)
      logger.error("Exception is: ", e)
  }

  def initialiseMongo() {
    applicationsToCreateOnStartup.foreach {
      application =>
        Applications.load(application.application_id).getOrElse {
          logger.info("Creating system application " + application.application_id)

          try {
            Applications.store(application)
          }
          catch {
            case e: Exception => logger.warn("Failed to created system application " + application.application_id)
          }
        }
    }
  }

  def getCollection(collectionName: String) = {
    val collection = database(collectionName)

    // Temporarily disabled slaveOK to ensure that all queries are to the master node to simplify roll outs
    //collection.slaveOk()
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

