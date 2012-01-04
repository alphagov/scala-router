package uk.gov.gds.router.controller

import com.google.inject.Guice
import com.google.inject.servlet.{ServletModule, GuiceServletContextListener}
import javax.servlet.Filter
import uk.gov.gds.router.util.Logging
import uk.gov.gds.router.management.{RouterManagementFilter, RouterRequestLoggingFilter}

class RouterModule extends ServletModule with Logging {

  protected override def configureServlets {
    serve("/*", classOf[RouterRequestLoggingFilter])
    serve("/*", classOf[RouteController])
    serve("/*", classOf[RouterApiController])
    serve("/*", classOf[RouterManagementFilter])
  }

  private def serve[A <: Filter](path: String, filterClass: Class[A]) = {
    filter(path).through(filterClass)
  }
}

class GuiceServletConfig extends GuiceServletContextListener {
  protected def getInjector = Guice.createInjector(new RouterModule)
}