package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.repository.{CommonRepository, KoulutuksetToteutuksetHakukohteetRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.Constants.{KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, OPH_PAAKAYTTAJA_OID}
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

    val hierarkiat =
      if (toimipisteet.nonEmpty) {
        db.run(
          commonRepository.selectToimipisteDescendants(toimipisteet),
          "selectToimipisteDescendants"
        ).toList
      } else if (oppilaitokset.nonEmpty) {
        db.run(
          commonRepository.selectOppilaitosDescendants(oppilaitokset),
          "selectOppilaitosDescendants"
        ).toList
      } else if (koulutustoimija.nonEmpty) {
        koulutustoimija match {
          case Some(koulutustoimija) =>
            db.run(
              commonRepository.selectKoulutustoimijaDescendants(List(koulutustoimija)),
              "selectKoulutustoimijaDescendants"
            ).toList
          case None => List()
        }
      } else {
        val koulutustoimijahierarkia = db
          .run(
            commonRepository.selectKoulutustoimijaDescendants(kayttooikeusOrganisaatiot),
            "selectKoulutustoimijaDescendants"
          )
          .toList

        if (hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)) {
          koulutustoimijahierarkia
        } else {
          val oppilaitoshierarkia = db.run(
            commonRepository.selectOppilaitosDescendants(kayttooikeusOrganisaatiot),
            "selectOppilaitosDescendants"
          )

          val toimipistehierarkia = db.run(
            commonRepository.selectToimipisteDescendants(kayttooikeusOrganisaatiot),
            "selectToimipisteDescendants"
          )

          koulutustoimijahierarkia concat oppilaitoshierarkia concat toimipistehierarkia
        }
      }

    val selectedOrgsDescendantOids =
      hierarkiat.flatMap(hierarkia => OrganisaatioUtils.getDescendantOids(hierarkia)).distinct

    val orgOidsForQuery = if (hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)) {
      selectedOrgsDescendantOids
    } else {
      val childKayttooikeusOrgs = hierarkiat.flatMap(hierarkia =>
        OrganisaatioUtils.getKayttooikeusDescendantOids(hierarkia, kayttooikeusOrganisaatiot)
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
      userLng
    )
  }
}
