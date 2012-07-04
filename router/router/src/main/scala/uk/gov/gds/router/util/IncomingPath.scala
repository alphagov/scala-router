package uk.gov.gds.router.util

import uk.gov.gds.router.model.Route
import scala.util.matching.Regex

object IncomingPath extends Logging {
 
  val hostPattern  = """^/*host/([^/]*)/*(.*)$""".r
  val prefixPattern  = """^/*([^/]*)/*(.*)$""".r

  def host(incoming_path: String) = incoming_path match {
    case hostPattern(host, path) =>
      host
    case _ =>
      ""
  }

  def path(incoming_path: String) = incoming_path match {
    case hostPattern(host, path) =>
      path
    case _ =>
      incoming_path
  }

  def prefix(incoming_path: String) = path(incoming_path) match {
    case prefixPattern(prefix, path) =>
      prefix
    case _ =>
      incoming_path
  }
}
