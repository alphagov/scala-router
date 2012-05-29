package uk.gov.gds.router.configuration

import javax.servlet.{ServletContextEvent, ServletContextListener}
import uk.gov.gds.router.util.{Logging, HttpProxy}

class ApplicationLifecycleManager extends ServletContextListener with Logging {

  def contextInitialized(sce: ServletContextEvent) { }

  def contextDestroyed(sce: ServletContextEvent) {
    HttpProxy.shutdown()
  }
}
