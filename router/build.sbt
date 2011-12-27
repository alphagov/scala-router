organization := "uk.gov.gds"

name := "router"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.0-1"

seq(webSettings :_*)

libraryDependencies ++= Seq(
        "com.mongodb.casbah" %% "casbah" % "2.1.5-1",
        "com.gu" %% "integration-tools" % "1.3" % "test",
        "com.gu" %% "management" % "4.3",
        "org.scalatest" %% "scalatest" % "1.6.1" % "test",
        "org.apache.httpcomponents" % "httpclient" % "4.1.2",
        "com.google.inject.extensions" % "guice-servlet" % "3.0-rc2",
        "org.scalatra" %% "scalatra" % "2.0.1",
        "org.scalatra" %% "scalatra-scalate" % "2.0.1",
        "org.eclipse.jetty" % "jetty-webapp" % "7.4.5.v20110725" % "jetty",
        "javax.servlet" % "servlet-api" % "2.5" % "provided",
	    "org.slf4j" % "log4j-over-slf4j" % "1.6.1" % "test",
	    "org.slf4j" % "jcl-over-slf4j" % "1.6.1" % "test",
	    "ch.qos.logback" % "logback-classic" % "0.9.28" % "test",
	    "org.slf4j" % "log4j-over-slf4j" % "1.6.1",
	    "org.slf4j" % "jcl-over-slf4j" % "1.6.1",
	    "ch.qos.logback" % "logback-classic" % "0.9.28",
	    "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT",
	    "com.codahale" %% "jerkson" % "0.4.2"
    )

jettyPort := 4000

jettyContext := "/router"

ivyXML := <dependencies>
	         <exclude module="log4j"/>
	         <exclude module="commons-logging"/>
	         <exclude module="slf4j-log4j12"/>
	         <exclude module="slf4j-log4j13"/>
	      </dependencies>

parallelExecution in Test := false

resolvers ++= Seq(
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "Guardian Github Releases" at "http://guardian.github.com/maven/repo-releases",
    "repo.novus snaps" at "http://repo.novus.com/snapshots/",
    "repo.codahale" at "http://repo.codahale.com"
)
