package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.Kielistetty

case class ParametriNimet(
                           parametri: String,
                           nimet: List[Kielistetty]
                         )

object ParameterUtils {

  def collectBooleanParams(params: List[(String, Option[Boolean])]): List[(String, Boolean)] = {
    params.collect {
      case (key, Some(value)) => key -> value
    }
  }
  
  def getKansalaisuusluokkaTranslation(value: String, translations: Map[String, String]): String = {
    value match {
      case "1" =>
        translations.getOrElse("raportti.kansalaisuus.suomi", "raportti.kansalaisuus.suomi")
      case "2" =>
        translations.getOrElse("raportti.kansalaisuus.eu-eta", "raportti.kansalaisuus.eu-eta")
      case _ =>
        translations.getOrElse("raportti.kansalaisuus.muu", "raportti.kansalaisuus.muu")
    }
  }
}
