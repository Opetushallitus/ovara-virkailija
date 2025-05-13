package fi.oph.ovara.backend.raportointi.dto

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
    "naytaHakutoiveet" -> params.naytaHakutoiveet
  ).collect { case (key, Some(value)) => key -> value }
}
