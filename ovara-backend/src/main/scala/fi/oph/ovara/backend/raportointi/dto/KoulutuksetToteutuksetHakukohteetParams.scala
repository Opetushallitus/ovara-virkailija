package fi.oph.ovara.backend.raportointi.dto

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

