package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.ToisenAsteenHakijaWithCombinedNimi
import fi.oph.ovara.backend.repository.{OvaraDatabase, ToisenAsteenHakijatRepository}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

@Component
@Service
class ToisenAsteenHakijatService(
    toisenAsteenHakijatRepository: ToisenAsteenHakijatRepository,
    userService: UserService,
    commonService: CommonService,
    lokalisointiService: LokalisointiService
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ToisenAsteenHakijatService]);

  @Autowired
  val db: OvaraDatabase = null

  def get(
      haku: List[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      hakukohteet: List[String],
      pohjakoulutukset: List[String],
      valintatieto: List[String],
      vastaanottotieto: List[String],
      harkinnanvaraisuudet: List[String],
      kaksoistutkintoKiinnostaa: Option[Boolean],
      urheilijatutkintoKiinnostaa: Option[Boolean],
      soraTerveys: Option[Boolean],
      soraAiempi: Option[Boolean],
      markkinointilupa: Option[Boolean],
      julkaisulupa: Option[Boolean]
  ): XSSFWorkbook = {
    val user          = userService.getEnrichedUserDetails
    val asiointikieli = user.asiointikieli.getOrElse("fi")

    val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)

    val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset
    )

    val query = toisenAsteenHakijatRepository.selectWithParams(
      kayttooikeusOrganisaatiot = orgOidsForQuery,
      haut = haku,
      oppilaitokset = oppilaitokset,
      toimipisteet = toimipisteet,
      hakukohteet = hakukohteet,
      pohjakoulutukset = pohjakoulutukset,
      valintatieto = valintatieto,
      vastaanottotieto = vastaanottotieto,
      harkinnanvaraisuudet = harkinnanvaraisuudet,
      kaksoistutkintoKiinnostaa = kaksoistutkintoKiinnostaa,
      urheilijatutkintoKiinnostaa = urheilijatutkintoKiinnostaa,
      soraTerveys = soraTerveys,
      soraAiempi = soraAiempi,
      markkinointilupa = markkinointilupa,
      julkaisulupa = julkaisulupa
    )

    val queryResult = db.run(query, "toisenAsteenHakijatRepository.selectWithParams")
    val sorted =
      queryResult.sortBy(resultRow => (resultRow.hakijanSukunimi, resultRow.hakijanEtunimi, resultRow.oppijanumero))
    val sortedListwithCombinedNimi = sorted.map(sortedResult => ToisenAsteenHakijaWithCombinedNimi(sortedResult))

    ExcelWriter.writeToisenAsteenHakijatRaportti(
      sortedListwithCombinedNimi,
      asiointikieli,
      translations,
      "toinen aste"
    )
  }
}
