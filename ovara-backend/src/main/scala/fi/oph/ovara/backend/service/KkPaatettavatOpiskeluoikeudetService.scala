package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{
  Fi,
  KKPaatettavaOpiskeluoikeusEntity,
  KKSitovastiVastaanottanut,
  KkPaatettavaOpiskeluoikeus
}
import fi.oph.ovara.backend.raportointi.dto.{
  buildKkPaatettavatOpiskeluoikeudetParamsForExcel,
  KkPaatettavatOpiskeluoikeudetParams
}
import fi.oph.ovara.backend.repository.{KkPaatettavatOpiskeluoikeudetRepository, ReadOnlyDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, CommonExcelParams, ExcelWriter}
import fi.oph.ovara.backend.yos.{YosPredicate, YosService}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.{LocalDate, LocalDateTime}
import scala.util.{Failure, Success, Try}

@Service
class KkPaatettavatOpiskeluoikeudetService(
  kkPaatettavatOpiskeluoikeudetRepository: KkPaatettavatOpiskeluoikeudetRepository,
  userService: UserService,
  commonService: CommonService,
  lokalisointiService: LokalisointiService,
  yosService: YosService
) {

  @Autowired
  val db: ReadOnlyDatabase = null

  val LOG: Logger = LoggerFactory.getLogger(classOf[KkPaatettavatOpiskeluoikeudetService])

  def get(params: KkPaatettavatOpiskeluoikeudetParams): Either[String, XSSFWorkbook] = {
    val user                      = userService.getEnrichedUserDetails
    val asiointikieli             = user.asiointikieli.getOrElse("fi")
    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)
    val translations              = lokalisointiService.getOvaraTranslations(asiointikieli)

    val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      oppilaitosOids = List(params.oppilaitos),
      List.empty
    )
    Try {
      val data               = yosService.getPaattyvatOpiskeluOikeudet(orgOidsForQuery, params)
      val raporttiParamNames = db
        .run(
          kkPaatettavatOpiskeluoikeudetRepository.organisaatioNameQuery(params.oppilaitos),
          "hakuParamNamesQuery"
        )
        .map(param => param.parametri -> param.nimi)
        .toMap

      val raporttiParams = buildKkPaatettavatOpiskeluoikeudetParamsForExcel(
        KkPaatettavatOpiskeluoikeudetParams(
          params.oppilaitos,
          params.sukunimi,
          params.etunimet,
          params.hetu,
          params.oppijanumero,
          params.opiskeluoikeudenTila
        ),
        raporttiParamNames
      )
      ExcelWriter.writeKorkeakouluPaatettavatOpiskeluoikeudetRaportti(
        data,
        CommonExcelParams(asiointikieli, translations, raporttiParams, LocalDateTime.now())
      )
    } match {
      case Success(excelFile) => Right(excelFile)
      case Failure(exception) =>
        LOG.error("Error generating Excel report", exception)
        Left("virhe.tietokanta")
    }
  }

}
