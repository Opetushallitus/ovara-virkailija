package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.KkHakijaWithCombinedNimi
import fi.oph.ovara.backend.repository.{KkHakijatRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.Constants.KORKEAKOULURAPORTTI
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

@Component
@Service
class KkHakijatService(
    kkHakijatRepository: KkHakijatRepository,
    userService: UserService,
    commonService: CommonService,
    lokalisointiService: LokalisointiService
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[KkHakijatService]);

  @Autowired
  val db: OvaraDatabase = null

  def get(
      haku: List[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      hakukohteet: List[String],
      valintatieto: List[String],
      vastaanottotieto: List[String],
      hakukohderyhmat: List[String],
      kansalaisuus: List[String],
      markkinointilupa: Option[Boolean],
      naytaYoArvosanat: Boolean,
      naytaHetu: Boolean,
      naytaPostiosoite: Boolean
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

    val query = kkHakijatRepository.selectWithParams(
      kayttooikeusOrganisaatiot = orgOidsForQuery,
      hakukohderyhmat = hakukohderyhmat,
      haut = haku,
      oppilaitokset = oppilaitokset,
      toimipisteet = toimipisteet,
      hakukohteet = hakukohteet,
      valintatieto = valintatieto,
      vastaanottotieto = vastaanottotieto,
      kansalaisuus = kansalaisuus,
      markkinointilupa = markkinointilupa
    )

    val queryResult = db.run(query, "kkHakijatRepository.selectWithParams")
    val sorted =
      queryResult.sortBy(resultRow => (resultRow.hakijanSukunimi, resultRow.hakijanEtunimi, resultRow.oppijanumero))
    val sortedListwithCombinedNimi = sorted.map(sortedResult => KkHakijaWithCombinedNimi(sortedResult))

    ExcelWriter.writeKkHakijatRaportti(
      sortedListwithCombinedNimi,
      asiointikieli,
      translations,
      Some(naytaYoArvosanat),
      Some(naytaHetu),
      Some(naytaPostiosoite)
    )
  }
}
