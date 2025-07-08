package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{Kieli, KkHakeneetHyvaksytytVastaanottaneetHakukohteittain, KkHakeneetHyvaksytytVastaanottaneetResult}
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
           uusiTilasto: Boolean
         ): Either[String, XSSFWorkbook] = {
    Try {
      val user = userService.getEnrichedUserDetails
      val asiointikieli = user.asiointikieli.getOrElse("fi")
      val authorities = user.authorities
      val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)
      val translations = lokalisointiService.getOvaraTranslations(asiointikieli)
      val isOphPaakayttaja = AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)

      val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
        kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
        toimipisteOids = toimipisteet,
        oppilaitosOids = oppilaitokset,
        koulutustoimijaOid = koulutustoimija
      )

      val hakukohderyhmarajaus =
        if (orgOidsForQuery.isEmpty && hakukohderyhmat.isEmpty && !isOphPaakayttaja) {
          // pakotetaan käyttöoikeuksien mukainen hakukohderyhmärajaus jos ei ole mitään organisaatiorajausta eikä ole pääkäyttäjä
          kayttooikeusOrganisaatiot
        } else
          hakukohderyhmat

      val useFixedQuery = uusiTilasto && AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)
      val queryResult = tulostustapa match
        case "hauittain" =>
          if (useFixedQuery) {
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHauittainWithParams2(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              haut = haut,
              hakukohteet = hakukohteet,
              hakukohderyhmat = hakukohderyhmarajaus,
              okmOhjauksenAlat = okmOhjauksenAlat,
              tutkinnonTasot = tutkinnonTasot,
              aidinkielet = aidinkielet,
              kansalaisuudet = kansalaisuudet,
              sukupuoli = sukupuoli,
              ensikertalainen = ensikertalainen
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectHauittainWithParams2")
              .map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))
          } else {
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHauittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              haut = haut,
              hakukohteet = hakukohteet,
              hakukohderyhmat = hakukohderyhmarajaus,
              okmOhjauksenAlat = okmOhjauksenAlat,
              tutkinnonTasot = tutkinnonTasot,
              aidinkielet = aidinkielet,
              kansalaisuudet = kansalaisuudet,
              sukupuoli = sukupuoli,
              ensikertalainen = ensikertalainen
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectHauittainWithParams")
              .map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))
          }
        case "hakukohteittain" =>
          if (useFixedQuery) {
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams2(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              haut = haut,
              hakukohteet = hakukohteet,
              hakukohderyhmat = hakukohderyhmarajaus,
              okmOhjauksenAlat = okmOhjauksenAlat,
              tutkinnonTasot = tutkinnonTasot,
              aidinkielet = aidinkielet,
              kansalaisuudet = kansalaisuudet,
              sukupuoli = sukupuoli,
              ensikertalainen = ensikertalainen
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams")
          } else {
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              haut = haut,
              hakukohteet = hakukohteet,
              hakukohderyhmat = hakukohderyhmarajaus,
              okmOhjauksenAlat = okmOhjauksenAlat,
              tutkinnonTasot = tutkinnonTasot,
              aidinkielet = aidinkielet,
              kansalaisuudet = kansalaisuudet,
              sukupuoli = sukupuoli,
              ensikertalainen = ensikertalainen
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams")
          }
        case "toimipisteittain" =>
          if (useFixedQuery) {
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams2(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              haut = haut,
              hakukohteet = hakukohteet,
              hakukohderyhmat = hakukohderyhmarajaus,
              okmOhjauksenAlat = okmOhjauksenAlat,
              tutkinnonTasot = tutkinnonTasot,
              aidinkielet = aidinkielet,
              kansalaisuudet = kansalaisuudet,
              sukupuoli = sukupuoli,
              ensikertalainen = ensikertalainen
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams2")
              .map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))
          } else {
            val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              haut = haut,
              hakukohteet = hakukohteet,
              hakukohderyhmat = hakukohderyhmarajaus,
              okmOhjauksenAlat = okmOhjauksenAlat,
              tutkinnonTasot = tutkinnonTasot,
              aidinkielet = aidinkielet,
              kansalaisuudet = kansalaisuudet,
              sukupuoli = sukupuoli,
              ensikertalainen = ensikertalainen
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams").map(r => KkHakeneetHyvaksytytVastaanottaneetResult(r))
          }
        case "okm-ohjauksen-aloittain" =>
          val query = kkHakeneetHyvaksytytVastaanottaneetRepository.selectOkmOhjauksenAloittainWithParams(
            selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
            haut = haut,
            hakukohteet = hakukohteet,
            hakukohderyhmat = hakukohderyhmarajaus,
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
            haut = haut,
            hakukohteet = hakukohteet,
            hakukohderyhmat = hakukohderyhmarajaus,
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
            haut = haut,
            hakukohteet = hakukohteet,
            hakukohderyhmat = hakukohderyhmarajaus,
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
            haut = haut,
            hakukohteet = hakukohteet,
            hakukohderyhmat = hakukohderyhmarajaus,
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
        haut = haut,
        hakukohteet = hakukohteet,
        hakukohderyhmat = hakukohderyhmarajaus,
        okmOhjauksenAlat = okmOhjauksenAlat,
        tutkinnonTasot = tutkinnonTasot,
        aidinkielet = aidinkielet,
        kansalaisuudet = kansalaisuudet,
        sukupuoli = sukupuoli,
        ensikertalainen = ensikertalainen
      )

      val ensikertalaisetSumQuery = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams(
        selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
        haut = haut,
        hakukohteet = hakukohteet,
        hakukohderyhmat = hakukohderyhmarajaus,
        okmOhjauksenAlat = okmOhjauksenAlat,
        tutkinnonTasot = tutkinnonTasot,
        aidinkielet = aidinkielet,
        kansalaisuudet = kansalaisuudet,
        sukupuoli = sukupuoli,
        ensikertalainen = Some(true)
      )

      val maksuvelvollisetSumQuery = kkHakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams(
        selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
        haut = haut,
        hakukohteet = hakukohteet,
        hakukohderyhmat = hakukohderyhmarajaus,
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
      ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
        asiointikieli,
        translations,
        sortedResult.toList,
        sumQueryResult,
        ensikertalaisetSumQueryResult,
        maksuvelvollisetSumQueryResult,
        naytaHakutoiveet,
        tulostustapa
      )
    } match {
      case Success(excelFile) => Right(excelFile)
      case Failure(exception) =>
        LOG.error("Error generating Excel report", exception)
        Left("virhe.tietokanta")
    }
  }

}
