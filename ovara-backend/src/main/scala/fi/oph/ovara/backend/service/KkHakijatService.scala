package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.KkHakijaWithCombinedNimi
import fi.oph.ovara.backend.repository.{KkHakijatRepository, ReadOnlyDatabase}
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
  val db: ReadOnlyDatabase = null

  def get(
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
  ): XSSFWorkbook = {
    val user          = userService.getEnrichedUserDetails
    val asiointikieli = user.asiointikieli.getOrElse("fi")

    val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)
    val isOphPaakayttaja          = AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)

    val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset
    )

    val allowedHakukohderyhmat = if (isOphPaakayttaja) {
      hakukohderyhmat
    } else {
      kayttooikeusOrganisaatiot intersect hakukohderyhmat
    }

    val query = kkHakijatRepository.selectWithParams(
      kayttooikeusOrganisaatiot = orgOidsForQuery,
      hakukohderyhmat = allowedHakukohderyhmat,
      haut = haut,
      oppilaitokset = oppilaitokset,
      toimipisteet = toimipisteet,
      hakukohteet = hakukohteet,
      valintatiedot = valintatiedot,
      vastaanottotiedot = vastaanottotiedot,
      kansalaisuusluokat = kansalaisuusluokat,
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
