package fi.oph.ovara.backend.raportointi.dto

case class RawHakeneetHyvaksytytVastaanottaneetParams(
                                                       haut: List[String],
                                                       tulostustapa: String,
                                                       koulutustoimija: Option[String],
                                                       oppilaitokset: List[String],
                                                       toimipisteet: List[String],
                                                       hakukohteet: List[String],
                                                       koulutusalat1: List[String],
                                                       koulutusalat2: List[String],
                                                       koulutusalat3: List[String],
                                                       opetuskielet: List[String],
                                                       maakunnat: List[String],
                                                       kunnat: List[String],
                                                       harkinnanvaraisuudet: List[String],
                                                       naytaHakutoiveet: String,
                                                       sukupuoli: Option[String]
                                                     )

case class ValidatedHakeneetHyvaksytytVastaanottaneetParams(
                                                             haut: List[String],
                                                             tulostustapa: String,
                                                             koulutustoimija: Option[String],
                                                             oppilaitokset: List[String],
                                                             toimipisteet: List[String],
                                                             hakukohteet: List[String],
                                                             koulutusalat1: List[String],
                                                             koulutusalat2: List[String],
                                                             koulutusalat3: List[String],
                                                             opetuskielet: List[String],
                                                             maakunnat: List[String],
                                                             kunnat: List[String],
                                                             harkinnanvaraisuudet: List[String],
                                                             naytaHakutoiveet: Boolean,
                                                             sukupuoli: Option[String]
                                                           )

def buildHakeneetHyvaksytytVastaanottaneetAuditParams(
                                                       params: ValidatedHakeneetHyvaksytytVastaanottaneetParams
                                                     ): List[(String, Any)] = {
  List(
    "haut" -> Option(params.haut).filter(_.nonEmpty),
    "tulostustapa" -> Option(params.tulostustapa),
    "koulutustoimija" -> params.koulutustoimija,
    "oppilaitokset" -> Option(params.oppilaitokset).filter(_.nonEmpty),
    "toimipisteet" -> Option(params.toimipisteet).filter(_.nonEmpty),
    "hakukohteet" -> Option(params.hakukohteet).filter(_.nonEmpty),
    "koulutusalat1" -> Option(params.koulutusalat1).filter(_.nonEmpty),
    "koulutusalat2" -> Option(params.koulutusalat2).filter(_.nonEmpty),
    "koulutusalat3" -> Option(params.koulutusalat3).filter(_.nonEmpty),
    "opetuskielet" -> Option(params.opetuskielet).filter(_.nonEmpty),
    "maakunnat" -> Option(params.maakunnat).filter(_.nonEmpty),
    "kunnat" -> Option(params.kunnat).filter(_.nonEmpty),
    "harkinnanvaraisuudet" -> Option(params.harkinnanvaraisuudet).filter(_.nonEmpty),
    "naytaHakutoiveet" -> Option(params.naytaHakutoiveet),
    "sukupuoli" -> params.sukupuoli
  ).collect { case (key, Some(value)) => key -> value }
}
