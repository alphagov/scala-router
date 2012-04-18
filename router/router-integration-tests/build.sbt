organization := "uk.gov.gds"

name := "router-integration-tests"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

seq(webSettings :_*)

libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % "2.0.1",
        "org.eclipse.jetty" % "jetty-webapp" % "7.4.5.v20110725" % "container",
        "javax.servlet" % "servlet-api" % "2.5" % "provided",
        "org.apache.httpcomponents" % "httpclient" % "4.1.2" % "test",
        "gov.gds" %% "integration-tools" % "1.4-SNAPSHOT" % "test",
        "org.scalatest" %% "scalatest" % "1.6.1" % "test",
   	    "org.slf4j" % "log4j-over-slf4j" % "1.6.1" % "test"
    )

parallelExecution in Test := false

resolvers ++= Seq(
    "GDS maven repo snapshots" at "http://alphagov.github.com/maven/snapshots",
    "GDS maven repo releases" at "http://alphagov.github.com/maven/releases",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "repo.novus snaps" at "http://repo.novus.com/snapshots/",
    "repo.codahale" at "http://repo.codahale.com"
)
