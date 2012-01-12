package uk.gov.gds.router.controller

import io.Source


object ErrorDocument {

  lazy val document = initialiseErrorDocument()

  private def initialiseErrorDocument() = {
    val stream = getClass().getResourceAsStream("/error.html")
    try {
      Source.fromInputStream(stream).mkString("")
    }
    finally {
      stream.close()
    }
  }
}