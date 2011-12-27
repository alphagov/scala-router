package uk.gov.gds.router.util

import com.codahale.jerkson.Json

object JsonSerializer {

  def toJson(obj: AnyRef) = Json.generate(obj)

  def fromJson[A](data: String)(implicit m: Manifest[A]) = Json.parse[A](data)
}