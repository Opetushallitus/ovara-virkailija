package fi.oph.ovara.backend.raportointi.dto

import fi.oph.ovara.backend.domain.Kielistetty
import fi.oph.ovara.backend.utils.ParameterUtils

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
    "naytaYoArvosanat" -> Option(valid.naytaYoArvosanat),
    "naytaHetu" -> Option(valid.naytaHetu),
    "naytaPostiosoite" -> Option(valid.naytaPostiosoite)
  ).collect { case (key, Some(value)) => key -> value }
}

def buildKkHakijatParamsForExcel(params: ValidatedKkHakijatParams, paramNames: Map[String, List[Kielistetty]]):
List[(String, Boolean | String | List[String] | Kielistetty | List[Kielistetty])] = {
  val stringAndKielistettyParams = List(
    "haku" -> paramNames.getOrElse("haku", List.empty),
    "oppilaitos" -> paramNames.getOrElse("oppilaitos", List.empty),
    "toimipiste" -> paramNames.getOrElse("toimipiste", List.empty),
    "hakukohderyhma" -> paramNames.getOrElse("hakukohderyhma", List.empty),
    "hakukohde" -> paramNames.getOrElse("hakukohde", List.empty),
    "valintatieto" -> params.valintatiedot,
    "vastaanottotieto" -> params.vastaanottotiedot,
    "kansalaisuus" -> params.kansalaisuusluokat,
    
  ).filterNot { case (_, value) =>
    value match {
      case list: List[_] => list.isEmpty
    }
  }

  val booleanParams = ParameterUtils.collectBooleanParams(List(
    "markkinointilupa" -> params.markkinointilupa,
    "nayta-yo-arvosanat" -> Some(params.naytaYoArvosanat),
    "nayta-hetu" -> Some(params.naytaHetu),
    "nayta-postiosoite" -> Some(params.naytaPostiosoite))
  )
  stringAndKielistettyParams ++ booleanParams
}

