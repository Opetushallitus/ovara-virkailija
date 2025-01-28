package fi.oph.ovara.backend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.gson.{JsonObject, JsonParser, JsonPrimitive}
import fi.oph.ovara.backend.raportointi.Controller
import fi.oph.ovara.backend.service.{CommonService, KoulutuksetToteutuksetHakukohteetService, UserService}
import fi.oph.ovara.backend.utils.{AuditLog, AuditOperation}
import fi.vm.sade.auditlog.{Audit, Changes, Logger, Target, User}
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.*

class ControllerSpec extends AnyFlatSpec with Matchers {

  "koulutukset_toteutukset_hakukohteet" should "log audit parameters correctly" in {
    val mockCommonService = mock(classOf[CommonService])
    val mockKoulutuksetToteutuksetHakukohteetService = mock(classOf[KoulutuksetToteutuksetHakukohteetService])
    val mockUserService = mock(classOf[UserService])
    val mockRequest = mock(classOf[HttpServletRequest])
    val mockResponse = mock(classOf[HttpServletResponse])
    val mockAudit = mock(classOf[Audit])
    val mockLogger = mock(classOf[Logger])
    val mockUser = mock(classOf[User])

    val mockAuditLog = new AuditLog(mockLogger) {
      override val audit = mockAudit
      override def getUser(request: HttpServletRequest): User = mockUser
    }

    val controller = new Controller(mockCommonService, mockKoulutuksetToteutuksetHakukohteetService, mockUserService, mockAuditLog)

    val alkamiskausi = List("2023").asJava
    val haku = List("haku1").asJava
    val koulutustoimija = "koulutustoimija1"
    val oppilaitos = List("oppilaitos1").asJava
    val toimipiste = List("toimipiste1").asJava
    val koulutuksenTila = "tila1"
    val toteutuksenTila = "tila2"
    val hakukohteenTila = "tila3"
    val valintakoe = "true"

    controller.koulutukset_toteutukset_hakukohteet(
      alkamiskausi,
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
    val targetJsonStr = target.asJson().toString

    // JSON-vertailu helpompaa tällä kuin auditlog-kirjaston käyttämällä Gsonilla
    // joten pientä konversiokikkailua
    val objectMapper = new ObjectMapper()
    objectMapper.registerModule(DefaultScalaModule)

    val paramsJson = objectMapper.readTree(targetJsonStr).get("parametrit").asText()
    val paramsMap = objectMapper.readValue(paramsJson, classOf[Map[String, Any]])

    val expectedMap = Map(
      "valintakoe" -> true,
      "toteutuksenTila" -> "tila2",
      "koulutustoimija" -> "koulutustoimija1",
      "haku" -> List("haku1"),
      "toimipiste" -> List("toimipiste1"),
      "koulutuksenTila" -> "tila1",
      "oppilaitos" -> List("oppilaitos1"),
      "alkamiskausi" -> List("2023"),
      "hakukohteenTila" -> "tila3"
    )

    // Compare the contents of actualMap with the expectedMap
    paramsMap shouldEqual expectedMap
  }
}