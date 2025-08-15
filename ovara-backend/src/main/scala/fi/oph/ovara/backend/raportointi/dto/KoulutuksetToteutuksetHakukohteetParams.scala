package fi.oph.ovara.backend.raportointi.dto

import fi.oph.ovara.backend.domain.Kielistetty

case class RawKoulutuksetToteutuksetHakukohteetParams(
                                                       haut: List[String],
                                                       koulutustoimija: Option[String],
                                                       oppilaitokset: List[String],
                                                       toimipisteet: List[String],
                                                       koulutuksenTila: Option[String],
                                                       toteutuksenTila: Option[String],
                                                       hakukohteenTila: Option[String],
                                                       valintakoe: String
                                                     )

case class ValidatedKoulutuksetToteutuksetHakukohteetParams(
                                                             haut: List[String],
                                                             koulutustoimija: Option[String],
                                                             oppilaitokset: List[String],
                                                             toimipisteet: List[String],
                                                             koulutuksenTila: Option[String],
                                                             toteutuksenTila: Option[String],
                                                             hakukohteenTila: Option[String],
                                                             valintakoe: Option[Boolean]
                                                           )

case class KoulutuksetToteutuksetHakukohteetReadableParams(
                                                  hakuNimet: List[Kielistetty],
                                                  koulutustoimijaNimi: Kielistetty,
                                                  oppilaitosNimet: List[Kielistetty],
                                                  toimipisteNimet: List[Kielistetty],
                                                  koulutuksenTila: Option[String],
                                                  toteutuksenTila: Option[String],
                                                  hakukohteenTila: Option[String],
                                                  valintakoe: Option[Boolean]
                                                )

case class ParametriNimet(
                           parametri: String,
                           nimet: List[Kielistetty]
                         )

def buildKoulutuksetToteutuksetHakukohteetAuditParams(valid: ValidatedKoulutuksetToteutuksetHakukohteetParams): Map[String, Any] = {
  Map(
    "haut" -> Option(valid.haut).filter(_.nonEmpty),
    "koulutustoimija" -> valid.koulutustoimija,
    "oppilaitokset" -> Option(valid.oppilaitokset).filter(_.nonEmpty),
    "toimipisteet" -> Option(valid.toimipisteet).filter(_.nonEmpty),
    "koulutuksenTila" -> valid.koulutuksenTila,
    "toteutuksenTila" -> valid.toteutuksenTila,
    "hakukohteenTila" -> valid.hakukohteenTila,
    "valintakoe" -> valid.valintakoe
  ).collect { case (key, Some(value)) => key -> value }
}

object KoulutuksetToteutuksetHakukohteetUtils {
  def buildParams(params: ValidatedKoulutuksetToteutuksetHakukohteetParams, paramNames: Map[String, List[Kielistetty]]):
  List[(String, Boolean | String | List[String] | Kielistetty | List[Kielistetty])] = {
    List(
      "haku" -> paramNames.getOrElse("haku", List.empty),
      "koulutustoimija" -> paramNames.getOrElse("koulutustoimija", List.empty),
      "oppilaitos" -> paramNames.getOrElse("oppilaitos", List.empty),
      "toimipiste" -> paramNames.getOrElse("toimipiste", List.empty),
      "koulutuksenTila" -> params.koulutuksenTila.getOrElse(""),
      "toteutuksenTila" -> params.toteutuksenTila.getOrElse(""),
      "hakukohteenTila" -> params.hakukohteenTila.getOrElse(""),
      "valintakoe" -> params.valintakoe.getOrElse(false)
    ).filterNot { case (_, value) =>
      value match {
        case list: List[_] => list.isEmpty
        case str: String => str.isEmpty
        case _ => false
      }
    }  
  }
}

