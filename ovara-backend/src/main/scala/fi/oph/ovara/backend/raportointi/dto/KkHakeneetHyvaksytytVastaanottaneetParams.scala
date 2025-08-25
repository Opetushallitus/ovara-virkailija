package fi.oph.ovara.backend.raportointi.dto

import fi.oph.ovara.backend.domain.Kielistetty
import fi.oph.ovara.backend.utils.ParameterUtils

case class RawKkHakeneetHyvaksytytVastaanottaneetParams(
                                                         haut: List[String],
                                                         tulostustapa: String,
                                                         koulutustoimija: Option[String],
                                                         oppilaitokset: List[String],
                                                         toimipisteet: List[String],
                                                         hakukohteet: List[String],
                                                         hakukohderyhmat: List[String],
                                                         okmOhjauksenAlat: List[String],
                                                         tutkinnonTasot: List[String],
                                                         aidinkielet: List[String],
                                                         kansalaisuusluokat: List[String],
                                                         sukupuoli: Option[String],
                                                         ensikertalainen: String,
                                                         naytaHakutoiveet: String
                                                       )

case class ValidatedKkHakeneetHyvaksytytVastaanottaneetParams(
                                                               haut: List[String],
                                                               tulostustapa: String,
                                                               koulutustoimija: Option[String],
                                                               oppilaitokset: List[String],
                                                               toimipisteet: List[String],
                                                               hakukohteet: List[String],
                                                               hakukohderyhmat: List[String],
                                                               okmOhjauksenAlat: List[String],
                                                               tutkinnonTasot: List[String],
                                                               aidinkielet: List[String],
                                                               kansalaisuusluokat: List[String],
                                                               sukupuoli: Option[String],
                                                               ensikertalainen: Option[Boolean],
                                                               naytaHakutoiveet: Boolean
                                                             )

def buildKkHakeneetHyvaksytytVastaanottaneetAuditParams(
                                                         params: ValidatedKkHakeneetHyvaksytytVastaanottaneetParams
                                                       ): Map[String, Any] = {
  Map(
    "haut" -> Option(params.haut).filter(_.nonEmpty),
    "tulostustapa" -> Option(params.tulostustapa),
    "koulutustoimija" -> params.koulutustoimija,
    "oppilaitokset" -> Option(params.oppilaitokset).filter(_.nonEmpty),
    "toimipisteet" -> Option(params.toimipisteet).filter(_.nonEmpty),
    "hakukohteet" -> Option(params.hakukohteet).filter(_.nonEmpty),
    "hakukohderyhmat" -> Option(params.hakukohderyhmat).filter(_.nonEmpty),
    "okmOhjauksenAlat" -> Option(params.okmOhjauksenAlat).filter(_.nonEmpty),
    "tutkinnonTasot" -> Option(params.tutkinnonTasot).filter(_.nonEmpty),
    "aidinkielet" -> Option(params.aidinkielet).filter(_.nonEmpty),
    "kansalaisuusluokat" -> Option(params.kansalaisuusluokat).filter(_.nonEmpty),
    "sukupuoli" -> params.sukupuoli,
    "ensikertalainen" -> params.ensikertalainen,
    "naytaHakutoiveet" -> Option(params.naytaHakutoiveet)
  ).collect { case (key, Some(value)) => key -> value }
}

def buildKkHakeneetHyvaksytytVastaanottaneetParamsForExcel(params: ValidatedKkHakeneetHyvaksytytVastaanottaneetParams, paramNames: Map[String, List[Kielistetty]]):
List[(String, Boolean | String | List[String] | Kielistetty | List[Kielistetty])] = {
  val stringAndKielistettyParams = List(
    "haku" -> paramNames.getOrElse("haku", List.empty),
    "tulostustapa" -> params.tulostustapa,
    "koulutustoimija" -> paramNames.getOrElse("koulutustoimija", List.empty),
    "oppilaitos" -> paramNames.getOrElse("oppilaitos", List.empty),
    "toimipiste" -> paramNames.getOrElse("toimipiste", List.empty),
    "hakukohderyhma" -> paramNames.getOrElse("hakukohderyhma", List.empty),
    "hakukohde" -> paramNames.getOrElse("hakukohde", List.empty),
    "okm-ohjauksen-alat" -> paramNames.getOrElse("okm-ohjauksen-ala", List.empty),
    "kk-tutkinnon-taso" -> params.tutkinnonTasot,
    "aidinkieli" -> params.aidinkielet,
    "kansalaisuus" -> params.kansalaisuusluokat,
    "sukupuoli" -> paramNames.getOrElse("sukupuoli", List.empty),
  ).filterNot { case (_, value) =>
    value match {
      case list: List[_] => list.isEmpty
      case str: String => str.isEmpty
    }
  }

  val booleanParams = ParameterUtils.collectBooleanParams(List(
    "ensikertalainen" -> params.ensikertalainen,
    "nayta-hakutoiveet" -> Some(params.naytaHakutoiveet))
  )
  stringAndKielistettyParams ++ booleanParams
}
