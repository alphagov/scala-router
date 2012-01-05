package uk.gov.gds.router.controller

object ErrorDocument {

  def document = scala.io.Source.fromInputStream(stream).mkString("")

  def stream = getClass().getResourceAsStream("/error.html")
}