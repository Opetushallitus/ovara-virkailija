package fi.oph.ovara.backend.raportointi

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.ovara.backend.domain.UserResponse
import fi.oph.ovara.backend.service.*
import fi.oph.ovara.backend.utils.AuditOperation.{HakeneetHyvaksytytVastaanottaneet, KkHakeneetHyvaksytytVastaanottaneet, KkHakijat, KorkeakouluKoulutuksetToteutuksetHakukohteet, KoulutuksetToteutuksetHakukohteet, ToisenAsteenHakijat}
import fi.oph.ovara.backend.utils.ParameterValidator.{validateAlphanumeric, validateAlphanumericList, validateHakeneetHyvaksytytVastaanottaneetParams, validateHakijatParams, validateKkHakeneetHyvaksytytVastaanottaneetParams, validateKkHakijatParams, validateKkKoulutuksetToteutuksetHakukohteetParams, validateKoulutuksetToteutuksetHakukohteetParams, validateNumericList, validateOid, validateOidList, validateOrganisaatioOidList}
import fi.oph.ovara.backend.utils.{AuditLog, AuditOperation}
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.apache.poi.ss.usermodel.Workbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import org.springframework.web.servlet.view.RedirectView
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util
import scala.jdk.CollectionConverters.*

case class ErrorResponse(
                          status: Int,
                          message: String,
                          details: Option[List[String]] = None
                        )

