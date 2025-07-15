package fi.oph.ovara.backend.raportointi

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.ovara.backend.domain.UserResponse
import fi.oph.ovara.backend.raportointi.dto.{RawHakeneetHyvaksytytVastaanottaneetParams, RawHakijatParams, RawKkHakeneetHyvaksytytVastaanottaneetParams, RawKkHakijatParams, RawKkKoulutuksetToteutuksetHakukohteetParams, RawKoulutuksetToteutuksetHakukohteetParams, buildHakeneetHyvaksytytVastaanottaneetAuditParams, buildHakijatAuditParams, buildKkHakeneetHyvaksytytVastaanottaneetAuditParams, buildKkHakijatAuditParams, buildKkKoulutuksetToteutuksetHakukohteetAuditParams, buildKoulutuksetToteutuksetHakukohteetAuditParams}
import fi.oph.ovara.backend.service.*
import fi.oph.ovara.backend.utils.AuditOperation.{HakeneetHyvaksytytVastaanottaneet, KkHakeneetHyvaksytytVastaanottaneet, KkHakijat, KorkeakouluKoulutuksetToteutuksetHakukohteet, KoulutuksetToteutuksetHakukohteet, ToisenAsteenHakijat}
import fi.oph.ovara.backend.utils.ParameterValidator.{strToOptionBoolean, validateAlphanumeric, validateAlphanumericList, validateHakeneetHyvaksytytVastaanottaneetParams, validateHakijatParams, validateKkHakeneetHyvaksytytVastaanottaneetParams, validateKkHakijatParams, validateKkKoulutuksetToteutuksetHakukohteetParams, validateKoulutuksetToteutuksetHakukohteetParams, validateNumericList, validateOid, validateOidList, validateOrganisaatioOid, validateOrganisaatioOidList}
import fi.oph.ovara.backend.utils.{AuditLog, AuditLogObj, AuditOperation}
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.{HttpHeaders, MediaType, ResponseEntity}
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
    val auditLog: AuditLog = AuditLogObj
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[Controller])

  @Value("${ovara.ui.url}")
  val ovaraUiUrl: String = null

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(SerializationFeature.INDENT_OUTPUT, true)


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
        message = "virhe.validointi",
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
  def response: ResponseEntity[String] = {
    // Palautetaan jokin paluuarvo koska client-kirjasto sellaisen haluaa
    ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_JSON)
      .body(mapper.writeValueAsString(Map("status" -> "ok")))
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
            @RequestParam("ovara_alkamiskaudet", required = false) alkamiskaudet: java.util.Collection[String],
            @RequestParam("ovara_haut", required = false) selectedHaut: java.util.Collection[String],
            @RequestParam("ovara_haun_tyyppi", required = false) haun_tyyppi: String
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
                   @RequestParam("ovara_haut") haut: java.util.Collection[String],
                   @RequestParam("ovara_koulutustoimija", required = false) koulutustoimija: String,
                   @RequestParam("ovara_oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
                   @RequestParam("ovara_toimipisteet", required = false) toimipisteet: java.util.Collection[String],
                   @RequestParam("ovara_hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String],
                   @RequestParam("ovara_hakukohteet", required = false) selectedHakukohteet: java.util.Collection[String]
                 ): ResponseEntity[String] = {
    val errors = List(
      validateOrganisaatioOid(Option(koulutustoimija), "koulutustoimija"),
      validateOrganisaatioOidList(getListParamAsScalaList(oppilaitokset), "oppilaitokset"),
      validateOrganisaatioOidList(getListParamAsScalaList(toimipisteet), "toimipisteet"),
      validateOidList(getListParamAsScalaList(haut), "haut"),
      validateOidList(getListParamAsScalaList(hakukohderyhmat), "hakukohderyhmat"),
      validateOidList(getListParamAsScalaList(selectedHakukohteet), "hakukohteet")
    ).flatten.distinct

    handleRequest(errors, mapper) {
      commonService.getHakukohteet(
        Option(koulutustoimija),
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
              @RequestParam("ovara_maakunnat", required = false) maakunnat: java.util.Collection[String],
              @RequestParam("ovara_selectedKunnat", required = false) selectedKunnat: java.util.Collection[String]
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
                     @RequestParam("ovara_koulutusalat1", required = false) koulutusalat1: java.util.Collection[String],
                     @RequestParam("ovara_selectedKoulutusalat2", required = false) selectedKoulutusalat2: java.util.Collection[String]
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
                     @RequestParam("ovara_koulutusalat2", required = false) koulutusalat2: java.util.Collection[String],
                     @RequestParam("ovara_selectedKoulutusalat3", required = false) selectedKoulutusalat3: java.util.Collection[String]
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
                       @RequestParam("ovara_haut", required = true) haut: java.util.Collection[String]
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
        "message" -> "virhe.validointi",
        "details" -> validationErrors.asJava
      ))
      response.getWriter.write(errorJson)
    } else {
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
  }

  @GetMapping(path = Array("koulutukset-toteutukset-hakukohteet"))
  def koulutukset_toteutukset_hakukohteet(
                                           @RequestParam("ovara_haut") haut: java.util.Collection[String],
                                           @RequestParam("ovara_koulutustoimija", required = false) koulutustoimija: String,
                                           @RequestParam("ovara_oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
                                           @RequestParam("ovara_toimipisteet", required = false) toimipisteet: java.util.Collection[String],
                                           @RequestParam("ovara_koulutuksen-tila", required = false) koulutuksenTila: String,
                                           @RequestParam("ovara_toteutuksen-tila", required = false) toteutuksenTila: String,
                                           @RequestParam("ovara_hakukohteen-tila", required = false) hakukohteenTila: String,
                                           @RequestParam("ovara_valintakoe", required = false) valintakoe: String,
                                           request: HttpServletRequest,
                                           response: HttpServletResponse
                                         ): Unit = {
    val params = RawKoulutuksetToteutuksetHakukohteetParams(
      haut = getListParamAsScalaList(haut),
      koulutustoimija = Option(koulutustoimija),
      oppilaitokset = getListParamAsScalaList(oppilaitokset),
      toimipisteet = getListParamAsScalaList(toimipisteet),
      koulutuksenTila = Option(koulutuksenTila),
      toteutuksenTila = Option(toteutuksenTila),
      hakukohteenTila = Option(hakukohteenTila),
      valintakoe = valintakoe
    )

    val validationResult = validateKoulutuksetToteutuksetHakukohteetParams(params)

    handleExcelRequest(
      validationErrors = validationResult.left.getOrElse(Nil),
      response = response,
      request = request,
      id = "koulutukset-toteutukset-hakukohteet",
      raporttiParams = validationResult.toOption.map(buildKoulutuksetToteutuksetHakukohteetAuditParams).getOrElse(Map.empty),
      auditOperation = KoulutuksetToteutuksetHakukohteet,
      mapper = mapper
    ) {
      validationResult match {
        case Left(_) =>
          Left("virhe.validointi")

        case Right(validParams) =>
          koulutuksetToteutuksetHakukohteetService.get(
            validParams.haut,
            validParams.koulutustoimija,
            validParams.oppilaitokset,
            validParams.toimipisteet,
            validParams.koulutuksenTila,
            validParams.toteutuksenTila,
            validParams.hakukohteenTila,
            validParams.valintakoe
          )
      }
    }
  }

  @GetMapping(path = Array("kk-koulutukset-toteutukset-hakukohteet"))
  def kk_koulutukset_toteutukset_hakukohteet(
                                              @RequestParam("ovara_haut") haut: java.util.Collection[String],
                                              @RequestParam("ovara_tulostustapa", defaultValue = "koulutuksittain") tulostustapa: String,
                                              @RequestParam("ovara_oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
                                              @RequestParam("ovara_toimipisteet", required = false) toimipisteet: java.util.Collection[String],
                                              @RequestParam("ovara_hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String],
                                              @RequestParam("ovara_koulutuksen-tila", required = false) koulutuksenTila: String,
                                              @RequestParam("ovara_toteutuksen-tila", required = false) toteutuksenTila: String,
                                              @RequestParam("ovara_hakukohteen-tila", required = false) hakukohteenTila: String,
                                              @RequestParam("ovara_tutkinnon-tasot", required = false) tutkinnonTasot: java.util.Collection[String],
                                              request: HttpServletRequest,
                                              response: HttpServletResponse
                                            ): Unit = {
    val params = RawKkKoulutuksetToteutuksetHakukohteetParams(
      haut = getListParamAsScalaList(haut),
      tulostustapa = tulostustapa,
      oppilaitokset = getListParamAsScalaList(oppilaitokset),
      toimipisteet = getListParamAsScalaList(toimipisteet),
      hakukohderyhmat = getListParamAsScalaList(hakukohderyhmat),
      koulutuksenTila = Option(koulutuksenTila),
      toteutuksenTila = Option(toteutuksenTila),
      hakukohteenTila = Option(hakukohteenTila),
      tutkinnonTasot = getListParamAsScalaList(tutkinnonTasot)
    )

    val validationResult = validateKkKoulutuksetToteutuksetHakukohteetParams(params)

    handleExcelRequest(
      validationErrors = validationResult.left.getOrElse(Nil),
      response = response,
      request = request,
      id = "kk-koulutukset-toteutukset-hakukohteet",
      raporttiParams = validationResult.toOption.map(buildKkKoulutuksetToteutuksetHakukohteetAuditParams).getOrElse(Map.empty),
      auditOperation = KorkeakouluKoulutuksetToteutuksetHakukohteet,
      mapper = mapper
    ) {
      validationResult match {
        case Left(_) =>
          Left("virhe.validointi")

        case Right(validParams) =>
          kkKoulutuksetToteutuksetHakukohteetService.get(
            validParams.haut,
            validParams.oppilaitokset,
            validParams.toimipisteet,
            validParams.hakukohderyhmat,
            validParams.koulutuksenTila,
            validParams.toteutuksenTila,
            validParams.hakukohteenTila,
            validParams.tutkinnonTasot,
            validParams.tulostustapa
          )
      }
    }
  }

  @GetMapping(path = Array("hakijat"))
  def hakijat(
      @RequestParam("ovara_haut") haut: java.util.Collection[String],
      @RequestParam("ovara_oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
      @RequestParam("ovara_toimipisteet", required = false) toimipisteet: java.util.Collection[String],
      @RequestParam("ovara_hakukohteet", required = false) hakukohteet: java.util.Collection[String],
      @RequestParam("ovara_pohjakoulutukset", required = false) pohjakoulutukset: java.util.Collection[String],
      @RequestParam("ovara_valintatiedot", required = false) valintatiedot: java.util.Collection[String],
      @RequestParam("ovara_vastaanottotiedot", required = false) vastaanottotiedot: java.util.Collection[String],
      @RequestParam("ovara_harkinnanvaraisuudet", required = false) harkinnanvaraisuudet: java.util.Collection[String],
      @RequestParam("ovara_kaksoistutkinto", required = false) kaksoistutkinto: String,
      @RequestParam("ovara_urheilijatutkinto", required = false) urheilijatutkinto: String,
      @RequestParam("ovara_sora_terveys", required = false) soraterveys: String,
      @RequestParam("ovara_sora_aiempi", required = false) soraAiempi: String,
      @RequestParam("ovara_markkinointilupa", required = false) markkinointilupa: String,
      @RequestParam("ovara_julkaisulupa", required = false) julkaisulupa: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val params = RawHakijatParams(
      haut = getListParamAsScalaList(haut),
      oppilaitokset = getListParamAsScalaList(oppilaitokset),
      toimipisteet = getListParamAsScalaList(toimipisteet),
      hakukohteet = getListParamAsScalaList(hakukohteet),
      pohjakoulutukset = getListParamAsScalaList(pohjakoulutukset),
      valintatiedot = getListParamAsScalaList(valintatiedot),
      vastaanottotiedot = getListParamAsScalaList(vastaanottotiedot),
      harkinnanvaraisuudet = getListParamAsScalaList(harkinnanvaraisuudet),
      kaksoistutkinto = kaksoistutkinto,
      urheilijatutkinto = urheilijatutkinto,
      soraTerveys = soraterveys,
      soraAiempi = soraAiempi,
      markkinointilupa = markkinointilupa,
      julkaisulupa = julkaisulupa
    )

    val validationResult = validateHakijatParams(params)

    handleExcelRequest(
      validationErrors = validationResult.left.getOrElse(Nil),
      response = response,
      request = request,
      id = "hakijat",
      raporttiParams = validationResult.toOption.map(buildHakijatAuditParams).getOrElse(Map.empty),
      auditOperation = ToisenAsteenHakijat,
      mapper = mapper
    ) {
      validationResult match {
        case Left(_) =>
          Left("virhe.validointi")

        case Right(validParams) =>
          hakijatService.get(
            validParams.haut,
            validParams.oppilaitokset,
            validParams.toimipisteet,
            validParams.hakukohteet,
            validParams.pohjakoulutukset,
            validParams.valintatiedot,
            validParams.vastaanottotiedot,
            validParams.harkinnanvaraisuudet,
            validParams.kaksoistutkinto,
            validParams.urheilijatutkinto,
            validParams.soraTerveys,
            validParams.soraAiempi,
            validParams.markkinointilupa,
            validParams.julkaisulupa
          )
      }
    }
  }


  @GetMapping(path = Array("kk-hakijat"))
  def kk_hakijat(
                  @RequestParam("ovara_haut") haut: java.util.Collection[String],
                  @RequestParam("ovara_oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
                  @RequestParam("ovara_toimipisteet", required = false) toimipisteet: java.util.Collection[String],
                  @RequestParam("ovara_hakukohteet", required = false) hakukohteet: java.util.Collection[String],
                  @RequestParam("ovara_valintatiedot", required = false) valintatiedot: java.util.Collection[String],
                  @RequestParam("ovara_vastaanottotiedot", required = false) vastaanottotiedot: java.util.Collection[String],
                  @RequestParam("ovara_hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String],
                  @RequestParam("ovara_kansalaisuusluokat", required = false) kansalaisuusluokat: java.util.Collection[String],
                  @RequestParam("ovara_markkinointilupa", required = false) markkinointilupa: String,
                  @RequestParam("ovara_nayta-yo-arvosanat", required = true) naytaYoArvosanat: String,
                  @RequestParam("ovara_nayta-hetu", required = true) naytaHetu: String,
                  @RequestParam("ovara_nayta-postiosoite", required = true) naytaPostiosoite: String,
                  request: HttpServletRequest,
                  response: HttpServletResponse
                ): Unit = {
    val params = RawKkHakijatParams(
      haut = getListParamAsScalaList(haut),
      oppilaitokset = getListParamAsScalaList(oppilaitokset),
      toimipisteet = getListParamAsScalaList(toimipisteet),
      hakukohteet = getListParamAsScalaList(hakukohteet),
      valintatiedot = getListParamAsScalaList(valintatiedot),
      vastaanottotiedot = getListParamAsScalaList(vastaanottotiedot),
      hakukohderyhmat = getListParamAsScalaList(hakukohderyhmat),
      kansalaisuusluokat = getListParamAsScalaList(kansalaisuusluokat),
      markkinointilupa = markkinointilupa,
      naytaYoArvosanat = naytaYoArvosanat,
      naytaHetu = naytaHetu,
      naytaPostiosoite = naytaPostiosoite
    )

    val validationResult = validateKkHakijatParams(params)

    handleExcelRequest(
      validationErrors = validationResult.left.getOrElse(Nil),
      response = response,
      request = request,
      id = "kk-hakijat",
      raporttiParams = validationResult.toOption.map(buildKkHakijatAuditParams).getOrElse(Map.empty),
      auditOperation = KkHakijat,
      mapper = mapper
    ) {
      validationResult match {
        case Left(_) =>
          Left("virhe.validointi")

        case Right(validParams) =>
          kkHakijatService.get(
            validParams.haut,
            validParams.oppilaitokset,
            validParams.toimipisteet,
            validParams.hakukohteet,
            validParams.valintatiedot,
            validParams.vastaanottotiedot,
            validParams.hakukohderyhmat,
            validParams.kansalaisuusluokat,
            validParams.markkinointilupa,
            validParams.naytaYoArvosanat,
            validParams.naytaHetu,
            validParams.naytaPostiosoite
          )
      }
    }
  }

  @GetMapping(path = Array("hakeneet-hyvaksytyt-vastaanottaneet"))
  def hakeneet_hyvaksytyt_vastaanottaneet(
                                           @RequestParam("ovara_haut") haut: java.util.Collection[String],
                                           @RequestParam("ovara_tulostustapa") tulostustapa: String,
                                           @RequestParam("ovara_koulutustoimija", required = false) koulutustoimija: String,
                                           @RequestParam("ovara_oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
                                           @RequestParam("ovara_toimipisteet", required = false) toimipisteet: java.util.Collection[String],
                                           @RequestParam("ovara_hakukohteet", required = false) hakukohteet: java.util.Collection[String],
                                           @RequestParam("ovara_koulutusalat1", required = false) koulutusalat1: java.util.Collection[String],
                                           @RequestParam("ovara_koulutusalat2", required = false) koulutusalat2: java.util.Collection[String],
                                           @RequestParam("ovara_koulutusalat3", required = false) koulutusalat3: java.util.Collection[String],
                                           @RequestParam("ovara_opetuskielet", required = false) opetuskielet: java.util.Collection[String],
                                           @RequestParam("ovara_maakunnat", required = false) maakunnat: java.util.Collection[String],
                                           @RequestParam("ovara_kunnat", required = false) kunnat: java.util.Collection[String],
                                           @RequestParam("ovara_harkinnanvaraisuudet", required = false) harkinnanvaraisuudet: java.util.Collection[String],
                                           @RequestParam("ovara_nayta-hakutoiveet", required = false) naytaHakutoiveet: String,
                                           @RequestParam("ovara_sukupuoli", required = false) sukupuoli: String,
                                           request: HttpServletRequest,
                                           response: HttpServletResponse
                                         ): Unit = {
    val params = RawHakeneetHyvaksytytVastaanottaneetParams(
      haut = getListParamAsScalaList(haut),
      tulostustapa = tulostustapa,
      koulutustoimija = Option(koulutustoimija),
      oppilaitokset = getListParamAsScalaList(oppilaitokset),
      toimipisteet = getListParamAsScalaList(toimipisteet),
      hakukohteet = getListParamAsScalaList(hakukohteet),
      koulutusalat1 = getListParamAsScalaList(koulutusalat1),
      koulutusalat2 = getListParamAsScalaList(koulutusalat2),
      koulutusalat3 = getListParamAsScalaList(koulutusalat3),
      opetuskielet = getListParamAsScalaList(opetuskielet),
      maakunnat = getListParamAsScalaList(maakunnat),
      kunnat = getListParamAsScalaList(kunnat),
      harkinnanvaraisuudet = getListParamAsScalaList(harkinnanvaraisuudet),
      naytaHakutoiveet = naytaHakutoiveet,
      sukupuoli = Option(sukupuoli)
    )

    val validationResult = validateHakeneetHyvaksytytVastaanottaneetParams(params)

    handleExcelRequest(
      validationErrors = validationResult.left.getOrElse(Nil),
      response = response,
      request = request,
      id = "hakeneet-hyvaksytyt-vastaanottaneet",
      raporttiParams = validationResult.toOption.map(buildHakeneetHyvaksytytVastaanottaneetAuditParams).getOrElse(Map.empty),
      auditOperation = HakeneetHyvaksytytVastaanottaneet,
      mapper = mapper
    ) {
      validationResult match {
        case Left(_) =>
          Left("virhe.validointi")

        case Right(validParams) =>
          hakeneetHyvaksytytVastaanottaneetService.get(
            validParams.haut,
            validParams.tulostustapa,
            validParams.koulutustoimija,
            validParams.oppilaitokset,
            validParams.toimipisteet,
            validParams.hakukohteet,
            validParams.koulutusalat1,
            validParams.koulutusalat2,
            validParams.koulutusalat3,
            validParams.opetuskielet,
            validParams.maakunnat,
            validParams.kunnat,
            validParams.harkinnanvaraisuudet,
            validParams.sukupuoli,
            validParams.naytaHakutoiveet,
          )
      }
    }
  }

  @GetMapping(path = Array("kk-hakeneet-hyvaksytyt-vastaanottaneet"))
  def kk_hakeneet_hyvaksytyt_vastaanottaneet(
                                              @RequestParam("ovara_haut") haut: java.util.Collection[String],
                                              @RequestParam("ovara_tulostustapa") tulostustapa: String,
                                              @RequestParam("ovara_koulutustoimija", required = false) koulutustoimija: String,
                                              @RequestParam("ovara_oppilaitokset", required = false) oppilaitokset: java.util.Collection[String],
                                              @RequestParam("ovara_toimipisteet", required = false) toimipisteet: java.util.Collection[String],
                                              @RequestParam("ovara_hakukohteet", required = false) hakukohteet: java.util.Collection[String],
                                              @RequestParam("ovara_hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String],
                                              @RequestParam("ovara_okm-ohjauksen-alat", required = false) okmOhjauksenAlat: java.util.Collection[String],
                                              @RequestParam("ovara_tutkinnon-tasot", required = false) tutkinnonTasot: java.util.Collection[String],
                                              @RequestParam("ovara_aidinkielet", required = false) aidinkielet: java.util.Collection[String],
                                              @RequestParam("ovara_kansalaisuusluokat", required = false) kansalaisuusluokat: java.util.Collection[String],
                                              @RequestParam("ovara_sukupuoli", required = false) sukupuoli: String,
                                              @RequestParam("ovara_ensikertalainen", required = false) ensikertalainen: String,
                                              @RequestParam("ovara_nayta-hakutoiveet", required = false) naytaHakutoiveet: String,
                                              request: HttpServletRequest,
                                              response: HttpServletResponse
                                            ): Unit = {
    val params = RawKkHakeneetHyvaksytytVastaanottaneetParams(
      haut = getListParamAsScalaList(haut),
      tulostustapa = tulostustapa,
      koulutustoimija = Option(koulutustoimija),
      oppilaitokset = getListParamAsScalaList(oppilaitokset),
      toimipisteet = getListParamAsScalaList(toimipisteet),
      hakukohteet = getListParamAsScalaList(hakukohteet),
      hakukohderyhmat = getListParamAsScalaList(hakukohderyhmat),
      okmOhjauksenAlat = getListParamAsScalaList(okmOhjauksenAlat),
      tutkinnonTasot = getListParamAsScalaList(tutkinnonTasot),
      aidinkielet = getListParamAsScalaList(aidinkielet),
      kansalaisuusluokat = getListParamAsScalaList(kansalaisuusluokat),
      sukupuoli = Option(sukupuoli),
      ensikertalainen = ensikertalainen,
      naytaHakutoiveet = naytaHakutoiveet
    )

    val validationResult = validateKkHakeneetHyvaksytytVastaanottaneetParams(params)

    handleExcelRequest(
      validationErrors = validationResult.left.getOrElse(Nil),
      response = response,
      request = request,
      id = "kk-hakeneet-hyvaksytyt-vastaanottaneet",
      raporttiParams = validationResult.toOption.map(buildKkHakeneetHyvaksytytVastaanottaneetAuditParams).getOrElse(Map.empty),
      auditOperation = KkHakeneetHyvaksytytVastaanottaneet,
      mapper = mapper
    ) {
      validationResult match {
        case Left(_) =>
          Left("virhe.validointi")

        case Right(validParams) =>
          kkHakeneetHyvaksytytVastaanottaneetService.get(
            validParams.haut,
            validParams.tulostustapa,
            validParams.koulutustoimija,
            validParams.oppilaitokset,
            validParams.toimipisteet,
            validParams.hakukohteet,
            validParams.hakukohderyhmat,
            validParams.okmOhjauksenAlat,
            validParams.tutkinnonTasot,
            validParams.aidinkielet,
            validParams.kansalaisuusluokat,
            validParams.sukupuoli,
            validParams.ensikertalainen,
            validParams.naytaHakutoiveet,
          )
      }
    }
  }
}
