package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.OrganisaatioHierarkia
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
    commonService: CommonService
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
    val userLng                   = user.asiointikieli.getOrElse("fi")
    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getOrganisaatiot(authorities)

    def hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot: List[String]) = {
      kayttooikeusOrganisaatiot.contains(OPH_PAAKAYTTAJA_OID)
    }

    def enrichHierarkiatWithKoulutustoimijaParent(oppilaitoshierarkiat: List[OrganisaatioHierarkia]) = {
      for (hierarkia <- oppilaitoshierarkiat) yield {
        val parentOids            = hierarkia.parent_oids
        val parentKoulutustoimija = commonService.getDistinctKoulutustoimijat(parentOids).headOption
        OrganisaatioUtils.addKoulutustoimijaParentToHierarkiaDescendants(hierarkia, parentKoulutustoimija)
      }
    }

    val (hierarkiat, raporttityyppi) =
      if (toimipisteet.nonEmpty) {
        val toimipistehierarkiat = commonService.getToimipistehierarkiat(toimipisteet)
        (enrichHierarkiatWithKoulutustoimijaParent(toimipistehierarkiat), TOIMIPISTERAPORTTI)
      } else if (oppilaitokset.nonEmpty) {
        val oppilaitoshierarkiat = commonService.getOppilaitoshierarkiat(oppilaitokset)
        (enrichHierarkiatWithKoulutustoimijaParent(oppilaitoshierarkiat), OPPILAITOSRAPORTTI)
      } else if (koulutustoimija.nonEmpty) {
        val hierarkiat = koulutustoimija match {
          case Some(koulutustoimija) =>
            commonService.getKoulutustoimijahierarkia(List(koulutustoimija))
          case None => List()
        }
        (hierarkiat, KOULUTUSTOIMIJARAPORTTI)
      } else {
        val koulutustoimijahierarkia = commonService.getKoulutustoimijahierarkia(kayttooikeusOrganisaatiot)

        val hierarkiat = if (hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)) {
          koulutustoimijahierarkia
        } else {
          val oppilaitoshierarkia = commonService.getOppilaitoshierarkiat(oppilaitokset)

          val toimipistehierarkia = commonService.getToimipistehierarkiat(toimipisteet)

          koulutustoimijahierarkia concat oppilaitoshierarkia concat toimipistehierarkia
        }
        (hierarkiat, "koulutustoimijaraportti")
      }

    val hierarkiatWithExistingOrgs = hierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterExistingOrgs(hierarkia))

    val selectedOrgsDescendantOids =
      hierarkiat.flatMap(hierarkia => OrganisaatioUtils.getDescendantOids(hierarkia)).distinct

    val orgOidsForQuery = if (hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)) {
      selectedOrgsDescendantOids
    } else {
      val childKayttooikeusOrgs = hierarkiat.flatMap(hierarkia =>
        OrganisaatioUtils.getKayttooikeusDescendantAndSelfOids(hierarkia, kayttooikeusOrganisaatiot)
      )
      (childKayttooikeusOrgs intersect selectedOrgsDescendantOids).distinct
    }

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
      userLng,
      raporttityyppi
    )
  }
}
