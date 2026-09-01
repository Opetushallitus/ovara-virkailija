package fi.oph.ovara.backend.external.kkhakijat

import com.fasterxml.jackson.databind.ObjectMapper
import fi.oph.ovara.backend.opiskelijavalintatieto.ValidationError
import fi.oph.ovara.backend.service.UserService
import fi.oph.ovara.backend.utils.ParameterValidator.{
  validateHakukohdeOid,
  validateHakukohderyhmaOid,
  validateHenkiloOid,
  validateOid,
  validateOrganisaatioOid
}
import fi.oph.ovara.backend.utils.{
  ApiException,
  AuditLog,
  AuditLogObj,
  AuditOperation,
  AuthoritiesUtil,
  Constants,
  ControllerUtils
}
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.http.{HttpHeaders, HttpStatus}
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters.*

@RestController
@RequestMapping(path = Array("api/external"))
class ExternalKKHakijatController(
  val userService: UserService,
  kkHakijatService: ExternalKKHakijatService,
  mapper: ObjectMapper,
  auditLog: AuditLog
) extends ControllerUtils(auditLog) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalKKHakijatController])

  private def auditParams(
    format: String,
    hakuOid: Option[String],
    hakukohdeOid: Option[String],
    hakukohderyhmaOid: Option[String],
    organisaatioOid: Option[String],
    valintarajaus: Option[String],
    oppijanumero: Option[String]
  ): Map[String, Any] = Map(
    "format"            -> format,
    "hakuOid"           -> hakuOid.getOrElse(""),
    "hakukohdeOid"      -> hakukohdeOid.getOrElse(""),
    "hakukohderyhmaOid" -> hakukohderyhmaOid.getOrElse(""),
    "organisaatioOid"   -> organisaatioOid.getOrElse(""),
    "valintarajaus"     -> valintarajaus.getOrElse(""),
    "oppijanumero"      -> oppijanumero.getOrElse("")
  )

  /**
   * Oppijanumerohaku ei tarvitse hakuOidia eikä muita rajaimia, joten niiden pakollisuus riippuu
   * siitä onko oppijanumero annettu. Annettuina ne rajaavat edelleen normaalisti.
   */
  private def validationErrors(
    hakuOid: Option[String],
    hakukohdeOid: Option[String],
    hakukohderyhmaOid: Option[String],
    organisaatioOid: Option[String],
    valintarajaus: Option[String],
    parsedRajaus: Option[Valintarajaus],
    oppijanumero: Option[String]
  ): List[String] = List(
    validateOid(hakuOid, "hakuOid"),
    validateHakukohdeOid(hakukohdeOid, "hakukohdeOid"),
    validateHakukohderyhmaOid(hakukohderyhmaOid, "hakukohderyhmaOid"),
    validateOrganisaatioOid(organisaatioOid, "organisaatioOid"),
    validateHenkiloOid(oppijanumero, "oppijanumero"),
    Option.when(hakuOid.isEmpty && oppijanumero.isEmpty)("hakuOid.required"),
    // Ryhmä laajennetaan hakukohteiksi haun sisällä, joten sitä ei voi antaa ilman hakua.
    Option.when(hakukohderyhmaOid.isDefined && hakuOid.isEmpty)("hakuOid.required.with.hakukohderyhmaOid"),
    Option.when(
      oppijanumero.isEmpty && hakukohdeOid.isEmpty && hakukohderyhmaOid.isEmpty && organisaatioOid.isEmpty
    )(
      "hakukohdeOid_or_organisaatioOid_or_hakukohderyhmaOid.required"
    ),
    Option.when(valintarajaus.isEmpty && oppijanumero.isEmpty)("valintarajaus.invalid"),
    Option.when(valintarajaus.isDefined && parsedRajaus.isEmpty)("valintarajaus.invalid")
  ).flatten

  /**
   * Vain KK_HAKENEET_-etuliitteellä myönnetyt oidit kelpaavat tälle rajapinnalle: 2ASTE /
   * HAKENEET / muiden ovara-roolien kautta saatuja oideja ei yhdistetä mukaan.
   */
  private def omatKayttooikeusOidit(authorities: List[String]): List[String] =
    AuthoritiesUtil.getKayttooikeusOids(
      authorities.filter(_.startsWith(Constants.KK_HAKENEET_AUTHORITY_PREFIX))
    )

  /**
   * Pääkäyttäjyys tunnistetaan vain tämän rajapinnan omista oikeuksista: joko varsinaisesta
   * OPH_PAAKAYTTAJA-oikeudesta, tai KK_HAKENEET-oikeudesta joka on myönnetty OPH-organisaatiolle.
   * Muun ovara-roolin (2ASTE, KK, HAKENEET, ...) kautta saatu OPH-oid ei laajenna näkymää --
   * toisin kuin sisäisissä raporteissa, ks. CommonService.
   */
  private def isOphPaakayttaja(authorities: List[String]): Boolean =
    authorities.contains(Constants.OPH_PAAKAYTTAJA_AUTHORITY) ||
      AuthoritiesUtil.hasOPHPaakayttajaRights(omatKayttooikeusOidit(authorities))

  /**
   * Erillinen "kaikki KK-hakijat" -oikeus (OILI). Ei organisaatio- eikä ryhmärajausta.
   * Prefiksivertailu, koska oikeus voi tulla sekä suffiksittomana että organisaatiolle
   * myönnettynä -- kumpikin muoto tarkoittaa tässä samaa täyttä oikeutta.
   */
  private def hasKaikkiTiedotOikeus(authorities: List[String]): Boolean =
    authorities.exists(_.startsWith(Constants.OILI_AUTHORITY_PREFIX))

  // OPH_PAAKAYTTAJA, OILI-oikeus ja vähintään yksi KK_HAKENEET_<oid> -oikeus
  // kaikki kelpuuttavat kutsujan tälle rajapinnalle.
  private def isAuthorized: Boolean = {
    val authorities = userService.getAuthorities
    isOphPaakayttaja(authorities) ||
    hasKaikkiTiedotOikeus(authorities) ||
    authorities.exists(_.startsWith(Constants.KK_HAKENEET_AUTHORITY_PREFIX))
  }

  private def requireAuthorized[T](f: => T): T =
    if (isAuthorized) f
    else throw org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN)

  /**
   * Oppijanumerohaku edellyttää oikeutta kaikkiin tämän rajapinnan tietoihin: haku ei ole
   * organisaatio- eikä ryhmärajattu ja sen pitää pystyä palauttamaan kaikki haetun henkilön
   * hakukohteet, joten rajatulle käyttäjälle sitä ei sallita. Tyhjä tulos
   * ei kelpaa vastaukseksi, koska se ei erottuisi henkilöstä jolla ei ole hakemuksia.
   *
   * Tarkistus tehdään ennen validointia, jottei rajapinta kerro oikeudettomalle kutsujalle
   * parametrien kelvollisuudesta.
   */
  private def hasOppijanumeroOikeus: Boolean = resolveKayttooikeusScope.saaKaikkiTiedot

  private def requireOppijanumeroOikeus(oppijanumero: Option[String]): Unit =
    if (oppijanumero.isDefined && !hasOppijanumeroOikeus)
      throw org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN)

  /**
   * Oppijanumerohaussa valintarajaus on vapaaehtoinen ja tarkoittaa oletuksena "kaikki tiedot",
   * eli HAKENEET, joka ei suodata valintatiedon perusteella.
   */
  private def valintarajausTaiOletus(rajaus: Option[String]): Valintarajaus =
    rajaus.flatMap(Valintarajaus.parse).getOrElse(Valintarajaus.HAKENEET)

  private def resolveKayttooikeusScope: KayttooikeusScopeKK = {
    val authorities = userService.getAuthorities
    if (isOphPaakayttaja(authorities)) {
      KayttooikeusScopeKK.paakayttaja
    } else if (hasKaikkiTiedotOikeus(authorities)) {
      KayttooikeusScopeKK.kaikkiTiedot
    } else {
      // Käyttöoikeus voi olla myönnetty organisaatiolle tai hakukohderyhmälle (oid-avaruus
      // 1.2.246.562.28.*), ja nämä eritellään oid-avaruuden perusteella: organisaatio-oikeutta
      // verrataan järjestyspaikkaan, ryhmäoikeus laajennetaan palvelussa haun hakukohteiksi.
      val kkHakeneetOids              = omatKayttooikeusOidit(authorities)
      val organisaatiotJoihinOikeus   = AuthoritiesUtil.filterOrganisaatioOids(kkHakeneetOids)
      val hakukohderyhmatJoihinOikeus = AuthoritiesUtil.filterHakukohderyhmaOids(kkHakeneetOids)
      KayttooikeusScopeKK.limited(organisaatiotJoihinOikeus.toSet, hakukohderyhmatJoihinOikeus.toSet)
    }
  }

  @GetMapping(path = Array("kkhakijat"))
  @Operation(
    summary = "Palauttaa KK-hakijoiden tiedot haun ja hakukohteen, hakukohderyhmän tai organisaation perusteella",
    description = "Vaatii hakuOid-parametrin sekä vähintään yhden rajaavista parametreista " +
      "hakukohdeOid, hakukohderyhmaOid tai organisaatioOid. Useampi rajaava parametri leikataan " +
      "keskenään: hakukohderyhmaOid laajennetaan ryhmään kuuluviksi hakukohteiksi. " +
      "Vaihtoehtoisesti voi antaa oppijanumero-parametrin, jolla palautetaan yhden henkilön " +
      "kaikki tiedot kaikista hauista. Tällöin muita parametreja ei tarvita, mutta annettuina ne " +
      "rajaavat tulosta edelleen (esim. oppijanumero + hakuOid = henkilön tiedot tuossa haussa) " +
      "ja valintarajaus on oletuksena HAKENEET. Parametriksi kelpaa oppijanumero tai mikä tahansa " +
      "siihen linkitetty henkilö-oid: kaikki henkilön aliakset ja niiden hakemukset palautuvat " +
      "riippumatta siitä, minkä oidin antaa. Oppijanumerohaku vaatii oikeuden kaikkiin tämän " +
      "rajapinnan tietoihin (rekisterinpitäjä tai OILI-oikeus); muuten vastaus on 403. " +
      "Tulos rajataan lisäksi käyttäjän oikeuksiin: organisaatio-oikeus kattaa organisaation " +
      "(ja sen alaorganisaatioiden) järjestämät hakukohteet, hakukohderyhmäoikeus ryhmään " +
      "kuuluvat hakukohteet. Jos pyynnössä annetaan organisaatioOid, se on katettava " +
      "organisaatio-oikeuksilla -- hakukohderyhmäoikeuksia ei tällöin huomioida. " +
      "Palauttaa tyhjän listan, jos hakijoita ei löydy. " +
      "Vain hakemuspalvelun (Ataru) hakemukset korkeakoulujen yhteishausta palautetaan.",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Hakijoiden tiedot."),
      new ApiResponse(
        responseCode = "400",
        description = "Validointivirhe",
        content = Array(new Content(schema = new Schema(implementation = classOf[ValidationError])))
      ),
      new ApiResponse(responseCode = "403", description = "403 virhe", content = Array(new Content())),
      new ApiResponse(
        responseCode = "500",
        description = "Muu virhe",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))
      )
    )
  )
  def getHakijat(
    @RequestParam(value = "hakuOid", required = false) hakuOid: String,
    @RequestParam(value = "hakukohdeOid", required = false) hakukohdeOid: String,
    @RequestParam(value = "hakukohderyhmaOid", required = false) hakukohderyhmaOid: String,
    @RequestParam(value = "organisaatioOid", required = false) organisaatioOid: String,
    @RequestParam(value = "valintarajaus", required = false) valintarajaus: String,
    @RequestParam(value = "oppijanumero", required = false) oppijanumero: String,
    request: HttpServletRequest
  ): KKHakijatResponse =
    requireAuthorized {
      val haku           = Option(hakuOid).filter(_.nonEmpty)
      val hakukohde      = Option(hakukohdeOid).filter(_.nonEmpty)
      val hakukohderyhma = Option(hakukohderyhmaOid).filter(_.nonEmpty)
      val organisaatio   = Option(organisaatioOid).filter(_.nonEmpty)
      val rajaus         = Option(valintarajaus).filter(_.nonEmpty)
      val oppija         = Option(oppijanumero).filter(_.nonEmpty)

      requireOppijanumeroOikeus(oppija)

      validate {
        validationErrors(
          haku,
          hakukohde,
          hakukohderyhma,
          organisaatio,
          rajaus,
          rajaus.flatMap(Valintarajaus.parse),
          oppija
        )
      }

      LOG.info(
        s"Haetaan KK-hakijat. HakuOid: $hakuOid, HakukohdeOid: $hakukohdeOid, " +
          s"HakukohderyhmaOid: $hakukohderyhmaOid, OrganisaatioOid: $organisaatioOid, " +
          s"Valintarajaus: $valintarajaus, oppijanumerohaku: ${oppija.isDefined}"
      )
      auditLog.logWithParams(
        request,
        AuditOperation.ExternalKKHakijat,
        auditParams("json", haku, hakukohde, hakukohderyhma, organisaatio, rajaus, oppija)
      )
      handleRequest {
        kkHakijatService
          .getKKHakijat(
            haku,
            hakukohde,
            organisaatio,
            valintarajausTaiOletus(rajaus),
            resolveKayttooikeusScope,
            hakukohderyhma,
            oppija
          )
          .map(KKHakijatResponse.apply)
      }
    }

  @GetMapping(path = Array("kkhakijat/excel"))
  @Operation(
    summary =
      "Palauttaa KK-hakijoiden tiedot Excel-muodossa haun ja hakukohteen, hakukohderyhmän tai organisaation perusteella",
    description = "Sama haku- ja käyttöoikeuslogiikka kuin JSON-rajapinnassa. Palauttaa .xlsx-tiedoston.",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Hakijoiden tiedot Excel-muodossa."),
      new ApiResponse(
        responseCode = "400",
        description = "Validointivirhe",
        content = Array(new Content(schema = new Schema(implementation = classOf[ValidationError])))
      ),
      new ApiResponse(responseCode = "403", description = "403 virhe", content = Array(new Content())),
      new ApiResponse(
        responseCode = "500",
        description = "Muu virhe",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))
      )
    )
  )
  def getHakijatExcel(
    @RequestParam(value = "hakuOid", required = false) hakuOid: String,
    @RequestParam(value = "hakukohdeOid", required = false) hakukohdeOid: String,
    @RequestParam(value = "hakukohderyhmaOid", required = false) hakukohderyhmaOid: String,
    @RequestParam(value = "organisaatioOid", required = false) organisaatioOid: String,
    @RequestParam(value = "valintarajaus", required = false) valintarajaus: String,
    @RequestParam(value = "oppijanumero", required = false) oppijanumero: String,
    request: HttpServletRequest,
    response: HttpServletResponse
  ): Unit = {
    val haku           = Option(hakuOid).filter(_.nonEmpty)
    val hakukohde      = Option(hakukohdeOid).filter(_.nonEmpty)
    val hakukohderyhma = Option(hakukohderyhmaOid).filter(_.nonEmpty)
    val organisaatio   = Option(organisaatioOid).filter(_.nonEmpty)
    val rajaus         = Option(valintarajaus).filter(_.nonEmpty)
    val oppija         = Option(oppijanumero).filter(_.nonEmpty)
    val parsedRajaus   = rajaus.flatMap(Valintarajaus.parse)

    if (!isAuthorized || (oppija.isDefined && !hasOppijanumeroOikeus)) {
      response.setStatus(HttpStatus.FORBIDDEN.value())
      return
    }

    val errors =
      validationErrors(haku, hakukohde, hakukohderyhma, organisaatio, rajaus, parsedRajaus, oppija)

    if (errors.nonEmpty) {
      response.setStatus(HttpStatus.BAD_REQUEST.value())
      response.setContentType("application/json")
      val payload = mapper.writeValueAsString(
        Map(
          "status"  -> HttpStatus.BAD_REQUEST.value(),
          "message" -> "virhe.validointi",
          "details" -> errors.asJava
        )
      )
      response.getWriter.write(payload)
      return
    }

    LOG.info(
      s"Haetaan KK-hakijat Excel-muodossa. HakuOid: $hakuOid, HakukohdeOid: $hakukohdeOid, " +
        s"HakukohderyhmaOid: $hakukohderyhmaOid, OrganisaatioOid: $organisaatioOid, " +
        s"Valintarajaus: $valintarajaus, oppijanumerohaku: ${oppija.isDefined}"
    )
    auditLog.logWithParams(
      request,
      AuditOperation.ExternalKKHakijat,
      auditParams("excel", haku, hakukohde, hakukohderyhma, organisaatio, rajaus, oppija)
    )

    kkHakijatService.getKKHakijat(
      haku,
      hakukohde,
      organisaatio,
      valintarajausTaiOletus(rajaus),
      resolveKayttooikeusScope,
      hakukohderyhma,
      oppija
    ) match {
      case Left(errorKey) =>
        LOG.error(s"Excel-raportin haku epäonnistui: $errorKey")
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
        response.setContentType("application/json")
        response.getWriter.write(mapper.writeValueAsString(errorKey))

      case Right(hakijat) =>
        val workbook  = ExternalKKHakijatExcelWriter.write(hakijat)
        val timestamp = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val filename  = s"kkhakijat-$timestamp.xlsx"
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, s"attachment; filename=$filename")
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
        try {
          workbook.write(response.getOutputStream)
        } finally {
          workbook.close()
        }
    }
  }

  private def handleRequest[T](block: => Either[String, T]): T =
    block match {
      case Right(result)      => result
      case Left(errorMessage) => throw ApiException(errorMessage)
    }
}
