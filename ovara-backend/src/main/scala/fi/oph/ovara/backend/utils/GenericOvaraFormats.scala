package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.Kieli
import org.json4s.JsonAST.JString
import org.json4s.jackson.Serialization.write
import org.json4s.{CustomKeySerializer, CustomSerializer, DefaultFormats, Formats, JNull, MappingException}

import scala.collection.Seq
import scala.util.control.NonFatal

trait GenericOvaraJsonFormats extends GenericOvaraFormats {
  implicit def jsonFormats: Formats = genericOvaraFormats

  def toJson(data: AnyRef): String = write(data)
}

trait GenericOvaraFormats {
  def genericOvaraFormats: Formats = DefaultFormats.strict
    .addKeySerializers(Seq(kieliKeySerializer)) ++
    Seq(
      stringSerializer(Kieli.withName)
    )
  
  private def kieliKeySerializer = new CustomKeySerializer[Kieli](_ => ( {
    case s: String => Kieli.withName(s)
  }, {
    case k: Kieli => k.toString
  }))

  private def stringSerializer[A>:Null: Manifest](construct: String => A): CustomSerializer[A] =
    stringSerializer(construct, (a: A) => a.toString)

  private def stringSerializer[A>:Null: Manifest](construct: String => A, deconstruct: A => String) =
    new CustomSerializer[A](_ => ( {
      case JString(s) =>
        try {
          construct(s)
        } catch {
          case NonFatal(e) =>
            throw MappingException(e.getMessage, new java.lang.IllegalArgumentException(e))
        }
      case JNull => null
    }, {
      case a: A => JString(deconstruct(a))
    }))
}

