package uk.gov.gds.router.configuration

object RouterConfig {

  def databaseHosts: List[String] = Config.setting("mongo.database.hosts").split(",").toList

  def databaseName: String = Config(name = "mongo.database.name", default = "router-dev")

  def getPrefixForHost(serverName: String): String = {
    val hostKey = "host-" + serverName
    Config(name = hostKey, default = "/govuk/")       // remove the default after puppet config has been rolled out
  }

}
