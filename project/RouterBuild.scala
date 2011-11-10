import sbt._
import Keys._
import Defaults._

object RouerBuild extends Build {
  lazy val routerApp = Project("routerApp", file("router"))
}