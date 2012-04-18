package uk.gov.gds.router.util

import com.codahale.jerkson.Json._

object JsonSerializer {

  def toJson(obj: AnyRef) = generate(obj)

  def fromJson[A](data: String)(implicit m: Manifest[A]) = parse[A](data)
}