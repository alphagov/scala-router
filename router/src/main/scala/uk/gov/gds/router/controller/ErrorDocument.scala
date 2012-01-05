package uk.gov.gds.router.controller

object ErrorDocument {

  def document(code: Int) = scala.io.Source.fromInputStream(stream(code)).mkString("")

  def stream(code: Int) = getClass().getResourceAsStream("/" + code.toString().substring(0, 1) + "00.html")
}