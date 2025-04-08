package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.Kieli
import fi.oph.ovara.backend.repository.{KorkeakouluKoulutuksetToteutuksetHakukohteetRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

@Component
@Service
class KorkeakouluKoulutuksetToteutuksetHakukohteetService(
    korkeakouluKoulutuksetToteutuksetHakukohteetRepository: KorkeakouluKoulutuksetToteutuksetHakukohteetRepository,
    userService: UserService,
    commonService: CommonService,
    lokalisointiService: LokalisointiService
) {

  @Autowired
  val db: OvaraDatabase = null

  def get(
      haku: List[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      hakukohderyhmat: List[String],
      koulutuksenTila: Option[String],
      toteutuksenTila: Option[String],
      hakukohteenTila: Option[String],
      tutkinnonTasot: List[String],
      tulostustapa: String
  ): XSSFWorkbook = {
    val user                      = userService.getEnrichedUserDetails
    val asiointikieli             = user.asiointikieli.getOrElse("fi")
    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)
    val isOphPaakayttaja = AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)

    val translations              = lokalisointiService.getOvaraTranslations(asiointikieli)

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

    val queryResult = db.run(
      korkeakouluKoulutuksetToteutuksetHakukohteetRepository.selectWithParams(
        orgOidsForQuery,
        allowedHakukohderyhmat,
        haku,
        koulutuksenTila,
        toteutuksenTila,
        hakukohteenTila,
        tutkinnonTasot
      ),
      "selectWithParams"
    )

    ExcelWriter.writeKorkeakouluKoulutuksetToteutuksetHakukohteetRaportti(
      queryResult,
      asiointikieli,
      translations,
      tulostustapa
    )
  }
}
