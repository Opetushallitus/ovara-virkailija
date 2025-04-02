package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.{Alkamiskausi, Hakuaika, Valintatapajono}
import org.json4s.jackson.Serialization.read

object ExtractorUtils extends GenericOvaraJsonFormats {
  def extractValintatapajonot(jsonArray: Option[String]): List[Valintatapajono] = {
    jsonArray.map(read[List[Valintatapajono]]).getOrElse(List())
  }

  def extractHakuaika(jsonObject: Option[String]): Option[Hakuaika] = {
    jsonObject.map(read[Hakuaika])
  }

  def extractAlkamiskausi(jsonObject: Option[String]): Option[Alkamiskausi] = {
    jsonObject.map(read[Alkamiskausi])
  }
}
