package fi.oph.ovara.backend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.ovara.backend.raportointi.{Controller, ErrorResponse}
import fi.oph.ovara.backend.service.*
import fi.oph.ovara.backend.utils.{AuditLog, AuditOperation}
import fi.vm.sade.auditlog.*
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json4s.jackson.JsonMethods.mapper
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.{PrintWriter, StringWriter}
import java.util
import scala.jdk.CollectionConverters.*

class ControllerSpec extends AnyFlatSpec with Matchers {

  "koulutukset_toteutukset_hakukohteet" should "log audit parameters correctly" in {
    val mockCommonService                            = mock(classOf[CommonService])
    val mockKoulutuksetToteutuksetHakukohteetService = mock(classOf[KoulutuksetToteutuksetHakukohteetService])
    val mockKkKoulutuksetToteutuksetHakukohteetService = mock(classOf[KorkeakouluKoulutuksetToteutuksetHakukohteetService])
    val mockUserService                              = mock(classOf[UserService])
    val mockHakijatService                           = mock(classOf[ToisenAsteenHakijatService])
    val mockKkHakijatService                         = mock(classOf[KkHakijatService])
    val mockHakeneetHyvaksytytVastaanottaneetService = mock(classOf[HakeneetHyvaksytytVastaanottaneetService])
    val mockKkHakeneetHyvaksytytVastaanottaneetService = mock(classOf[KkHakeneetHyvaksytytVastaanottaneetService])
    val mockRequest                                  = mock(classOf[HttpServletRequest])
    val mockResponse                                 = mock(classOf[HttpServletResponse])
    val mockAudit                                    = mock(classOf[Audit])
    val mockLogger                                   = mock(classOf[Logger])
    val mockUser                                     = mock(classOf[User])

    val mockAuditLog = new AuditLog(mockLogger) {
      override val audit                                      = mockAudit
      override def getUser(request: HttpServletRequest): User = mockUser
    }

    val controller = new Controller(
      mockCommonService,
      mockKoulutuksetToteutuksetHakukohteetService,
      mockKkKoulutuksetToteutuksetHakukohteetService,
      mockHakijatService,
      mockKkHakijatService,
      mockHakeneetHyvaksytytVastaanottaneetService,
      mockKkHakeneetHyvaksytytVastaanottaneetService,
      mockUserService,
      mockAuditLog
    )

    val haku                                = List("1.2.246.562.29.00000000000000049925").asJava
    val koulutustoimija                     = "1.2.246.562.10.1000000000000000000"
    val oppilaitos                          = List("1.2.246.562.10.1000000000000000001").asJava
    val toimipiste: util.Collection[String] = null
    val koulutuksenTila                     = "julkaistu"
    val toteutuksenTila                     = "julkaistu"
    val hakukohteenTila                     = "julkaistu"
    val valintakoe                          = "true"

    val stringWriter = new StringWriter()
    val printWriter = new PrintWriter(stringWriter)
    when(mockResponse.getWriter).thenReturn(printWriter)

    val fakeWorkbook = new XSSFWorkbook()
    // Mock the service to return a Right value
    when(mockKoulutuksetToteutuksetHakukohteetService.get(
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any()
    )).thenReturn(Right(fakeWorkbook))

    controller.koulutukset_toteutukset_hakukohteet(
      haku,
      koulutustoimija,
      oppilaitos,
      toimipiste,
      koulutuksenTila,
      toteutuksenTila,
      hakukohteenTila,
      valintakoe,
      mockRequest,
      mockResponse
    )

    val targetCaptor = ArgumentCaptor.forClass(classOf[Target])
    verify(mockAudit).log(any[User], any[AuditOperation], targetCaptor.capture(), any[Changes])

    val target: Target = targetCaptor.getValue
    val targetJsonStr  = target.asJson().toString

    // JSON-vertailu helpompaa tällä kuin auditlog-kirjaston käyttämällä Gsonilla
    // joten pientä konversiokikkailua
    val objectMapper = new ObjectMapper()
    objectMapper.registerModule(DefaultScalaModule)

    val paramsJson = objectMapper.readTree(targetJsonStr).get("parametrit").asText()
    val paramsMap  = objectMapper.readValue(paramsJson, classOf[Map[String, Any]])

    val expectedMap = Map(
      "valintakoe"      -> true,
      "toteutuksenTila" -> "julkaistu",
      "koulutustoimija" -> "1.2.246.562.10.1000000000000000000",
      "haku"            -> List("1.2.246.562.29.00000000000000049925"),
      "koulutuksenTila" -> "julkaistu",
      "oppilaitos"      -> List("1.2.246.562.10.1000000000000000001"),
      "hakukohteenTila" -> "julkaistu"
    )

    // Compare the contents of actualMap with the expectedMap
    paramsMap shouldEqual expectedMap
  }

