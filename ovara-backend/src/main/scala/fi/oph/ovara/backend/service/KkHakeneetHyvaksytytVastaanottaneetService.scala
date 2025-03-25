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
           hakukohderyhmat: List[String],
           okmOhjauksenAlat: List[String],
           tutkinnonTasot: List[String],
           aidinkielet: List[String],
           kansalaisuudet: List[String],
           sukupuoli: Option[String],
           ensikertalainen: Option[Boolean],
           naytaHakutoiveet: Boolean
         ): XSSFWorkbook = {
    val user = userService.getEnrichedUserDetails
    val asiointikieli = user.asiointikieli.getOrElse("fi")
    val authorities = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)
    val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

    val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset,
      koulutustoimijaOid = koulutustoimija
    )

    val queryResult = tulostustapa match
      case "hauittain" =>
        val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHauittainWithParams(
          selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
          haut = haku,
          hakukohteet = hakukohteet,
          hakukohderyhmat = hakukohderyhmat,
          okmOhjauksenAlat = okmOhjauksenAlat,
          tutkinnonTasot = tutkinnonTasot,
          aidinkielet = aidinkielet,
          kansalaisuudet = kansalaisuudet,
          sukupuoli = sukupuoli,
          ensikertalainen = ensikertalainen
        )
        db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectHauittainWithParams").map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))
      case "hakukohteittain" =>
        val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams(
          selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
          haut = haku,
          hakukohteet = hakukohteet,
          hakukohderyhmat = hakukohderyhmat,
          okmOhjauksenAlat = okmOhjauksenAlat,
          tutkinnonTasot = tutkinnonTasot,
          aidinkielet = aidinkielet,
          kansalaisuudet = kansalaisuudet,
          sukupuoli = sukupuoli,
          ensikertalainen = ensikertalainen
        )
        db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams").map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))
      case "toimipisteittain" =>
        val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams(
          selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
          haut = haku,
          hakukohteet = hakukohteet,
          hakukohderyhmat = hakukohderyhmat,
          okmOhjauksenAlat = okmOhjauksenAlat,
          tutkinnonTasot = tutkinnonTasot,
          aidinkielet = aidinkielet,
          kansalaisuudet = kansalaisuudet,
          sukupuoli = sukupuoli,
          ensikertalainen = ensikertalainen
        )
        db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams").map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))
      case "okm-ohjauksen-aloittain" =>
        val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectOkmOhjauksenAloittainWithParams(
          selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
          haut = haku,
          hakukohteet = hakukohteet,
          hakukohderyhmat = hakukohderyhmat,
          okmOhjauksenAlat = okmOhjauksenAlat,
          tutkinnonTasot = tutkinnonTasot,
          aidinkielet = aidinkielet,
          kansalaisuudet = kansalaisuudet,
          sukupuoli = sukupuoli,
          ensikertalainen = ensikertalainen
        )
        db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectOkmOhjauksenAloittainWithParams")
      case "kansalaisuuksittain" =>
        val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectKansalaisuuksittainWithParams(
          selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
          haut = haku,
          hakukohteet = hakukohteet,
          hakukohderyhmat = hakukohderyhmat,
          okmOhjauksenAlat = okmOhjauksenAlat,
          tutkinnonTasot = tutkinnonTasot,
          aidinkielet = aidinkielet,
          kansalaisuudet = kansalaisuudet,
          sukupuoli = sukupuoli,
          ensikertalainen = ensikertalainen
        )
        db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectKansalaisuuksittainWithParams")
      case "hakukohderyhmittain" =>
        val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakukohderyhmittainWithParams2(
          selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
          haut = haku,
          hakukohteet = hakukohteet,
          hakukohderyhmat = hakukohderyhmat,
          okmOhjauksenAlat = okmOhjauksenAlat,
          tutkinnonTasot = tutkinnonTasot,
          aidinkielet = aidinkielet,
          kansalaisuudet = kansalaisuudet,
          sukupuoli = sukupuoli,
          ensikertalainen = ensikertalainen
        )
        db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectHakukohderyhmittainWithParams")
      case _ =>
        val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectOrganisaatioittainWithParams(
          selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
          haut = haku,
          hakukohteet = hakukohteet,
          hakukohderyhmat = hakukohderyhmat,
          okmOhjauksenAlat = okmOhjauksenAlat,
          tutkinnonTasot = tutkinnonTasot,
          aidinkielet = aidinkielet,
          kansalaisuudet = kansalaisuudet,
          sukupuoli = sukupuoli,
          ensikertalainen = ensikertalainen,
          organisaatiotaso = tulostustapa
        )
        db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectOrganisaatioittainWithParams")

    val vainEnsikertalaiset = ensikertalainen.getOrElse(false)
    val sumQuery = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams(
      selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
      haut = haku,
      hakukohteet = hakukohteet,
      hakukohderyhmat = hakukohderyhmat,
      okmOhjauksenAlat = okmOhjauksenAlat,
      tutkinnonTasot = tutkinnonTasot,
      aidinkielet = aidinkielet,
      kansalaisuudet = kansalaisuudet,
      sukupuoli = sukupuoli,
      ensikertalainen = ensikertalainen
    )

    val ensikertalaisetSumQuery = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams(
      selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
      haut = haku,
      hakukohteet = hakukohteet,
      hakukohderyhmat = hakukohderyhmat,
      okmOhjauksenAlat = okmOhjauksenAlat,
      tutkinnonTasot = tutkinnonTasot,
      aidinkielet = aidinkielet,
      kansalaisuudet = kansalaisuudet,
      sukupuoli = sukupuoli,
      ensikertalainen = Some(true)
    )

    val maksuvelvollisetSumQuery = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams(
      selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
      haut = haku,
      hakukohteet = hakukohteet,
      hakukohderyhmat = hakukohderyhmat,
      okmOhjauksenAlat = okmOhjauksenAlat,
      tutkinnonTasot = tutkinnonTasot,
      aidinkielet = aidinkielet,
      kansalaisuudet = kansalaisuudet,
      sukupuoli = sukupuoli,
      ensikertalainen = ensikertalainen,
      maksuvelvollinen = Some("obligated")
    )

    val sumQueryResult = db.run(sumQuery, "kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams")
    val ensikertalaisetSumQueryResult =
      if (vainEnsikertalaiset)
        sumQueryResult
      else
        db.run(ensikertalaisetSumQuery, "kkHakeneetHyvaksytytVastaanottaneetRepository.selectEnsikertalaisetHakijatYhteensaWithParams")
    val maksuvelvollisetSumQueryResult = db.run(maksuvelvollisetSumQuery, "kkHakeneetHyvaksytytVastaanottaneetRepository.selectMaksuvelvollisetHakijatYhteensaWithParams")
    ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli,
      translations,
      queryResult.toList,
      sumQueryResult,
      ensikertalaisetSumQueryResult,
      maksuvelvollisetSumQueryResult,
      naytaHakutoiveet,
      tulostustapa
    )
  }

}
