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
    val user                      = userService.getEnrichedUserDetails
    val userLng                   = user.asiointikieli.getOrElse("fi")
    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getOrganisaatiot(authorities)
    val parentChildKayttooikeusOrgs =
      db.run(commonRepository.selectChildOrganisaatiot(kayttooikeusOrganisaatiot), "selectChildOrganisaatiot")

    val hierarkiat = koulutustoimija match {
      case Some(koulutustoimija) =>
        db.run(commonRepository.selectKoulutustoimijaDescendants(koulutustoimija), "selectKoulutustoimijaDescendants")
          .toList
      case None => List()
    }

    val descendantOids = hierarkiat.flatMap(hierarkia => OrganisaatioUtils.getDescendantOids(hierarkia))
    val orgOids = parentChildKayttooikeusOrgs.map(_.child_oid).toList intersect descendantOids

    val queryResult = db.run(
      koulutuksetToteutuksetHakukohteetRepository.selectWithParams(
        orgOids,
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
      userLng
    )
  }
}