@RestController
@RequestMapping(path = Array("api"))
class Controller(
    commonService: CommonService,
    koulutuksetToteutuksetHakukohteetService: KoulutuksetToteutuksetHakukohteetService,
    kkKoulutuksetToteutuksetHakukohteetService: KorkeakouluKoulutuksetToteutuksetHakukohteetService,
    hakijatService: ToisenAsteenHakijatService,
    kkHakijatService: KkHakijatService,
    hakeneetHyvaksytytVastaanottaneetService: HakeneetHyvaksytytVastaanottaneetService,
    kkHakeneetHyvaksytytVastaanottaneetService: KkHakeneetHyvaksytytVastaanottaneetService,
    userService: UserService,
    val auditLog: AuditLog = AuditLog
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[Controller])

  @Value("${ovara.ui.url}")
  val ovaraUiUrl: String = null

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(SerializationFeature.INDENT_OUTPUT, true)

  private def strToOptionBoolean(str: String) = {
    if (str == null) {
      None
    } else {
      try {
        Option(str.toBoolean)
      } catch {
        case _: Exception =>
          // jos ei validi boolean, palautetaan None
          None
      }
    }
  }

  private def getListParamAsScalaList(listParam: util.Collection[String]) = {
    if (listParam == null) List() else listParam.asScala.toList
  }

  private def handleRequest[T](
                        validationErrors: List[String],
                        mapper: ObjectMapper
                      )(block: => Either[String, T]): ResponseEntity[String] = {
    if (validationErrors.nonEmpty) {
      // validointivirheistä palautetaan yksityiskohtia
      val errorResponse = ErrorResponse(
        status = HttpServletResponse.SC_BAD_REQUEST,
        message = "validation.error",
        details = Some(validationErrors)
      )
      ResponseEntity
        .status(HttpServletResponse.SC_BAD_REQUEST)
        .body(mapper.writeValueAsString(errorResponse))
    } else {
      block match {
        case Right(result) =>
          ResponseEntity.ok(mapper.writeValueAsString(result))
        case Left(errorMessage) =>
          // odottamattomista virheistä vain virheviesti
          ResponseEntity
            .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            .body(mapper.writeValueAsString(errorMessage))
      }
    }
  }

  @GetMapping(path = Array("healthcheck"))
  def healthcheck = "Ovara application is running!"

  @GetMapping(path = Array("user"))
  def user(): String = {
    val enrichedUserDetails = userService.getEnrichedUserDetails
    mapper.writeValueAsString(
      UserResponse(
        user =
          if (enrichedUserDetails == null)
            null
          else
            enrichedUserDetails
      )
    )
  }

  @GetMapping(path = Array("login"))
  def login = RedirectView(ovaraUiUrl)

  @GetMapping(path = Array("session"))
  def response: ResponseEntity[Map[String, String]] = {
    // Palautetaan jokin paluuarvo koska client-kirjasto sellaisen haluaa
    ResponseEntity.ok(Map("status" -> "ok"))
  }

  @GetMapping(path = Array("csrf"))
  def csrf(csrfToken: CsrfToken): String = mapper.writeValueAsString(csrfToken)

  @GetMapping(path = Array("alkamisvuodet"))
  def alkamisvuodet: ResponseEntity[String] = {
    handleRequest(Nil, mapper) {
      commonService.getAlkamisvuodet
    }
  }

  @GetMapping(path = Array("haut"))
  def haut(
            @RequestParam("alkamiskaudet", required = false) alkamiskaudet: java.util.Collection[String],
            @RequestParam("haut", required = false) selectedHaut: java.util.Collection[String],
            @RequestParam("haun_tyyppi", required = false) haun_tyyppi: String
          ): ResponseEntity[String] = {
    val errors = List(
      validateAlphanumericList(getListParamAsScalaList(alkamiskaudet), "alkamiskaudet"),
      validateOidList(getListParamAsScalaList(selectedHaut), "haut"),
      validateAlphanumeric(Option(haun_tyyppi), "haun-tyyppi")
    ).flatten.distinct

    handleRequest(errors, mapper) {
      commonService.getHaut(
        getListParamAsScalaList(alkamiskaudet),
        getListParamAsScalaList(selectedHaut),
        Option(haun_tyyppi).getOrElse("")
      )
    }
  }

  @GetMapping(path = Array("hakukohteet"))
  def hakukohteet(
                   @RequestParam("haut") haut: java.util.Collection[String],
                   @RequestParam("oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
                   @RequestParam("toimipisteet", required = false) toimipisteet: java.util.Collection[String],
                   @RequestParam("hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String],
                   @RequestParam("hakukohteet", required = false) selectedHakukohteet: java.util.Collection[String]
                 ): ResponseEntity[String] = {
    val errors = List(
      validateOrganisaatioOidList(getListParamAsScalaList(oppilaitokset), "oppilaitokset"),
      validateOrganisaatioOidList(getListParamAsScalaList(toimipisteet), "toimipisteet"),
      validateOidList(getListParamAsScalaList(haut), "haut"),
      validateOidList(getListParamAsScalaList(hakukohderyhmat), "hakukohderyhmat"),
      validateOidList(getListParamAsScalaList(selectedHakukohteet), "hakukohteet")
    ).flatten.distinct

    handleRequest(errors, mapper) {
      commonService.getHakukohteet(
        getListParamAsScalaList(oppilaitokset),
        getListParamAsScalaList(toimipisteet),
        getListParamAsScalaList(haut),
        getListParamAsScalaList(hakukohderyhmat),
        getListParamAsScalaList(selectedHakukohteet)
      )
    }
  }

  @GetMapping(path = Array("pohjakoulutukset-toinen-aste"))
  def pohjakoulutuksetToinenAste(): ResponseEntity[String] = {
    handleRequest(Nil, mapper) {
      commonService.getPohjakoulutukset
    }
  }

  @GetMapping(path = Array("organisaatiot"))
  def organisaatiot: ResponseEntity[String] = {
    handleRequest(Nil, mapper) {
      commonService.getOrganisaatioHierarkiatWithUserRights
    }
  }

  @GetMapping(path = Array("harkinnanvaraisuudet"))
  def harkinnanvaraisuudet: ResponseEntity[String] = {
    handleRequest(Nil, mapper) {
      commonService.getHarkinnanvaraisuudet
    }
  }

  @GetMapping(path = Array("valintatiedot"))
  def valintatiedot: ResponseEntity[String] = {
    handleRequest(Nil, mapper) {
      commonService.getValintatiedot
    }
  }

  @GetMapping(path = Array("vastaanottotiedot"))
  def vastaanottotiedot: ResponseEntity[String] = {
    handleRequest(Nil, mapper) {
      commonService.getVastaanottotiedot
    }
  }

  @GetMapping(path = Array("opetuskielet"))
  def opetuskielet: ResponseEntity[String] = {
    handleRequest(Nil, mapper) {
      commonService.getOpetuskielet
    }
  }

  @GetMapping(path = Array("maakunnat"))
  def maakunnat: ResponseEntity[String] = {
    handleRequest(Nil, mapper) {
      commonService.getMaakunnat
    }
  }

  @GetMapping(path = Array("kunnat"))
  def kunnat(
              @RequestParam("maakunnat", required = false) maakunnat: java.util.Collection[String],
              @RequestParam("selectedKunnat", required = false) selectedKunnat: java.util.Collection[String]
            ): ResponseEntity[String] = {
    val errors = List(
      validateNumericList(getListParamAsScalaList(maakunnat), "maakunnat"),
      validateNumericList(getListParamAsScalaList(selectedKunnat), "kunnat")
    ).flatten.distinct

    handleRequest(errors, mapper) {
      commonService.getKunnat(
        getListParamAsScalaList(maakunnat),
        getListParamAsScalaList(selectedKunnat)
      )
    }
  }

  @GetMapping(path = Array("koulutusalat1"))
  def koulutusalat1: ResponseEntity[String] = {
    handleRequest(Nil, mapper) {
      commonService.getKoulutusalat1
    }
  }

  @GetMapping(path = Array("koulutusalat2"))
  def koulutusalat2(
                     @RequestParam("koulutusalat1", required = false) koulutusalat1: java.util.Collection[String],
                     @RequestParam("selectedKoulutusalat2", required = false) selectedKoulutusalat2: java.util.Collection[String]
                   ): ResponseEntity[String] = {
    val errors = List(
      validateNumericList(getListParamAsScalaList(koulutusalat1), "koulutusalat1"),
      validateNumericList(getListParamAsScalaList(selectedKoulutusalat2), "koulutusalat2")
    ).flatten.distinct

    handleRequest(errors, mapper) {
      commonService.getKoulutusalat2(
        getListParamAsScalaList(koulutusalat1),
        getListParamAsScalaList(selectedKoulutusalat2)
      )
    }
  }

  @GetMapping(path = Array("koulutusalat3"))
  def koulutusalat3(
                     @RequestParam("koulutusalat2", required = false) koulutusalat2: java.util.Collection[String],
                     @RequestParam("selectedKoulutusalat3", required = false) selectedKoulutusalat3: java.util.Collection[String]
                   ): ResponseEntity[String] = {
    val errors = List(
      validateNumericList(getListParamAsScalaList(koulutusalat2), "koulutusalat2"),
      validateNumericList(getListParamAsScalaList(selectedKoulutusalat3), "koulutusalat3")
    ).flatten.distinct

    handleRequest(errors, mapper) {
      commonService.getKoulutusalat3(
        getListParamAsScalaList(koulutusalat2),
        getListParamAsScalaList(selectedKoulutusalat3)
      )
    }
  }

  @GetMapping(path = Array("hakukohderyhmat"))
  def hakukohderyhmat(
                       @RequestParam("haut", required = true) haut: java.util.Collection[String]
                     ): ResponseEntity[String] = {
    val errors = validateOidList(getListParamAsScalaList(haut), "haut")

    handleRequest(errors, mapper) {
      commonService.getHakukohderyhmat(getListParamAsScalaList(haut))
    }
  }

  @GetMapping(path = Array("okm-ohjauksen-alat"))
  def okmOhjauksenAlat: ResponseEntity[String] = {
    handleRequest(Nil, mapper) {
      commonService.getOkmOhjauksenAlat
    }
  }

  // RAPORTIT

  private def handleExcelRequest(
                                  validationErrors: List[String],
                                  response: HttpServletResponse,
                                  request: HttpServletRequest,
                                  id: String,
                                  raporttiParams: Map[String, Any],
                                  auditOperation: AuditOperation,
                                  mapper: ObjectMapper
                                )(block: => Either[String, XSSFWorkbook]): Unit = {
    if (validationErrors.nonEmpty) {
      LOG.warn(s"Excel parameter validation failed: ${validationErrors.mkString(", ")}")
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
      response.setContentType("application/json")
      val errorJson = mapper.writeValueAsString(Map(
        "status" -> HttpServletResponse.SC_BAD_REQUEST,
        "message" -> "validation.error",
        "details" -> validationErrors.asJava
      ))
      response.getWriter.write(errorJson)
      return
    }

    try {
      block match {
        case Right(wb) =>
          auditLog.logWithParams(request, auditOperation, raporttiParams)
          LOG.info(s"Sending Excel report: $id")
          val dateTimeStr = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
          val out = response.getOutputStream
          response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
          response.setHeader(
            HttpHeaders.CONTENT_DISPOSITION,
            s"attachment; filename=$id-$dateTimeStr.xlsx"
          )
          response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
          wb.write(out)
          out.close()
          wb.close()

        case Left(errorKey) =>
          LOG.error(s"Excel report generation failed ($id): $errorKey")
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
          response.setContentType("application/json")
          response.getWriter.write(mapper.writeValueAsString(errorKey))
      }
    } catch {
      case e: Exception =>
        LOG.error(s"Unexpected error while generating Excel ($id): ${e.getMessage}", e)
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        response.setContentType("application/json")
        response.getWriter.write(mapper.writeValueAsString("unexpected.error"))
    }
  }

  @GetMapping(path = Array("koulutukset-toteutukset-hakukohteet"))
  def koulutukset_toteutukset_hakukohteet(
      @RequestParam("haut") haut: java.util.Collection[String],
      @RequestParam("koulutustoimija", required = false) koulutustoimija: String,
      @RequestParam("oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
      @RequestParam("toimipisteet", required = false) toimipisteet: java.util.Collection[String],
      @RequestParam("koulutuksen-tila", required = false) koulutuksenTila: String,
      @RequestParam("toteutuksen-tila", required = false) toteutuksenTila: String,
      @RequestParam("hakukohteen-tila", required = false) hakukohteenTila: String,
      @RequestParam("valintakoe", required = false) valintakoe: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val maybeKoulutustoimija = Option(koulutustoimija)
    val maybeKoulutuksenTila = Option(koulutuksenTila)
    val maybeToteutuksenTila = Option(toteutuksenTila)
    val maybeHakukohteenTila = Option(hakukohteenTila)
    val maybeValintakoe = strToOptionBoolean(valintakoe)
    val oppilaitosList = getListParamAsScalaList(oppilaitokset)
    val toimipisteList = getListParamAsScalaList(toimipisteet)
    val hakuList       = getListParamAsScalaList(haut)

    val validationErrors = validateKoulutuksetToteutuksetHakukohteetParams(
      hakuList,
      maybeKoulutustoimija,
      oppilaitosList,
      toimipisteList,
      maybeKoulutuksenTila,
      maybeToteutuksenTila,
      maybeHakukohteenTila,
      valintakoe
    )

    val raporttiParams = Map(
      "haut"            -> Option(hakuList).filterNot(_.isEmpty),
      "koulutustoimija" -> maybeKoulutustoimija,
      "oppilaitokset"   -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipisteet"    -> Option(toimipisteList).filterNot(_.isEmpty),
      "koulutuksenTila" -> maybeKoulutuksenTila,
      "toteutuksenTila" -> maybeToteutuksenTila,
      "hakukohteenTila" -> maybeHakukohteenTila,
      "valintakoe"      -> maybeValintakoe
    ).collect { case (key, Some(value)) => key -> value } // jätetään pois tyhjät parametrit

    handleExcelRequest(
      validationErrors = validationErrors,
      response = response,
      request = request,
      id = "koulutukset-toteutukset-hakukohteet",
      raporttiParams = raporttiParams,
      auditOperation = KoulutuksetToteutuksetHakukohteet,
      mapper = mapper
    ) {
      koulutuksetToteutuksetHakukohteetService.get(
        hakuList,
        maybeKoulutustoimija,
        oppilaitosList,
        toimipisteList,
        maybeKoulutuksenTila,
        maybeToteutuksenTila,
        maybeHakukohteenTila,
        maybeValintakoe
      )
    }
  }

  @GetMapping(path = Array("kk-koulutukset-toteutukset-hakukohteet"))
  def kk_koulutukset_toteutukset_hakukohteet(
      @RequestParam("haut") haut: java.util.Collection[String],
      @RequestParam("tulostustapa", defaultValue = "koulutuksittain") tulostustapa: String,
      @RequestParam("oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
      @RequestParam("toimipisteet", required = false) toimipisteet: java.util.Collection[String],
      @RequestParam("hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String],
      @RequestParam("koulutuksen-tila", required = false) koulutuksenTila: String,
      @RequestParam("toteutuksen-tila", required = false) toteutuksenTila: String,
      @RequestParam("hakukohteen-tila", required = false) hakukohteenTila: String,
      @RequestParam("tutkinnon-tasot", required = false) tutkinnonTasot: java.util.Collection[String],
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val maybeKoulutuksenTila = Option(koulutuksenTila)
    val maybeToteutuksenTila = Option(toteutuksenTila)
    val maybeHakukohteenTila = Option(hakukohteenTila)

    val oppilaitosList     = getListParamAsScalaList(oppilaitokset)
    val toimipisteList     = getListParamAsScalaList(toimipisteet)
    val hakukohderyhmaList = getListParamAsScalaList(hakukohderyhmat)
    val hakuList           = getListParamAsScalaList(haut)
    val tutkinnonTasoList  = getListParamAsScalaList(tutkinnonTasot)

    val validationErrors = validateKkKoulutuksetToteutuksetHakukohteetParams(
      hakuList,
      oppilaitosList,
      toimipisteList,
      hakukohderyhmaList,
      tulostustapa,
      maybeKoulutuksenTila,
      maybeToteutuksenTila,
      maybeHakukohteenTila,
      tutkinnonTasoList
    )

    val raporttiParams = Map(
      "haut"            -> Option(hakuList).filterNot(_.isEmpty),
      "oppilaitokset"   -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipisteet"    -> Option(toimipisteList).filterNot(_.isEmpty),
      "hakukohderyhmat" -> Option(hakukohderyhmaList).filterNot(_.isEmpty),
      "koulutuksenTila" -> maybeKoulutuksenTila,
      "toteutuksenTila" -> maybeToteutuksenTila,
      "hakukohteenTila" -> maybeHakukohteenTila,
      "tutkinnonTasot"  -> Option(tutkinnonTasoList).filterNot(_.isEmpty)
    ).collect { case (key, Some(value)) => key -> value }

    handleExcelRequest(
      validationErrors = validationErrors,
      response = response,
      request = request,
      id = "kk-koulutukset-toteutukset-hakukohteet",
      raporttiParams = raporttiParams,
      auditOperation = KorkeakouluKoulutuksetToteutuksetHakukohteet,
      mapper = mapper
    ) {
      kkKoulutuksetToteutuksetHakukohteetService.get(
        hakuList,
        oppilaitosList,
        toimipisteList,
        hakukohderyhmaList,
        maybeKoulutuksenTila,
        maybeToteutuksenTila,
        maybeHakukohteenTila,
        tutkinnonTasoList,
        tulostustapa
      )
    }
  }

  @GetMapping(path = Array("hakijat"))
  def hakijat(
      @RequestParam("haut") haut: java.util.Collection[String],
      @RequestParam("oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
      @RequestParam("toimipisteet", required = false) toimipisteet: java.util.Collection[String],
      @RequestParam("hakukohteet", required = false) hakukohteet: java.util.Collection[String],
      @RequestParam("pohjakoulutukset", required = false) pohjakoulutukset: java.util.Collection[String],
      @RequestParam("valintatiedot", required = false) valintatiedot: java.util.Collection[String],
      @RequestParam("vastaanottotiedot", required = false) vastaanottotiedot: java.util.Collection[String],
      @RequestParam("harkinnanvaraisuudet", required = false) harkinnanvaraisuudet: java.util.Collection[String],
      @RequestParam("kaksoistutkinto", required = false) kaksoistutkinto: String,
      @RequestParam("urheilijatutkinto", required = false) urheilijatutkinto: String,
      @RequestParam("sora_terveys", required = false) soraterveys: String,
      @RequestParam("sora_aiempi", required = false) soraAiempi: String,
      @RequestParam("markkinointilupa", required = false) markkinointilupa: String,
      @RequestParam("julkaisulupa", required = false) julkaisulupa: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val hakuList               = getListParamAsScalaList(haut)
    val oppilaitosList         = getListParamAsScalaList(oppilaitokset)
    val toimipisteList         = getListParamAsScalaList(toimipisteet)
    val hakukohdeList          = getListParamAsScalaList(hakukohteet)
    val pohjakoulutusList      = getListParamAsScalaList(pohjakoulutukset)
    val valintatietoList       = getListParamAsScalaList(valintatiedot)
    val vastaanottotietoList   = getListParamAsScalaList(vastaanottotiedot)
    val harkinnanvaraisuusList = getListParamAsScalaList(harkinnanvaraisuudet)

    val maybeKaksoistutkintoKiinnostaa   = strToOptionBoolean(kaksoistutkinto)
    val maybeUrheilijatutkintoKiinnostaa = strToOptionBoolean(urheilijatutkinto)
    val maybeSoraTerveys                 = strToOptionBoolean(soraterveys)
    val maybeSoraAiempi                  = strToOptionBoolean(soraAiempi)
    val maybeMarkkinointilupa            = strToOptionBoolean(markkinointilupa)
    val maybeJulkaisulupa                = strToOptionBoolean(julkaisulupa)

    val validationErrors = validateHakijatParams(
      hakuList,
      oppilaitosList,
      toimipisteList,
      hakukohdeList,
      pohjakoulutusList,
      valintatietoList,
      vastaanottotietoList,
      harkinnanvaraisuusList,
      kaksoistutkinto,
      urheilijatutkinto,
      soraterveys,
      soraAiempi,
      markkinointilupa,
      julkaisulupa
    ) ++ Option.when(oppilaitosList.isEmpty && toimipisteList.isEmpty)("error.required.missing").toList

    val raporttiParams = Map(
      "haut"                 -> Option(hakuList).filterNot(_.isEmpty),
      "oppilaitokset"        -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipisteet"         -> Option(toimipisteList).filterNot(_.isEmpty),
      "hakukohteet"          -> Option(hakukohdeList).filterNot(_.isEmpty),
      "pohjakoulutukset"     -> Option(pohjakoulutusList).filterNot(_.isEmpty),
      "valintatiedot"        -> Option(vastaanottotietoList).filterNot(_.isEmpty),
      "vastaanottotiedot"    -> Option(vastaanottotietoList).filterNot(_.isEmpty),
      "harkinnanvaraisuudet" -> Option(harkinnanvaraisuusList).filterNot(_.isEmpty),
      "kaksoistutkinto"      -> maybeKaksoistutkintoKiinnostaa,
      "urheilijatutkinto"    -> maybeUrheilijatutkintoKiinnostaa,
      "soraTerveys"          -> maybeSoraTerveys,
      "soraAiempi"           -> maybeSoraAiempi,
      "markkinointilupa"     -> maybeMarkkinointilupa,
      "julkaisulupa"         -> maybeJulkaisulupa
    ).collect { case (key, Some(value)) => key -> value }

    handleExcelRequest(
      validationErrors = validationErrors,
      response = response,
      request = request,
      id = "hakijat",
      raporttiParams = raporttiParams,
      auditOperation = ToisenAsteenHakijat,
      mapper = mapper
    ) {
      if (validationErrors.nonEmpty) {
        Left("validation.error")
      } else {
        hakijatService.get(
          hakuList,
          oppilaitosList,
          toimipisteList,
          hakukohdeList,
          pohjakoulutusList,
          valintatietoList,
          vastaanottotietoList,
          harkinnanvaraisuusList,
          maybeKaksoistutkintoKiinnostaa,
          maybeUrheilijatutkintoKiinnostaa,
          maybeSoraTerveys,
          maybeSoraAiempi,
          maybeMarkkinointilupa,
          maybeJulkaisulupa
        )
      }
    }
  }


  @GetMapping(path = Array("kk-hakijat"))
  def kk_hakijat(
      @RequestParam("haut") haut: java.util.Collection[String],
      @RequestParam("oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
      @RequestParam("toimipisteet", required = false) toimipisteet: java.util.Collection[String],
      @RequestParam("hakukohteet", required = false) hakukohteet: java.util.Collection[String],
      @RequestParam("valintatiedot", required = false) valintatiedot: java.util.Collection[String],
      @RequestParam("vastaanottotiedot", required = false) vastaanottotiedot: java.util.Collection[String],
      @RequestParam("hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String],
      @RequestParam("kansalaisuusluokat", required = false) kansalaisuusluokat: java.util.Collection[String],
      @RequestParam("markkinointilupa", required = false) markkinointilupa: String,
      @RequestParam("nayta-yo-arvosanat", required = true) naytaYoArvosanat: String,
      @RequestParam("nayta-hetu", required = true) naytaHetu: String,
      @RequestParam("nayta-postiosoite", required = true) naytaPostiosoite: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val hakuList             = getListParamAsScalaList(haut)
    val oppilaitosList       = getListParamAsScalaList(oppilaitokset)
    val toimipisteList       = getListParamAsScalaList(toimipisteet)
    val hakukohdeList        = getListParamAsScalaList(hakukohteet)
    val valintatietoList     = getListParamAsScalaList(valintatiedot)
    val vastaanottotietoList = getListParamAsScalaList(vastaanottotiedot)
    val hakukohderyhmaList   = getListParamAsScalaList(hakukohderyhmat)
    val kansalaisuusList     = getListParamAsScalaList(kansalaisuusluokat)

    val maybeMarkkinointilupa = strToOptionBoolean(markkinointilupa)

    val validationErrors = validateKkHakijatParams(
      hakuList,
      oppilaitosList,
      toimipisteList,
      hakukohdeList,
      valintatietoList,
      vastaanottotietoList,
      hakukohderyhmaList,
      kansalaisuusList,
      markkinointilupa,
      naytaYoArvosanat,
      naytaHetu,
      naytaPostiosoite,
    ) ++ Option.when(oppilaitosList.isEmpty && toimipisteList.isEmpty && hakukohderyhmaList.isEmpty)("error.required.missing").toList

    val raporttiParams = Map(
      "haut" -> Option(hakuList).filterNot(_.isEmpty),
      "oppilaitokset" -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipisteet" -> Option(toimipisteList).filterNot(_.isEmpty),
      "hakukohteet" -> Option(hakukohdeList).filterNot(_.isEmpty),
      "valintatiedot" -> Option(valintatietoList).filterNot(_.isEmpty),
      "vastaanottotiedot" -> Option(vastaanottotietoList).filterNot(_.isEmpty),
      "hakukohderyhmat" -> Option(hakukohderyhmaList).filterNot(_.isEmpty),
      "kansalaisuusluokat" -> Option(kansalaisuusList).filterNot(_.isEmpty),
      "markkinointilupa" -> maybeMarkkinointilupa,
      "naytaYoArvosanat" -> naytaYoArvosanat,
      "naytaHetu" -> naytaHetu,
      "naytaPostiosoite" -> naytaPostiosoite
    ).collect { case (key, Some(value)) => key -> value }

    handleExcelRequest(
      validationErrors = validationErrors,
      response = response,
      request = request,
      id = "kk-hakijat",
      raporttiParams = raporttiParams,
      auditOperation = KkHakijat,
      mapper = mapper
    ) {
      kkHakijatService.get(
        hakuList,
        oppilaitosList,
        toimipisteList,
        hakukohdeList,
        valintatietoList,
        vastaanottotietoList,
        hakukohderyhmaList,
        kansalaisuusList,
        maybeMarkkinointilupa,
        naytaYoArvosanat.toBoolean,
        naytaHetu.toBoolean,
        naytaPostiosoite.toBoolean
      )
    }
  }

  @GetMapping(path = Array("hakeneet-hyvaksytyt-vastaanottaneet"))
  def hakeneet_hyvaksytyt_vastaanottaneet(
      @RequestParam("haut") haut: java.util.Collection[String],
      @RequestParam("tulostustapa") tulostustapa: String,
      @RequestParam("koulutustoimija", required = false) koulutustoimija: String,
      @RequestParam("oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
      @RequestParam("toimipisteet", required = false) toimipisteet: java.util.Collection[String],
      @RequestParam("hakukohteet", required = false) hakukohteet: java.util.Collection[String],
      @RequestParam("koulutusalat1", required = false) koulutusalat1: java.util.Collection[String],
      @RequestParam("koulutusalat2", required = false) koulutusalat2: java.util.Collection[String],
      @RequestParam("koulutusalat3", required = false) koulutusalat3: java.util.Collection[String],
      @RequestParam("opetuskielet", required = false) opetuskielet: java.util.Collection[String],
      @RequestParam("maakunnat", required = false) maakunnat: java.util.Collection[String],
      @RequestParam("kunnat", required = false) kunnat: java.util.Collection[String],
      @RequestParam("harkinnanvaraisuudet", required = false) harkinnanvaraisuudet: java.util.Collection[String],
      @RequestParam("nayta-hakutoiveet", required = false) naytaHakutoiveet: String,
      @RequestParam("sukupuoli", required = false) sukupuoli: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val maybeKoulutustoimija           = Option(koulutustoimija)
    val tulostustapaValinta            = Option(tulostustapa).getOrElse("hakukohteittain")
    val naytaHakutoiveetBool           = strToOptionBoolean(naytaHakutoiveet).getOrElse(true) // oletuksena näytetään
    val maybeSukupuoli: Option[String] = if (sukupuoli == "neutral") None else Option(sukupuoli)
    val hakuList                       = getListParamAsScalaList(haut)
    val oppilaitosList                 = getListParamAsScalaList(oppilaitokset)
    val toimipisteList                 = getListParamAsScalaList(toimipisteet)
    val hakukohdeList                  = getListParamAsScalaList(hakukohteet)
    val koulutusala1List               = getListParamAsScalaList(koulutusalat1)
    val koulutusala2List               = getListParamAsScalaList(koulutusalat2)
    val koulutusala3List               = getListParamAsScalaList(koulutusalat3)
    val maakuntaList                   = getListParamAsScalaList(maakunnat).map("maakunta_" + _)
    val kuntaList                      = getListParamAsScalaList(kunnat).map("kunta_" + _)
    val opetuskieliList =
      getListParamAsScalaList(opetuskielet).map("oppilaitoksenopetuskieli_" + _)
    val harkinnanvaraisuusList = getListParamAsScalaList(harkinnanvaraisuudet)
    
    val validationErrors = validateHakeneetHyvaksytytVastaanottaneetParams(
      hakuList,
      tulostustapa,
      maybeKoulutustoimija,
      oppilaitosList,
      toimipisteList,
      hakukohdeList,
      koulutusala1List,
      koulutusala2List,
      koulutusala3List,
      opetuskieliList,
      maakuntaList,
      kuntaList,
      harkinnanvaraisuusList,
      maybeSukupuoli,
      naytaHakutoiveet,
    )

    val raporttiParams = Map(
      "haut" -> Option(hakuList).filterNot(_.isEmpty),
      "tulostustapa" -> Option(tulostustapaValinta),
      "koulutustoimija" -> maybeKoulutustoimija,
      "oppilaitokset" -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipisteet" -> Option(toimipisteList).filterNot(_.isEmpty),
      "hakukohteet" -> Option(hakukohdeList).filterNot(_.isEmpty),
      "koulutusalat1" -> Option(koulutusalat1).filterNot(_.isEmpty),
      "koulutusalat2" -> Option(koulutusalat2).filterNot(_.isEmpty),
      "koulutusalat3" -> Option(koulutusalat3).filterNot(_.isEmpty),
      "opetuskielet" -> Option(opetuskieliList).filterNot(_.isEmpty),
      "maakunnat" -> Option(maakuntaList).filterNot(_.isEmpty),
      "kunnat" -> Option(kuntaList).filterNot(_.isEmpty),
      "harkinnanvaraisuudet" -> Option(harkinnanvaraisuusList).filterNot(_.isEmpty),
      "naytaHakutoiveet" -> naytaHakutoiveetBool,
      "sukupuoli" -> maybeSukupuoli
    ).collect { case (key, Some(value)) => key -> value } // jätetään pois tyhjät parametrit


    handleExcelRequest(
      validationErrors = validationErrors,
      response = response,
      request = request,
      id = "hakeneet-hyvaksytyt-vastaanottaneet",
      raporttiParams = raporttiParams,
      auditOperation = HakeneetHyvaksytytVastaanottaneet,
      mapper = mapper
    ) {
      hakeneetHyvaksytytVastaanottaneetService.get(
        hakuList,
        tulostustapaValinta,
        maybeKoulutustoimija,
        oppilaitosList,
        toimipisteList,
        hakukohdeList,
        koulutusala1List,
        koulutusala2List,
        koulutusala3List,
        opetuskieliList,
        maakuntaList,
        kuntaList,
        harkinnanvaraisuusList,
        maybeSukupuoli,
        naytaHakutoiveetBool
      )
    }
  }

  @GetMapping(path = Array("kk-hakeneet-hyvaksytyt-vastaanottaneet"))
  def kk_hakeneet_hyvaksytyt_vastaanottaneet(
      @RequestParam("haut") haut: java.util.Collection[String],
      @RequestParam("tulostustapa") tulostustapa: String,
      @RequestParam("koulutustoimija", required = false) koulutustoimija: String,
      @RequestParam("oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
      @RequestParam("toimipisteet", required = false) toimipisteet: java.util.Collection[String],
      @RequestParam("hakukohteet", required = false) hakukohteet: java.util.Collection[String],
      @RequestParam("hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String],
      @RequestParam("okm-ohjauksen-alat", required = false) okmOhjauksenAlat: java.util.Collection[String],
      @RequestParam("tutkinnon-tasot", required = false) tutkinnonTasot: java.util.Collection[String],
      @RequestParam("aidinkielet", required = false) aidinkielet: java.util.Collection[String],
      @RequestParam("kansalaisuusluokat", required = false) kansalaisuusluokat: java.util.Collection[String],
      @RequestParam("sukupuoli", required = false) sukupuoli: String,
      @RequestParam("ensikertalainen", required = false) ensikertalainen: String,
      @RequestParam("nayta-hakutoiveet", required = false) naytaHakutoiveet: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val maybeKoulutustoimija = Option(koulutustoimija)
    val tulostustapaValinta = Option(tulostustapa).getOrElse("hakukohteittain")
    val naytaHakutoiveetBool = strToOptionBoolean(naytaHakutoiveet).getOrElse(true)
    val maybeSukupuoli: Option[String] = if (sukupuoli == "neutral") None else Option(sukupuoli)
    val maybeEnsikertalainen: Option[Boolean] = strToOptionBoolean(ensikertalainen)
    val hakuList = getListParamAsScalaList(haut)
    val oppilaitosList = getListParamAsScalaList(oppilaitokset)
    val toimipisteList = getListParamAsScalaList(toimipisteet)
    val hakukohdeList = getListParamAsScalaList(hakukohteet)
    val hakukohderyhmaList = getListParamAsScalaList(hakukohderyhmat)
    val okmOhjauksenAlaList = getListParamAsScalaList(okmOhjauksenAlat)
    val tutkinnonTasoList = getListParamAsScalaList(tutkinnonTasot)
    val aidinkieliList = getListParamAsScalaList(aidinkielet)
    val kansalaisuusList = getListParamAsScalaList(kansalaisuusluokat)

    val validationErrors = validateKkHakeneetHyvaksytytVastaanottaneetParams(
      hakuList,
      tulostustapa,
      maybeKoulutustoimija,
      oppilaitosList,
      toimipisteList,
      hakukohdeList,
      hakukohderyhmaList,
      okmOhjauksenAlaList,
      tutkinnonTasoList,
      aidinkieliList,
      kansalaisuusList,
      maybeSukupuoli,
      ensikertalainen,
      naytaHakutoiveet,
    )

    val raporttiParams = Map(
      "haut" -> Option(hakuList).filterNot(_.isEmpty),
      "tulostustapa" -> Option(tulostustapaValinta),
      "koulutustoimija" -> maybeKoulutustoimija,
      "oppilaitokset" -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipisteet" -> Option(toimipisteList).filterNot(_.isEmpty),
      "hakukohteet" -> Option(hakukohdeList).filterNot(_.isEmpty),
      "hakukohderyhmat" -> Option(hakukohderyhmaList).filterNot(_.isEmpty),
      "okmOhjauksenAlat" -> Option(okmOhjauksenAlaList).filterNot(_.isEmpty),
      "tutkinnonTasot" -> Option(tutkinnonTasoList).filterNot(_.isEmpty),
      "aidinkielet" -> Option(aidinkieliList).filterNot(_.isEmpty),
      "kansalaisuusluokat" -> Option(kansalaisuusList).filterNot(_.isEmpty),
      "sukupuoli" -> maybeSukupuoli,
      "ensikertalainen" -> maybeEnsikertalainen,
      "naytaHakutoiveet" -> naytaHakutoiveetBool
    ).collect { case (key, Some(value)) => key -> value }

    handleExcelRequest(
      validationErrors = validationErrors,
      response = response,
      request = request,
      id = "kk-hakeneet-hyvaksytyt-vastaanottaneet",
      raporttiParams = raporttiParams,
      auditOperation = KkHakeneetHyvaksytytVastaanottaneet,
      mapper = mapper
    ) {
      kkHakeneetHyvaksytytVastaanottaneetService.get(
        hakuList,
        tulostustapaValinta,
        maybeKoulutustoimija,
        oppilaitosList,
        toimipisteList,
        hakukohdeList,
        hakukohderyhmaList,
        okmOhjauksenAlaList,
        tutkinnonTasoList,
        aidinkieliList,
        kansalaisuusList,
        maybeSukupuoli,
        maybeEnsikertalainen,
        naytaHakutoiveetBool
      )
    }
  }

}
