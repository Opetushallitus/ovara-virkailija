package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.utils.Constants.ISO_LOCAL_DATE_TIME_FORMATTER
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.MonadicJValue.jvalueToMonadic
import org.json4s.jackson.Serialization.write
import org.json4s.{
  CustomKeySerializer,
  CustomSerializer,
  DefaultFormats,
  Extraction,
  Formats,
  JNull,
  JValue,
  MappingException,
  jvalue2extractable
}

import java.time.LocalDate
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
      valintatapajonoSerializer,
      hakuaikaSerializer,
      koulutuksenAlkamiskausiSerializer
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

  private def hakuaikaSerializer = new CustomSerializer[Hakuaika](_ =>
    (
      { case s: JObject =>
        implicit def formats: Formats = genericOvaraFormats

        def parseLocalDate(str: String) = {
          Option(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse(str)))
        }

        val alkaa = s \ "alkaa" match {
          case JString(alkaaStr) => parseLocalDate(alkaaStr)
          case _                 => None
        }
        val paattyy = s \ "paattyy" match {
          case JString(paattyyStr) => parseLocalDate(paattyyStr)
          case _                   => None
        }
        Hakuaika(
          alkaa = alkaa,
          paattyy = paattyy
        )
      },
      { case h: Hakuaika =>
        implicit def formats: Formats = genericOvaraFormats

        Extraction.decompose(h)
      }
    )
  )

  private def koulutuksenAlkamiskausiSerializer = new CustomSerializer[Alkamiskausi](_ =>
    (
      { case s: JObject =>
        implicit def formats: Formats = genericOvaraFormats

        def parseLocalDate(str: String) = {
          Option(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse(str)))
        }

        val alkamiskausityyppi = (s \ "alkamiskausityyppi").extract[String]
        val alkamiskausikoodiuri = s \ "koulutuksenAlkamiskausiKoodiUri" match {
          case JString(kausikoodiuri) => Some(kausikoodiuri)
          case _ => None
        }
        val alkamisvuosi = s \ "koulutuksenAlkamisvuosi" match {
          case JString(kausikoodiuri) => Some(kausikoodiuri)
          case _ => None
        }
        val alkamispaivamaara = s \ "koulutuksenAlkamispaivamaara" match {
          case JString(alkaaStr) => parseLocalDate(alkaaStr)
          case _                 => None
        }
        val paattymispaivamaara = s \ "koulutuksenPaattymispaivamaara" match {
          case JString(paattyyStr) => parseLocalDate(paattyyStr)
          case _                   => None
        }
        Alkamiskausi(
          alkamiskausityyppi = alkamiskausityyppi,
          koulutuksenAlkamiskausiKoodiUri = alkamiskausikoodiuri,
          koulutuksenAlkamisvuosi = alkamisvuosi,
          koulutuksenAlkamispaivamaara = alkamispaivamaara,
          koulutuksenPaattymispaivamaara = paattymispaivamaara
        )
      },
      { case a: Alkamiskausi =>
        implicit def formats: Formats = genericOvaraFormats

        Extraction.decompose(a)
      }
    )
  )
}
