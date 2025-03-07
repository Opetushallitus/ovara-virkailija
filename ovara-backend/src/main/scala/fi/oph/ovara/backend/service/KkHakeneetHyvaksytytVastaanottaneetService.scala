package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{HakeneetHyvaksytytVastaanottaneetResult, KkHakeneetHyvaksytytVastaanottaneetResult}
import fi.oph.ovara.backend.repository.{KkHakeneetHyvaksytytVastaanottaneetRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

@Component
@Service
class KkHakeneetHyvaksytytVastaanottaneetService(
                                                kkHakeneetHyvaksytytVastaanottaneetRepository: KkHakeneetHyvaksytytVastaanottaneetRepository,
                                                userService: UserService,
                                                commonService: CommonService,
                                                lokalisointiService: LokalisointiService
                                              ) {

  @Autowired
  val db: OvaraDatabase = null

  def get(
           haku: List[String],
           tulostustapa: String,
           koulutustoimija: Option[String],
           oppilaitokset: List[String],
           toimipisteet: List[String],
           hakukohteet: List[String],
           sukupuoli: Option[String],
           naytaHakutoiveet: Boolean
         ): XSSFWorkbook = {
    val user = userService.getEnrichedUserDetails
    val asiointikieli = user.asiointikieli.getOrElse("fi")
    val authorities = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getOrganisaatiot(authorities)
    val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

    val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset,
      koulutustoimijaOid = koulutustoimija
    )

    val queryResult = tulostustapa match
      case _ =>
//      case "hakukohteittain" =>
        val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams(
          selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
          haut = haku,
          hakukohteet = hakukohteet,
          sukupuoli = sukupuoli
        )
        db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams").map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))
//      case "toimipisteittain" =>
//        val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams(
//          selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
//          haut = haku,
//          hakukohteet = hakukohteet,
//          sukupuoli = sukupuoli
//        )
//        db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams").map(r => HakeneetHyvaksytytVastaanottaneetResult(r))
//      case _ =>
//        val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectOrganisaatioittainWithParams(
//          selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
//          haut = haku,
//          hakukohteet = hakukohteet,
//          sukupuoli = sukupuoli,
//          organisaatiotaso = tulostustapa
//        )
//        db.run(query, "kkHakeneetHyvaksytytVastaanottaneetRepository.selectOrganisaatioittainWithParams")

    val sumQuery = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams(
      selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
      haut = haku,
      hakukohteet = hakukohteet,
      sukupuoli = sukupuoli
    )

    val sumQueryResult = db.run(sumQuery, "kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams")

    ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli,
      translations,
      queryResult.toList,
      sumQueryResult,
      naytaHakutoiveet,
      tulostustapa
    )
  }

}
