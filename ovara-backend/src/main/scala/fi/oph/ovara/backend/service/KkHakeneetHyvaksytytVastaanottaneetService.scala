package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{Kieli, KkHakeneetHyvaksytytVastaanottaneetHakukohteittain, KkHakeneetHyvaksytytVastaanottaneetResult}
import fi.oph.ovara.backend.raportointi.dto.{ValidatedKkHakeneetHyvaksytytVastaanottaneetParams, buildKkHakeneetHyvaksytytVastaanottaneetParamsForExcel}
import fi.oph.ovara.backend.repository.{KkHakeneetHyvaksytytVastaanottaneetRepository, ReadOnlyDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

import java.text.Collator
import java.util.Locale
import scala.util.{Failure, Success, Try}

@Component
@Service
class KkHakeneetHyvaksytytVastaanottaneetService(
                                                  kkHakeneetHyvaksytytVastaanottaneetRepository: KkHakeneetHyvaksytytVastaanottaneetRepository,
                                                  userService: UserService,
                                                  commonService: CommonService,
                                                  lokalisointiService: LokalisointiService
                                                ) {

  val LOG: Logger = LoggerFactory.getLogger(classOf[KkHakeneetHyvaksytytVastaanottaneetService])
  @Autowired
  val db: ReadOnlyDatabase = null

  def get(
           haut: List[String],
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
           naytaHakutoiveet: Boolean,
         ): Either[String, XSSFWorkbook] = {
    Try {
      val user = userService.getEnrichedUserDetails
      val asiointikieli = user.asiointikieli.getOrElse("fi")
      val authorities = user.authorities
      val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)
      val kayttooikeusHakukohderyhmat = AuthoritiesUtil.filterHakukohderyhmaOids(kayttooikeusOrganisaatiot)
      val isOphPaakayttaja = AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)
      val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

      val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
        kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
        toimipisteOids = toimipisteet,
        oppilaitosOids = oppilaitokset,
        koulutustoimijaOid = koulutustoimija
      )
      val isOrganisaatioRajain =  (koulutustoimija.isDefined || oppilaitokset.nonEmpty || toimipisteet.nonEmpty) && orgOidsForQuery.nonEmpty
      val queryResult = tulostustapa match
        case "hauittain" =>
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHauittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              isOrganisaatioRajain = isOrganisaatioRajain,
              isOphPaakayttaja = isOphPaakayttaja,
              kayttooikeusHakukohderyhmat = kayttooikeusHakukohderyhmat,
              haut = haut,
              hakukohteet = hakukohteet,
              hakukohderyhmat = hakukohderyhmat,
              okmOhjauksenAlat = okmOhjauksenAlat,
              tutkinnonTasot = tutkinnonTasot,
              aidinkielet = aidinkielet,
              kansalaisuudet = kansalaisuudet,
              sukupuoli = sukupuoli,
              ensikertalainen = ensikertalainen
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectHauittainWithParams")
              .map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))

        case "hakukohteittain" =>
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              isOrganisaatioRajain = isOrganisaatioRajain,
              isOphPaakayttaja = isOphPaakayttaja,
              kayttooikeusHakukohderyhmat = kayttooikeusHakukohderyhmat,
              haut = haut,
              hakukohteet = hakukohteet,
              hakukohderyhmat = hakukohderyhmat,
              okmOhjauksenAlat = okmOhjauksenAlat,
              tutkinnonTasot = tutkinnonTasot,
              aidinkielet = aidinkielet,
              kansalaisuudet = kansalaisuudet,
              sukupuoli = sukupuoli,
              ensikertalainen = ensikertalainen
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams")
        case "toimipisteittain" =>
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              isOrganisaatioRajain = isOrganisaatioRajain,
              isOphPaakayttaja = isOphPaakayttaja,
              kayttooikeusHakukohderyhmat = kayttooikeusHakukohderyhmat,
              haut = haut,
              hakukohteet = hakukohteet,
              hakukohderyhmat = hakukohderyhmat,
              okmOhjauksenAlat = okmOhjauksenAlat,
              tutkinnonTasot = tutkinnonTasot,
              aidinkielet = aidinkielet,
              kansalaisuudet = kansalaisuudet,
              sukupuoli = sukupuoli,
              ensikertalainen = ensikertalainen
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams")
              .map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))
        case "okm-ohjauksen-aloittain" =>
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectOkmOhjauksenAloittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              isOrganisaatioRajain = isOrganisaatioRajain,
              isOphPaakayttaja = isOphPaakayttaja,
              kayttooikeusHakukohderyhmat = kayttooikeusHakukohderyhmat,
              haut = haut,
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
              .map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))
        case "kansalaisuuksittain" =>
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectKansalaisuuksittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              isOrganisaatioRajain = isOrganisaatioRajain,
              isOphPaakayttaja = isOphPaakayttaja,
              kayttooikeusHakukohderyhmat = kayttooikeusHakukohderyhmat,
              haut = haut,
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
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakukohderyhmittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              isOrganisaatioRajain = isOrganisaatioRajain,
              isOphPaakayttaja = isOphPaakayttaja,
              kayttooikeusHakukohderyhmat = kayttooikeusHakukohderyhmat,
              haut = haut,
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
              .map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))
        case _ =>
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectOrganisaatioittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              isOrganisaatioRajain = isOrganisaatioRajain,
              isOphPaakayttaja = isOphPaakayttaja,
              kayttooikeusHakukohderyhmat = kayttooikeusHakukohderyhmat,
              haut = haut,
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
              .map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))


      val vainEnsikertalaiset = ensikertalainen.getOrElse(false)
      val sumQuery = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams(
        selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
        isOrganisaatioRajain = isOrganisaatioRajain,
        isOphPaakayttaja = isOphPaakayttaja,
        kayttooikeusHakukohderyhmat = kayttooikeusHakukohderyhmat,
        haut = haut,
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
        isOrganisaatioRajain = isOrganisaatioRajain,
        isOphPaakayttaja = isOphPaakayttaja,
        kayttooikeusHakukohderyhmat = kayttooikeusHakukohderyhmat,
        haut = haut,
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
        isOrganisaatioRajain = isOrganisaatioRajain,
        isOphPaakayttaja = isOphPaakayttaja,
        kayttooikeusHakukohderyhmat = kayttooikeusHakukohderyhmat,
        haut = haut,
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

      val collator: Collator = Collator.getInstance(new Locale(asiointikieli))
      collator.setStrength(Collator.PRIMARY)
      val sortedResult = queryResult.sortWith {
        case (a: KkHakeneetHyvaksytytVastaanottaneetHakukohteittain, b: KkHakeneetHyvaksytytVastaanottaneetHakukohteittain) =>
          val primaryComparison = collator.compare(
            a.hakukohdeNimi.getOrElse(Kieli.withName(asiointikieli), ""),
            b.hakukohdeNimi.getOrElse(Kieli.withName(asiointikieli), "")
          )
          if (primaryComparison == 0) {
            collator.compare(
              a.organisaatioNimi.getOrElse(Kieli.withName(asiointikieli), ""),
              b.organisaatioNimi.getOrElse(Kieli.withName(asiointikieli), "")
            ) < 0
          } else {
            primaryComparison < 0
          }
        case (a: KkHakeneetHyvaksytytVastaanottaneetResult, b: KkHakeneetHyvaksytytVastaanottaneetResult) =>
          collator.compare(a.otsikko.getOrElse(Kieli.withName(asiointikieli), ""), b.otsikko.getOrElse(Kieli.withName(asiointikieli), "")) < 0
      }
      val sumQueryResult = db.run(sumQuery, "kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams")
      val ensikertalaisetSumQueryResult =
        if (vainEnsikertalaiset)
          sumQueryResult
        else
          db.run(ensikertalaisetSumQuery, "kkHakeneetHyvaksytytVastaanottaneetRepository.selectEnsikertalaisetHakijatYhteensaWithParams")
      val maksuvelvollisetSumQueryResult = db.run(maksuvelvollisetSumQuery, "kkHakeneetHyvaksytytVastaanottaneetRepository.selectMaksuvelvollisetHakijatYhteensaWithParams")

      val raporttiParamNames = db.run(
        kkHakeneetHyvaksytytVastaanottaneetRepository.hakuParamNamesQuery(
          haut = haut,
          koulutustoimija = koulutustoimija,
          oppilaitokset = oppilaitokset,
          toimipisteet = toimipisteet,
          hakukohteet = hakukohteet,
          hakukohderyhmat = hakukohderyhmat,
          okmOhjauksenAlat = okmOhjauksenAlat,
          sukupuoli = sukupuoli,
        ),
        "hakuParamNamesQuery"
      ).map(param => param.parametri -> param.nimet).toMap

      val raporttiParams = buildKkHakeneetHyvaksytytVastaanottaneetParamsForExcel(
        ValidatedKkHakeneetHyvaksytytVastaanottaneetParams(
          haut = haut,
          tulostustapa = tulostustapa,
          koulutustoimija = koulutustoimija,
          oppilaitokset = oppilaitokset,
          toimipisteet = toimipisteet,
          hakukohteet = hakukohteet,
          hakukohderyhmat = hakukohderyhmat,
          okmOhjauksenAlat = okmOhjauksenAlat,
          tutkinnonTasot = tutkinnonTasot,
          aidinkielet = aidinkielet,
          kansalaisuusluokat = kansalaisuudet,
          sukupuoli = sukupuoli,
          ensikertalainen = ensikertalainen,
          naytaHakutoiveet = naytaHakutoiveet
        ), raporttiParamNames
      )
      
      ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
        asiointikieli,
        translations,
        sortedResult.toList,
        sumQueryResult,
        ensikertalaisetSumQueryResult,
        maksuvelvollisetSumQueryResult,
        naytaHakutoiveet,
        tulostustapa,
        raporttiParams
      )
    } match {
      case Success(excelFile) => Right(excelFile)
      case Failure(exception) =>
        LOG.error("Error generating Excel report", exception)
        Left("virhe.tietokanta")
    }
  }

}
