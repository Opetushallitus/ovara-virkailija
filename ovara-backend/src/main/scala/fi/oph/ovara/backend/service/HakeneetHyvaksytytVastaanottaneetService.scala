package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.repository.{HakeneetHyvaksytytVastaanottaneetRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.Constants.HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_TITLES
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

@Component
@Service
class HakeneetHyvaksytytVastaanottaneetService(
                                                hakeneetHyvaksytytVastaanottaneetRepository: HakeneetHyvaksytytVastaanottaneetRepository,
                                                userService: UserService,
                                                commonService: CommonService,
                                                lokalisointiService: LokalisointiService
                                              ) {

  @Autowired
  val db: OvaraDatabase = null
  
  def get(
           haku: List[String],
           koulutustoimija: Option[String],
           oppilaitokset: List[String],
           toimipisteet: List[String],
           hakukohteet: List[String],
           opetuskielet: List[String],
           harkinnanvaraisuudet: List[String],
           sukupuoli: Option[String],
           naytaHakutoiveet: Boolean
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

    val query = hakeneetHyvaksytytVastaanottaneetRepository.selectWithParams(
      selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
      haut = haku,
      hakukohteet = hakukohteet,
      opetuskielet = opetuskielet,
      harkinnanvaraisuudet = harkinnanvaraisuudet,
      sukupuoli = sukupuoli
    )

    val queryResult = db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectWithParams")
    
    ExcelWriter.writeHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli,
      translations,
      queryResult.toList
    )
  }

}
