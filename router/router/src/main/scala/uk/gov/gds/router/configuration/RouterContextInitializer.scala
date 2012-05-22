package uk.gov.gds.router.configuration

import javax.servlet.{ServletContextEvent, ServletContextListener}
import uk.gov.gds.router.repository.route.Routes
import uk.gov.gds.router.util.{Logging, HttpProxy}


class ApplicationLifecycleManager extends ServletContextListener with Logging {

  def contextInitialized(sce: ServletContextEvent) {


//    Routes.all.filterNot(x => x.incoming_path.startsWith("/govuk/")).foreach {
//      route =>
//        val newRouteId = "/govuk/" + route.incoming_path
//        logger.info("Updating route: " + route.incoming_path + " to " + newRouteId)
//        Routes.simpleAtomicUpdate(route.incoming_path, Map("incoming_path" -> newRouteId))
//    }
  }

  def contextDestroyed(sce: ServletContextEvent) {
    HttpProxy.shutdown()
  }
}
