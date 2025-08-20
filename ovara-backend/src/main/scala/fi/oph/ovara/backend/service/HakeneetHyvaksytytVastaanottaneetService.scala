package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{HakeneetHyvaksytytVastaanottaneetHakukohteittain, HakeneetHyvaksytytVastaanottaneetResult, Kieli}
import fi.oph.ovara.backend.raportointi.dto.{ValidatedHakeneetHyvaksytytVastaanottaneetParams, buildHakeneetHyvaksytytVastaanottaneetParamsForExcel}
import fi.oph.ovara.backend.repository.{HakeneetHyvaksytytVastaanottaneetRepository, ReadOnlyDatabase}
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
class HakeneetHyvaksytytVastaanottaneetService(
                                                hakeneetHyvaksytytVastaanottaneetRepository: HakeneetHyvaksytytVastaanottaneetRepository,
                                                userService: UserService,
                                                commonService: CommonService,
                                                lokalisointiService: LokalisointiService
                                              ) {

  @Autowired
  val db: ReadOnlyDatabase = null

  val LOG: Logger = LoggerFactory.getLogger(classOf[HakeneetHyvaksytytVastaanottaneetService]);

  def get(
           haut: List[String],
           tulostustapa: String,
           koulutustoimija: Option[String],
           oppilaitokset: List[String],
           toimipisteet: List[String],
           hakukohteet: List[String],
           koulutusalat1: List[String],
           koulutusalat2: List[String],
           koulutusalat3: List[String],
           opetuskielet: List[String],
           maakunnat: List[String],
           kunnat: List[String],
           harkinnanvaraisuudet: List[String],
           sukupuoli: Option[String],
           naytaHakutoiveet: Boolean,
         ): Either[String, XSSFWorkbook] = {
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
    Try {
      val queryResult = tulostustapa match {
        case "hakukohteittain" =>
            val query = hakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              haut = haut,
              hakukohteet = hakukohteet,
              koulutusalat1 = koulutusalat1,
              koulutusalat2 = koulutusalat2,
              koulutusalat3 = koulutusalat3,
              opetuskielet = opetuskielet,
              maakunnat = maakunnat,
              kunnat = kunnat,
              harkinnanvaraisuudet = harkinnanvaraisuudet,
              sukupuoli = sukupuoli
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectHakukohteittainWithParams")
        case "koulutusaloittain" =>
            val query = hakeneetHyvaksytytVastaanottaneetRepository.selectKoulutusaloittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              haut = haut,
              hakukohteet = hakukohteet,
              koulutusalat1 = koulutusalat1,
              koulutusalat2 = koulutusalat2,
              koulutusalat3 = koulutusalat3,
              opetuskielet = opetuskielet,
              maakunnat = maakunnat,
              kunnat = kunnat,
              harkinnanvaraisuudet = harkinnanvaraisuudet,
              sukupuoli = sukupuoli
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectKoulutusaloittainWithParams")
            .map(r => HakeneetHyvaksytytVastaanottaneetResult(r))
        case "toimipisteittain" =>
            val query = hakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              haut = haut,
              hakukohteet = hakukohteet,
              koulutusalat1 = koulutusalat1,
              koulutusalat2 = koulutusalat2,
              koulutusalat3 = koulutusalat3,
              opetuskielet = opetuskielet,
              maakunnat = maakunnat,
              kunnat = kunnat,
              harkinnanvaraisuudet = harkinnanvaraisuudet,
              sukupuoli = sukupuoli
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectToimipisteittainWithParams")
              .map(r => HakeneetHyvaksytytVastaanottaneetResult(r))
        case _ =>
            val query = hakeneetHyvaksytytVastaanottaneetRepository.selectOrganisaatioittainWithParams(
              selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
              haut = haut,
              hakukohteet = hakukohteet,
              koulutusalat1 = koulutusalat1,
              koulutusalat2 = koulutusalat2,
              koulutusalat3 = koulutusalat3,
              opetuskielet = opetuskielet,
              maakunnat = maakunnat,
              kunnat = kunnat,
              harkinnanvaraisuudet = harkinnanvaraisuudet,
              sukupuoli = sukupuoli,
              organisaatiotaso = tulostustapa
            )
            db.run(query, "hakeneetHyvaksytytVastaanottaneetRepository.selectOrganisaatioittainWithParams")
              .map(r => HakeneetHyvaksytytVastaanottaneetResult(r))
      }

      val collator: Collator = Collator.getInstance(new Locale(asiointikieli))
      collator.setStrength(Collator.PRIMARY)
      val sortedResult = queryResult.sortWith {
        case (a: HakeneetHyvaksytytVastaanottaneetHakukohteittain, b: HakeneetHyvaksytytVastaanottaneetHakukohteittain) =>
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
        case (a: HakeneetHyvaksytytVastaanottaneetResult, b: HakeneetHyvaksytytVastaanottaneetResult) =>
          collator.compare(a.otsikko.getOrElse(Kieli.withName(asiointikieli), ""), b.otsikko.getOrElse(Kieli.withName(asiointikieli), "")) < 0
      }
      val sumQuery = hakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams(
        selectedKayttooikeusOrganisaatiot = orgOidsForQuery,
        haut = haut,
        hakukohteet = hakukohteet,
        koulutusalat1 = koulutusalat1,
        koulutusalat2 = koulutusalat2,
        koulutusalat3 = koulutusalat3,
        opetuskielet = opetuskielet,
        maakunnat = maakunnat,
        kunnat = kunnat,
        harkinnanvaraisuudet = harkinnanvaraisuudet,
        sukupuoli = sukupuoli
      )

      val sumQueryResult = db.run(sumQuery, "hakeneetHyvaksytytVastaanottaneetRepository.selectHakijatYhteensaWithParams")

      val raporttiParamNames = db.run(
        hakeneetHyvaksytytVastaanottaneetRepository.hakuParamNamesQuery(
          haut = haut,
          koulutustoimija = koulutustoimija,
          oppilaitokset = oppilaitokset,
          toimipisteet = toimipisteet,
          hakukohteet = hakukohteet,
          opetuskielet = opetuskielet,
          koulutusalat1 = koulutusalat1,
          koulutusalat2 = koulutusalat2,
          koulutusalat3 = koulutusalat3,
          maakunnat = maakunnat,
          kunnat = kunnat,
          sukupuoli = sukupuoli,
        ),
        "hakuParamNamesQuery"
      ).map(param => param.parametri -> param.nimet).toMap

      val raporttiParams = buildHakeneetHyvaksytytVastaanottaneetParamsForExcel(
        ValidatedHakeneetHyvaksytytVastaanottaneetParams(
          haut = haut,
          tulostustapa = tulostustapa,
          koulutustoimija = koulutustoimija,
          oppilaitokset = oppilaitokset,
          toimipisteet = toimipisteet,
          hakukohteet = hakukohteet,
          koulutusalat1 = koulutusalat1,
          koulutusalat2 = koulutusalat2,
          koulutusalat3 = koulutusalat3,
          opetuskielet = opetuskielet,
          maakunnat = maakunnat,
          kunnat = kunnat,
          harkinnanvaraisuudet = harkinnanvaraisuudet,
          naytaHakutoiveet = naytaHakutoiveet,
          sukupuoli = sukupuoli
        ), raporttiParamNames
      )
      
      ExcelWriter.writeHakeneetHyvaksytytVastaanottaneetRaportti(
        asiointikieli,
        translations,
        sortedResult.toList,
        sumQueryResult,
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
