package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.repository.{KoulutuksetToteutuksetHakukohteetRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.Constants.*
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter, OrganisaatioUtils}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class KoulutuksetToteutuksetHakukohteetService(
    koulutuksetToteutuksetHakukohteetRepository: KoulutuksetToteutuksetHakukohteetRepository,
    userService: UserService,
    commonService: CommonService,
    lokalisointiService: LokalisointiService
) {

  @Autowired
  val db: OvaraDatabase = null

  def get(
      alkamiskausi: List[String],
      haku: List[String],
      koulutustoimija: Option[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      koulutuksenTila: Option[String],
      toteutuksenTila: Option[String],
      hakukohteenTila: Option[String],
      valintakoe: Option[Boolean]
  ): XSSFWorkbook = {
    val user                      = userService.getEnrichedUserDetails
    val asiointikieli             = user.asiointikieli.getOrElse("fi")
    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getOrganisaatiot(authorities)
    val translations              = lokalisointiService.getOvaraTranslations(asiointikieli)

    val (orgOidsForQuery, hierarkiat, raporttityyppi) = commonService.getAllowedOrgsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      koulutustoimijaOid = koulutustoimija,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset
    )

    val queryResult = db.run(
      koulutuksetToteutuksetHakukohteetRepository.selectWithParams(
        orgOidsForQuery,
        alkamiskausi,
        haku,
        koulutuksenTila,
        toteutuksenTila,
        hakukohteenTila,
        valintakoe
      ),
      "selectWithParams"
    )

    val groupedQueryResult = queryResult.groupBy(_.organisaatio_oid)

    val organisaationKoulutuksetHakukohteetToteutukset =
      OrganisaatioUtils.mapOrganisaationHakukohteetToParents(
        hierarkiat,
        groupedQueryResult
      )

    ExcelWriter.writeRaportti(
      organisaationKoulutuksetHakukohteetToteutukset,
      KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES,
      asiointikieli,
      raporttityyppi,
      translations
    )
  }
}
