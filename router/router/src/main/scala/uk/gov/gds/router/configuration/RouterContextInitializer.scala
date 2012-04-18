package uk.gov.gds.router.configuration

import javax.servlet.{ServletContextEvent, ServletContextListener}
import uk.gov.gds.router.util.HttpProxy


class ApplicationLifecycleManager extends ServletContextListener {

  def contextInitialized(sce: ServletContextEvent) {}

  def contextDestroyed(sce: ServletContextEvent) {
    HttpProxy.shutdown()
  }
}
