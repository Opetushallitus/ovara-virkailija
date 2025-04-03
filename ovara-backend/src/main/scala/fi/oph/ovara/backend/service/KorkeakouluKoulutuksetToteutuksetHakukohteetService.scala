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
      valintakoe: Option[Boolean]
  ): XSSFWorkbook = {
    val user                      = userService.getEnrichedUserDetails
    val asiointikieli             = user.asiointikieli.getOrElse("fi")
    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)
    val translations              = lokalisointiService.getOvaraTranslations(asiointikieli)

    val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset
    )

    val allowedHakukohderyhmat = kayttooikeusOrganisaatiot intersect hakukohderyhmat

    val queryResult = db.run(
      korkeakouluKoulutuksetToteutuksetHakukohteetRepository.selectWithParams(
        orgOidsForQuery,
        allowedHakukohderyhmat,
        haku,
        koulutuksenTila,
        toteutuksenTila,
        hakukohteenTila
      ),
      "selectWithParams"
    )

    val sorted =
      queryResult.sortBy(resultRow =>
        (
          resultRow.oppilaitosJaToimipiste.get(Kieli.withName(asiointikieli)),
          resultRow.koulutuksenNimi.get(Kieli.withName(asiointikieli))
        )
      )

    ExcelWriter.writeKorkeakouluKoulutuksetToteutuksetHakukohteetRaportti(
      sorted,
      asiointikieli,
      translations
    )
  }
}
