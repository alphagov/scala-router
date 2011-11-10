package uk.gov.gds.router.util

import org.slf4j.LoggerFactory

trait Logging {
  val logger = LoggerFactory.getLogger(this.getClass)

  def info(f :String => String) {

  }
}