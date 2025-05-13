package fi.oph.ovara.backend.raportointi.dto

case class RawKkHakijatParams(
                               haut: List[String],
                               oppilaitokset: List[String],
                               toimipisteet: List[String],
                               hakukohteet: List[String],
                               valintatiedot: List[String],
                               vastaanottotiedot: List[String],
                               hakukohderyhmat: List[String],
                               kansalaisuusluokat: List[String],
                               markkinointilupa: String,
                               naytaYoArvosanat: String,
                               naytaHetu: String,
                               naytaPostiosoite: String
                             )

case class ValidatedKkHakijatParams(
                                     haut: List[String],
                                     oppilaitokset: List[String],
                                     toimipisteet: List[String],
                                     hakukohteet: List[String],
                                     valintatiedot: List[String],
                                     vastaanottotiedot: List[String],
                                     hakukohderyhmat: List[String],
                                     kansalaisuusluokat: List[String],
                                     markkinointilupa: Option[Boolean],
                                     naytaYoArvosanat: Boolean,
                                     naytaHetu: Boolean,
                                     naytaPostiosoite: Boolean
                                   )

def buildKkHakijatAuditParams(valid: ValidatedKkHakijatParams): Map[String, Any] = {
  Map(
    "haut" -> Option(valid.haut).filter(_.nonEmpty),
    "oppilaitokset" -> Option(valid.oppilaitokset).filter(_.nonEmpty),
    "toimipisteet" -> Option(valid.toimipisteet).filter(_.nonEmpty),
    "hakukohteet" -> Option(valid.hakukohteet).filter(_.nonEmpty),
    "valintatiedot" -> Option(valid.valintatiedot).filter(_.nonEmpty),
    "vastaanottotiedot" -> Option(valid.vastaanottotiedot).filter(_.nonEmpty),
    "hakukohderyhmat" -> Option(valid.hakukohderyhmat).filter(_.nonEmpty),
    "kansalaisuusluokat" -> Option(valid.kansalaisuusluokat).filter(_.nonEmpty),
    "markkinointilupa" -> valid.markkinointilupa,
    "naytaYoArvosanat" -> valid.naytaYoArvosanat,
    "naytaHetu" -> valid.naytaHetu,
    "naytaPostiosoite" -> valid.naytaPostiosoite
  ).collect { case (key, Some(value)) => key -> value }
}