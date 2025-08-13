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

object KoulutuksetToteutuksetHakukohteetUtils {
  def buildParams(valid: ValidatedKoulutuksetToteutuksetHakukohteetParams): List[(String, Boolean | String | List[String])] = {
    List(
      "haku" -> Option(valid.haut).filter(_.nonEmpty),
      "koulutustoimija" -> valid.koulutustoimija,
      "oppilaitos" -> Option(valid.oppilaitokset).filter(_.nonEmpty),
      "toimipiste" -> Option(valid.toimipisteet).filter(_.nonEmpty),
      "koulutuksenTila" -> valid.koulutuksenTila,
      "toteutuksenTila" -> valid.toteutuksenTila,
      "hakukohteenTila" -> valid.hakukohteenTila,
      "valintakoe" -> valid.valintakoe
    ).collect { case (key, Some(value)) => key -> value }
  }
}

