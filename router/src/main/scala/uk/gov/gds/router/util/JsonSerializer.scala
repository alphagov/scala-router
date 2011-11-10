package uk.gov.gds.router.util

import net.liftweb.json.{NoTypeHints, Serialization}

object JsonSerializer {

  implicit val formats = Serialization.formats(NoTypeHints)

  def toJson(obj: AnyRef) = Serialization.write(obj)

  def fromJson[A](data: String)(implicit m: Manifest[A]) = Serialization.read[A](data)
}