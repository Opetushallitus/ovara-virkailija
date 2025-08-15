package fi.oph.ovara.backend.raportointi.dto

case class RawHakijatParams(
                               haut: List[String],
                               oppilaitokset: List[String],
                               toimipisteet: List[String],
                               hakukohteet: List[String],
                               pohjakoulutukset: List[String],
                               valintatiedot: List[String],
                               vastaanottotiedot: List[String],
                               harkinnanvaraisuudet: List[String],
                               kaksoistutkinto: String,
                               urheilijatutkinto: String,
                               soraTerveys: String,
                               soraAiempi: String,
                               markkinointilupa: String,
                               julkaisulupa: String
                             )

case class ValidatedHakijatParams(
                          haut: List[String],
                          oppilaitokset: List[String],
                          toimipisteet: List[String],
                          hakukohteet: List[String],
                          pohjakoulutukset: List[String],
                          valintatiedot: List[String],
                          vastaanottotiedot: List[String],
                          harkinnanvaraisuudet: List[String],
                          kaksoistutkinto: Option[Boolean],
                          urheilijatutkinto: Option[Boolean],
                          soraTerveys: Option[Boolean],
                          soraAiempi: Option[Boolean],
                          markkinointilupa: Option[Boolean],
                          julkaisulupa: Option[Boolean]
                        )

def buildHakijatAuditParams(valid: ValidatedHakijatParams): Map[String, Any] = {
  Map(
    "haut"                 -> Option(valid.haut).filter(_.nonEmpty),
    "oppilaitokset"        -> Option(valid.oppilaitokset).filter(_.nonEmpty),
    "toimipisteet"         -> Option(valid.toimipisteet).filter(_.nonEmpty),
    "hakukohteet"          -> Option(valid.hakukohteet).filter(_.nonEmpty),
    "pohjakoulutukset"     -> Option(valid.pohjakoulutukset).filter(_.nonEmpty),
    "valintatiedot"        -> Option(valid.valintatiedot).filter(_.nonEmpty),
    "vastaanottotiedot"    -> Option(valid.vastaanottotiedot).filter(_.nonEmpty),
    "harkinnanvaraisuudet" -> Option(valid.harkinnanvaraisuudet).filter(_.nonEmpty),
    "kaksoistutkinto"      -> valid.kaksoistutkinto,
    "urheilijatutkinto"    -> valid.urheilijatutkinto,
    "soraTerveys"          -> valid.soraTerveys,
    "soraAiempi"           -> valid.soraAiempi,
    "markkinointilupa"     -> valid.markkinointilupa,
    "julkaisulupa"         -> valid.julkaisulupa
  ).collect { case (key, Some(value)) => key -> value }
}
