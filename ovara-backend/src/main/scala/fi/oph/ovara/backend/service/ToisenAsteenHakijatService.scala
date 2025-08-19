package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.ToisenAsteenHakija
import fi.oph.ovara.backend.raportointi.dto.{ValidatedHakijatParams, buildHakijatParamsForExcel}
import fi.oph.ovara.backend.repository.{ReadOnlyDatabase, ToisenAsteenHakijatRepository}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

import scala.util.{Failure, Success, Try}

@Component
@Service
class ToisenAsteenHakijatService(
    toisenAsteenHakijatRepository: ToisenAsteenHakijatRepository,
    userService: UserService,
    commonService: CommonService,
    lokalisointiService: LokalisointiService
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ToisenAsteenHakijatService]);

  @Autowired
  val db: ReadOnlyDatabase = null

  def get(
           haut: List[String],
           oppilaitokset: List[String],
           toimipisteet: List[String],
           hakukohteet: List[String],
           pohjakoulutukset: List[String],
           valintatiedot: List[String],
           vastaanottotiedot: List[String],
           harkinnanvaraisuudet: List[String],
           kaksoistutkintoKiinnostaa: Option[Boolean],
           urheilijatutkintoKiinnostaa: Option[Boolean],
           soraTerveys: Option[Boolean],
           soraAiempi: Option[Boolean],
           markkinointilupa: Option[Boolean],
           julkaisulupa: Option[Boolean]
         ): Either[String, XSSFWorkbook] = {

    val user = userService.getEnrichedUserDetails
    val asiointikieli = user.asiointikieli.getOrElse("fi")
    val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

    val authorities = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)

    val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset
    )

    Try {
      val query = toisenAsteenHakijatRepository.selectWithParams(
        kayttooikeusOrganisaatiot = orgOidsForQuery,
        haut = haut,
        oppilaitokset = oppilaitokset,
        toimipisteet = toimipisteet,
        hakukohteet = hakukohteet,
        pohjakoulutukset = pohjakoulutukset,
        valintatieto = valintatiedot,
        vastaanottotieto = vastaanottotiedot,
        harkinnanvaraisuudet = harkinnanvaraisuudet,
        kaksoistutkintoKiinnostaa = kaksoistutkintoKiinnostaa,
        urheilijatutkintoKiinnostaa = urheilijatutkintoKiinnostaa,
        soraTerveys = soraTerveys,
        soraAiempi = soraAiempi,
        markkinointilupa = markkinointilupa,
        julkaisulupa = julkaisulupa
      )

      val queryResult = db.run(query, "toisenAsteenHakijatRepository.selectWithParams")
      val sortedList =
        queryResult.sortBy(resultRow => (resultRow.hakijanSukunimi.toLowerCase(), resultRow.hakijanEtunimi.toLowerCase, resultRow.oppijanumero))

      val raporttiParamNames = db.run(
        toisenAsteenHakijatRepository.hakuParamNamesQuery(
          haut,
          oppilaitokset,
          toimipisteet,
          hakukohteet,
          pohjakoulutukset,
        ),
        "hakuParamNamesQuery"
      ).map(param => param.parametri -> param.nimet).toMap
      
      val raporttiParams = buildHakijatParamsForExcel(
        ValidatedHakijatParams(
          haut,
          oppilaitokset,
          toimipisteet,
          hakukohteet,
          pohjakoulutukset,
          valintatiedot,
          vastaanottotiedot,
          harkinnanvaraisuudet,
          kaksoistutkintoKiinnostaa,
          urheilijatutkintoKiinnostaa,
          soraTerveys,
          soraAiempi,
          markkinointilupa,
          julkaisulupa,
        ), 
        raporttiParamNames)
      ExcelWriter.writeToisenAsteenHakijatRaportti(
        sortedList,
        asiointikieli,
        translations,
        raporttiParams
      )
    } match {
      case Success(excelFile) => Right(excelFile)
      case Failure(exception) =>
        LOG.error("Error generating Excel report", exception)
        Left("virhe.raportti")
    }
  }
}
