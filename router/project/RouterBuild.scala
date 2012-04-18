import sbt._

object RouterBuild extends Build {
 
  lazy val root =
    Project("router-universe", file("."))
      .aggregate(routerApp, routerTestHarness, routerIntegrationTests)

  lazy val routerApp = Project("router", file("router"))

  lazy val routerTestHarness = Project("router-test-harness", file("router-test-harness"))

  lazy val routerIntegrationTests =
    Project("router-integration-tests", file("router-integration-tests"))
      .dependsOn(routerApp % "test->test;compile->compile")
      .dependsOn(routerTestHarness % "compile->compile;test->compile")
}
