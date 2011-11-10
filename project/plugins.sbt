resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"

resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.8"))

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "0.11.0")


