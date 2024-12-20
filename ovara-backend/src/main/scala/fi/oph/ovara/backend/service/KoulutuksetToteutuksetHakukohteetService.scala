package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.repository.{CommonRepository, KoulutuksetToteutuksetHakukohteetRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.Constants.KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter, OrganisaatioUtils}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class KoulutuksetToteutuksetHakukohteetService(
    koulutuksetToteutuksetHakukohteetRepository: KoulutuksetToteutuksetHakukohteetRepository,
    userService: UserService,
    commonRepository: CommonRepository
) {

  @Autowired
  val db: OvaraDatabase = null

  def get(
      alkamiskausi: List[String],
      haku: List[String],
      koulutustoimija: Option[String],
      oppilaitos: List[String],
      toimipiste: List[String],
      koulutuksenTila: Option[String],
      toteutuksenTila: Option[String],
      hakukohteenTila: Option[String],
      valintakoe: Option[Boolean]
  ): XSSFWorkbook = {
    val user                        = userService.getEnrichedUserDetails
    val authorities                 = user.authorities
    val kayttooikeusOrganisaatiot   = AuthoritiesUtil.getOrganisaatiot(authorities)
    val parentChildKayttooikeusOrgs = db.run(commonRepository.selectChildOrganisaatiot(kayttooikeusOrganisaatiot))
    val allUserKayttooikeusOrgOids  = parentChildKayttooikeusOrgs.groupBy(_.parent_oid).keys.toList.distinct
    val kayttooikeusOrgsForKoulutustoimijaSelection = koulutustoimija match {
      case Some(oid) =>
        val childOrganisaatiotForKoulutustoimija = db.run(commonRepository.selectChildOrganisaatiot(List(oid)))
        parentChildKayttooikeusOrgs intersect childOrganisaatiotForKoulutustoimija
      case None => parentChildKayttooikeusOrgs
    }
    val queryResult = db.run(
      koulutuksetToteutuksetHakukohteetRepository.selectWithParams(
        kayttooikeusOrgsForKoulutustoimijaSelection.map(_.parent_oid).toList.distinct,
        alkamiskausi,
        haku,
        koulutuksenTila,
        toteutuksenTila,
        hakukohteenTila,
        valintakoe
      )
    )

    val groupedQueryResult = queryResult.groupBy(_.organisaatio_oid)

    val organisaationKoulutuksetHakukohteetToteutukset =
      OrganisaatioUtils.mapOrganisaationHakukohteetToParent(
        kayttooikeusOrgsForKoulutustoimijaSelection,
        groupedQueryResult
      )

    ExcelWriter.writeRaportti(
      organisaationKoulutuksetHakukohteetToteutukset,
      KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES,
      user
    )
  }
}
