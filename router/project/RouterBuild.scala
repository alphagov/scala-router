import sbt._

object RouterBuild extends Build {
 
  lazy val root =
    Project("router-universe", file("."))
      .aggregate(routerApp, routerTestHarnessMainHost, routerTestHarnessAlsoSupportedHost, routerIntegrationTests)

  lazy val routerApp = Project("router", file("router"))

  lazy val routerTestHarnessMainHost= Project("router-test-harness-main-host", file("router-test-harness-main-host"))

  lazy val routerTestHarnessAlsoSupportedHost= Project("router-test-harness-also-supported-host", file("router-test-harness-also-supported-host"))

  lazy val routerIntegrationTests =
    Project("router-integration-tests", file("router-integration-tests"))
      .dependsOn(routerApp % "test->test;compile->compile")
      .dependsOn(routerTestHarnessMainHost % "compile->compile;test->compile")
      .dependsOn(routerTestHarnessAlsoSupportedHost % "compile->compile;test->compile")
}
