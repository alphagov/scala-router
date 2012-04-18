package uk.gov.gds.router.configuration

import java.util.Properties
import uk.gov.gds.router.util.Logging
import java.io.{FileInputStream, File, InputStream}

private[configuration] object Config extends Logging {

  private val developmentConfigFile = "/router-development-configuration.properties"
  private val productionConfigFile = "/etc/gdsrouter.properties"

  private val properties = new Properties
  readConfigFile()

  def setting(propertyName: String) =
    Option(properties.get(propertyName).asInstanceOf[String])
      .getOrElse(throw new RuntimeException("Can't find setting: " + propertyName))

  private def readConfigFile() {
   initialiseFromStream(selectConfig())
  }

  private def selectConfig() = {
    val productionConfig = new File(productionConfigFile)

    if (productionConfig.exists()) {
      logger.info("Using production configuration from " + productionConfigFile)
      new FileInputStream(productionConfigFile)
    }
    else {
      logger.info("No production config found in " + productionConfigFile + ", using development configuration")
      this.getClass.getResourceAsStream(developmentConfigFile)
    }
  }

  private def initialiseFromStream(propertyStream: InputStream) {
    try {
      properties.load(propertyStream)
    }
    finally {
      propertyStream.close();
    }
  }
}