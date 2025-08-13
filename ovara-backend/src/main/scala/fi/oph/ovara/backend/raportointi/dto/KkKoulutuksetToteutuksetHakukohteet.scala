package fi.oph.ovara.backend.raportointi.dto

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
                                                       ): List[(String, Any)] = {
  List(
    "haut" -> Option(params.haut).filter(_.nonEmpty),
    "tulostustapa" -> Option(params.tulostustapa),
    "oppilaitokset" -> Option(params.oppilaitokset).filter(_.nonEmpty),
    "toimipisteet" -> Option(params.toimipisteet).filter(_.nonEmpty),
    "hakukohderyhmat" -> Option(params.hakukohderyhmat).filter(_.nonEmpty),
    "koulutuksenTila" -> params.koulutuksenTila,
    "toteutuksenTila" -> params.toteutuksenTila,
    "hakukohteenTila" -> params.hakukohteenTila,
    "tutkinnonTasot" -> Option(params.tutkinnonTasot).filter(_.nonEmpty)
  ).collect { case (key, Some(value)) => key -> value }
}