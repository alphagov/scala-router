organization := "uk.gov.gds"

name := "router-test-harness"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

seq(webSettings :_*)

libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % "2.0.1",
        "org.eclipse.jetty" % "jetty-webapp" % "7.4.5.v20110725" % "jetty",
        "javax.servlet" % "servlet-api" % "2.5" % "provided"
    )

jettyPort := 4001

jettyContext := "/router-test-harness"

parallelExecution in Test := false

resolvers ++= Seq(
    "GDS maven repo snapshots" at "http://alphagov.github.com/maven/snapshots",
    "GDS maven repo releases" at "http://alphagov.github.com/maven/releases",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "repo.novus snaps" at "http://repo.novus.com/snapshots/",
    "repo.codahale" at "http://repo.codahale.com"
)
