package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.Valintatapajono
import org.json4s.jackson.Serialization.read

object ExtractorUtils extends GenericOvaraJsonFormats {
  def extractValintatapajonot(jsonArray: Option[String]): List[Valintatapajono] = {
    jsonArray.map(read[List[Valintatapajono]]).getOrElse(List())
  }
}
