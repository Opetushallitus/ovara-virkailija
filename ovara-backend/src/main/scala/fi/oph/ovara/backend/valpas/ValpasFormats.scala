package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.utils.GenericOvaraJsonFormats
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.MonadicJValue.jvalueToMonadic
import org.json4s.{CustomSerializer, Extraction, Formats, JValue}

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField

trait ValpasFormats extends GenericOvaraJsonFormats {

  private val alkaaFormatter: DateTimeFormatter = new DateTimeFormatterBuilder()
    .appendPattern("uuuu-MM-dd['T'HH:mm]")
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
    .toFormatter()

  override implicit def jsonFormats: Formats = genericOvaraFormats + valpasHakuaikaSerializer

  private def valpasHakuaikaSerializer = new CustomSerializer[ValpasHakuaika](_ =>
    (
      { case s: JObject =>
        implicit def formats: Formats = jsonFormats

        // Päättymispäivää ei käytetä, joten oletetaan kaikille ajoille alkuajaksi
        // vuorokauden ensimmäinen hetki.
        def parseLocalDateTime(str: String) = {
          Option(LocalDateTime.from(alkaaFormatter.parse(str)))
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
