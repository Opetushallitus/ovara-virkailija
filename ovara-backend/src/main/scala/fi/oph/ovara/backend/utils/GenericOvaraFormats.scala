package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.{Kieli, Kielistetty, Valintatapajono}
import org.json4s.JsonAST.{JInt, JObject, JString}
import org.json4s.MonadicJValue.jvalueToMonadic
import org.json4s.jackson.Serialization.write
import org.json4s.{CustomKeySerializer, CustomSerializer, DefaultFormats, Extraction, Formats, JNull, JObject, JValue, MappingException, jvalue2extractable}

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
      stringSerializer(Kieli.withName),
      valintatapajonoSerializer
    )

  private def kieliKeySerializer = new CustomKeySerializer[Kieli](_ =>
    (
      { case s: String =>
        Kieli.withName(s)
      },
      { case k: Kieli =>
        k.toString
      }
    )
  )

  private def stringSerializer[A >: Null: Manifest](construct: String => A): CustomSerializer[A] =
    stringSerializer(construct, (a: A) => a.toString)

  private def stringSerializer[A >: Null: Manifest](construct: String => A, deconstruct: A => String) = {
    new CustomSerializer[A](_ =>
      (
        {
          case JString(s) =>
            try {
              construct(s)
            } catch {
              case NonFatal(e) =>
                throw MappingException(e.getMessage, new java.lang.IllegalArgumentException(e))
            }
          case JNull => null
        },
        { case a: A =>
          JString(deconstruct(a))
        }
      )
    )
  }

  private def valintatapajonoSerializer = new CustomSerializer[Valintatapajono](_ =>
    (
      { case s: JObject =>
        implicit def formats: Formats = genericOvaraFormats

        Valintatapajono(
          valintatapajonoOid = (s \ "valintatapajono_oid").extract[String],
          valintatapajononNimi = (s \ "valintatapajono_nimi").extract[String],
          valinnanTila = (s \ "valinnan_tila").extract[String],
          valinnanTilanKuvaus = (s \ "valinnantilan_kuvauksen_teksti").extract[Kielistetty]
        )
      },
      { case v: Valintatapajono =>
        implicit def formats: Formats = genericOvaraFormats

        Extraction.decompose(v)
      }
    )
  )
}
