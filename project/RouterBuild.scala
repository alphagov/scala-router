import sbt._

object RouterBuild extends Build {
  lazy val routerApp = Project("routerApp", file("router"))
}