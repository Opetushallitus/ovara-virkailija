package fi.oph.ovara.backend.raportointi.dto

import fi.oph.ovara.backend.domain.Kielistetty
import fi.oph.ovara.backend.utils.ParameterUtils

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
                                                     ): Map[String, Any] = {
  Map(
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

def buildHakeneetHyvaksytytVastaanottaneetParamsForExcel(params: ValidatedHakeneetHyvaksytytVastaanottaneetParams, paramNames: Map[String, List[Kielistetty]]):
List[(String, Boolean | String | List[String] | Kielistetty | List[Kielistetty])] = {
  val stringAndKielistettyParams = List(
    "haku" -> paramNames.getOrElse("haku", List.empty),
    "tulostustapa" -> params.tulostustapa,
    "koulutustoimija" -> paramNames.getOrElse("koulutustoimija", List.empty),
    "oppilaitos" -> paramNames.getOrElse("oppilaitos", List.empty),
    "toimipiste" -> paramNames.getOrElse("toimipiste", List.empty),
    "hakukohde" -> paramNames.getOrElse("hakukohde", List.empty),
    "opetuskieli" -> paramNames.getOrElse("opetuskieli", List.empty),
    "koulutusala1" -> paramNames.getOrElse("koulutusala1", List.empty),
    "koulutusala2" -> paramNames.getOrElse("koulutusala2", List.empty),
    "koulutusala3" -> paramNames.getOrElse("koulutusala3", List.empty),
    "maakunta" -> paramNames.getOrElse("maakunta", List.empty),
    "kunta" -> paramNames.getOrElse("kunta", List.empty),
    "harkinnanvaraisuus" -> params.harkinnanvaraisuudet.map(_.stripPrefix("ATARU_").toLowerCase),
    "sukupuoli" -> paramNames.getOrElse("sukupuoli", List.empty),
  ).filterNot { case (_, value) =>
    value match {
      case list: List[_] => list.isEmpty
      case str: String => str.isEmpty
    }
  }
  stringAndKielistettyParams ++ List(("nayta-hakutoiveet", params.naytaHakutoiveet))
}
