package fi.oph.ovara.backend.raportointi.dto

import fi.oph.ovara.backend.domain.Kielistetty

case class RawKkKoulutuksetToteutuksetHakukohteetParams(
                                                         haut: List[String],
                                                         tulostustapa: String,
                                                         oppilaitokset: List[String],
                                                         toimipisteet: List[String],
                                                         hakukohderyhmat: List[String],
                                                         koulutuksenTila: Option[String],
                                                         toteutuksenTila: Option[String],
                                                         hakukohteenTila: Option[String],
                                                         tutkinnonTasot: List[String]
                                                       )

case class ValidatedKkKoulutuksetToteutuksetHakukohteetParams(
                                                               haut: List[String],
                                                               tulostustapa: String,
                                                               oppilaitokset: List[String],
                                                               toimipisteet: List[String],
                                                               hakukohderyhmat: List[String],
                                                               koulutuksenTila: Option[String],
                                                               toteutuksenTila: Option[String],
                                                               hakukohteenTila: Option[String],
                                                               tutkinnonTasot: List[String]
                                                             )

def buildKkKoulutuksetToteutuksetHakukohteetAuditParams(
                                                         params: ValidatedKkKoulutuksetToteutuksetHakukohteetParams
                                                       ): Map[String, Any] = {
  Map(
    "haut" -> Option(params.haut).filter(_.nonEmpty),
    "tulostustapa" -> Option(params.tulostustapa),
    "oppilaitokset" -> Option(params.oppilaitokset).filter(_.nonEmpty),
    "toimipisteet" -> Option(params.toimipisteet).filter(_.nonEmpty),
    "hakukohderyhmat" -> Option(params.hakukohderyhmat).filter(_.nonEmpty),
    "koulutuksenTila" -> params.koulutuksenTila,
    "toteutuksenTila" -> params.toteutuksenTila,
    "hakukohteenTila" -> params.hakukohteenTila,
    "tutkinnonTaso" -> Option(params.tutkinnonTasot).filter(_.nonEmpty)
  ).collect { case (key, Some(value)) => key -> value }
}

def buildKkKoulutuksetToteutuksetHakukohteetParamsForExcel(params: ValidatedKkKoulutuksetToteutuksetHakukohteetParams, paramNames: Map[String, List[Kielistetty]]):
List[(String, Boolean | String | List[String] | Kielistetty | List[Kielistetty])] = {
  List(
    "haku" -> paramNames.getOrElse("haku", List.empty),
    "tulostustapa" -> params.tulostustapa,
    "oppilaitos" -> paramNames.getOrElse("oppilaitos", List.empty),
    "toimipiste" -> paramNames.getOrElse("toimipiste", List.empty),
    "hakukohderyhma" -> paramNames.getOrElse("hakukohderyhma", List.empty),
    "koulutuksenTila" -> params.koulutuksenTila.getOrElse(""),
    "toteutuksenTila" -> params.toteutuksenTila.getOrElse(""),
    "hakukohteenTila" -> params.hakukohteenTila.getOrElse(""),
    "kk-tutkinnon-taso" -> params.tutkinnonTasot
  ).filterNot { case (_, value) =>
    value match {
      case list: List[_] => list.isEmpty
      case str: String => str.isEmpty
      case _ => false
    }
  }
}
