package gov.uk.gds.router

import com.gu.integration.{ClasspathWebApp, LazyStop, AppServer}

object ApplicationsUnderTest extends AppServer with LazyStop {
  lazy val apps = List(RouterWebApp)
}

object RouterWebApp extends ClasspathWebApp {
  lazy val srcPath = "router"
  override lazy val contextPath = "/router"
}