  it should "return 400 for invalid parameters and include all validation errors" in {
    val mockCommonService = mock(classOf[CommonService])
    val mockKoulutuksetToteutuksetHakukohteetService = mock(classOf[KoulutuksetToteutuksetHakukohteetService])
    val mockKkKoulutuksetToteutuksetHakukohteetService = mock(classOf[KorkeakouluKoulutuksetToteutuksetHakukohteetService])
    val mockUserService = mock(classOf[UserService])
    val mockHakijatService = mock(classOf[ToisenAsteenHakijatService])
    val mockKkHakijatService = mock(classOf[KkHakijatService])
    val mockHakeneetHyvaksytytVastaanottaneetService = mock(classOf[HakeneetHyvaksytytVastaanottaneetService])
    val mockKkHakeneetHyvaksytytVastaanottaneetService = mock(classOf[KkHakeneetHyvaksytytVastaanottaneetService])
    val mockRequest = mock(classOf[HttpServletRequest])
    val mockResponse = mock(classOf[HttpServletResponse])
    val mockAudit = mock(classOf[Audit])
    val mockLogger = mock(classOf[Logger])
    val mockUser = mock(classOf[User])
    val mockWriter = mock(classOf[PrintWriter])

    val mockAuditLog = new AuditLog(mockLogger) {
      override val audit = mockAudit

      override def getUser(request: HttpServletRequest): User = mockUser
    }

    val controller = new Controller(
      mockCommonService,
      mockKoulutuksetToteutuksetHakukohteetService,
      mockKkKoulutuksetToteutuksetHakukohteetService,
      mockHakijatService,
      mockKkHakijatService,
      mockHakeneetHyvaksytytVastaanottaneetService,
      mockKkHakeneetHyvaksytytVastaanottaneetService,
      mockUserService,
      mockAuditLog
    )

    val invalidHaku = List("invalid-oid").asJava
    val invalidOppilaitos = List("invalid-oid").asJava
    val koulutustoimija = "1234"
    val toimipiste: util.Collection[String] = null
    val koulutuksenTila = "julkaistu"
    val toteutuksenTila = "julkaistu"
    val hakukohteenTila = "julkaistu"
    val valintakoe = null


    when(mockResponse.getWriter).thenReturn(mockWriter)

    val stringCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

    controller.koulutukset_toteutukset_hakukohteet(
      invalidHaku,
      koulutustoimija,
      invalidOppilaitos,
      toimipiste,
      koulutuksenTila,
      toteutuksenTila,
      hakukohteenTila,
      valintakoe,
      mockRequest,
      mockResponse
    )


    verify(mockWriter).write(stringCaptor.capture())
    val jsonWritten = stringCaptor.getValue

    val objectMapper = new ObjectMapper()
    objectMapper.registerModule(DefaultScalaModule)

    val jsonMap = objectMapper.readValue(jsonWritten, classOf[Map[String, Any]])

    jsonMap("status") shouldEqual 400
    jsonMap("message") shouldEqual "virhe.validointi"

    val details = jsonMap("details").asInstanceOf[List[String]]
    details.length shouldEqual 3
    details should contain ("haut.invalid.oid")
    details should contain ("koulutustoimija.invalid.org")
    details should contain ("oppilaitokset.invalid.org")
  }
}
