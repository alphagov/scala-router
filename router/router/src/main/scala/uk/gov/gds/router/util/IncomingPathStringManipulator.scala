package uk.gov.gds.router.util

import uk.gov.gds.router.model.Route

object IncomingPathStringManipulator extends Logging {

  def getPrefixPath(incomingPath: String) = {

    val host = incomingPath.split("/").take(1).mkString("/")

    var prefixPath = ""

    if (host.startsWith("www") || host.startsWith("mainhost") || host.startsWith("alsosupported") || host.startsWith("router") ) {
      prefixPath = incomingPath.split("/").take(2).mkString("/")
    }
    else {
      prefixPath = host
    }
    prefixPath

  }
  def getPathToResource(requestedPath: String) = {

    val host = requestedPath.split("/").take(2).mkString("/")
    var restOfPath = ""
    if (host.startsWith("/www") || host.startsWith("/mainhost") || host.startsWith("/alsosupported") || host.startsWith("/router")) {
      restOfPath = requestedPath.substring((host.length()), requestedPath.length());
    }
    else {
      restOfPath = requestedPath
    }

    restOfPath
  }

}
