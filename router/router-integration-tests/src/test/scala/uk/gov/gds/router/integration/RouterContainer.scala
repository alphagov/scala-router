package uk.gov.gds.router.integration

import com.gu.integration.{ClasspathWebApp, LazyStop, AppServer}

object RouterContainer extends AppServer with LazyStop {
  override def port = 4000

  val apps = List(RouterWebApp)

  object RouterWebApp extends ClasspathWebApp {
    val srcPath = "router"
  }

}

object RouterTestHarnessContainer extends AppServer with LazyStop {
  override def port = 4001

  val apps = List(RouterTestHarnessWebapp)

  object RouterTestHarnessWebapp extends ClasspathWebApp {
    def srcPath = "router-test-harness-main-host"
  }

}


