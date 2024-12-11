package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.repository.{KoulutuksetToteutuksetHakukohteetRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class KoulutuksetToteutuksetHakukohteetService(
    koulutuksetToteutuksetHakukohteetRepository: KoulutuksetToteutuksetHakukohteetRepository,
    userService: UserService) {

  @Autowired
  val db: OvaraDatabase = null

  def get(
      alkamiskausi: List[String],
      haku: List[String],
      koulutuksenTila: Option[String],
      toteutuksenTila: Option[String],
      hakukohteenTila: Option[String],
      valintakoe: Option[Boolean]
  ): XSSFWorkbook = {
    val user = userService.getEnrichedUserDetails
    val authorities              = user.authorities
    val raportointiOrganisaatiot = AuthoritiesUtil.getOrganisaatiot(authorities)
    val res = db.run(
      koulutuksetToteutuksetHakukohteetRepository.selectWithParams(
        raportointiOrganisaatiot,
        alkamiskausi,
        haku,
        koulutuksenTila,
        toteutuksenTila,
        hakukohteenTila,
        valintakoe
      )
    )

    ExcelWriter.writeRaportti(res, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, user)
  }
}
