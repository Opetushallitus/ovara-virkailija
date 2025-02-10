package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.utils.Constants.HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_TITLES
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component

@Component
class HakeneetHyvaksytytVastaanottaneetService(
                                                userService: UserService,
                                                commonService: CommonService,
                                                lokalisointiService: LokalisointiService
                                              ) {

  def get(
           haku: List[String],
           koulutustoimija: Option[String],
           oppilaitokset: List[String],
           toimipisteet: List[String],
           hakukohteet: List[String],
           opetuskielet: List[String],
           harkinnanvaraisuudet: List[String],
           naytaHakutoiveet: Boolean,
           sukupuoli: Option[Boolean]
         ): XSSFWorkbook = {
    val user = userService.getEnrichedUserDetails
    val asiointikieli = user.asiointikieli.getOrElse("fi")
    val authorities = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getOrganisaatiot(authorities)
    val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

    val orgOidsForQuery = commonService.getAllowedOrgsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset
    )

    ExcelWriter.writeHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli,
      translations
    )
  }

}
