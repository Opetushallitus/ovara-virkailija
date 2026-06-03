package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.utils.Constants.ISO_LOCAL_DATE_TIME_FORMATTER
import fi.oph.ovara.backend.utils.GenericOvaraJsonFormats
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.MonadicJValue.jvalueToMonadic
import org.json4s.{CustomSerializer, Extraction, Formats, JValue}

import java.time.LocalDateTime

trait ValpasFormats extends GenericOvaraJsonFormats {

  override implicit def jsonFormats: Formats = genericOvaraFormats + valpasHakuaikaSerializer

  private def valpasHakuaikaSerializer = new CustomSerializer[ValpasHakuaika](_ =>
    (
      { case s: JObject =>
        implicit def formats: Formats = jsonFormats

        def parseLocalDateTime(str: String) = {
          Option(LocalDateTime.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse(str)))
        }

        val alkaa = s \ "alkaa" match {
          case JString(alkaaStr) => parseLocalDateTime(alkaaStr)
          case _                 => None
        }
        val paattyy = s \ "paattyy" match {
          case JString(paattyyStr) => parseLocalDateTime(paattyyStr)
          case _                   => None
        }
        ValpasHakuaika(
          alkaa = alkaa,
          paattyy = paattyy
        )
      },
      { case h: ValpasHakuaika =>
        implicit def formats: Formats = jsonFormats

        Extraction.decompose(h)
      }
    )
  )

}
