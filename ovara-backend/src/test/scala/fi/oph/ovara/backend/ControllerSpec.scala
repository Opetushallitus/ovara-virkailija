//package fi.oph.ovara.backend
//
//
//import com.google.gson.JsonObject
//import com.google.gson.JsonParser
//import fi.oph.ovara.backend.raportointi.Controller
//import fi.oph.ovara.backend.service.{CommonService, KoulutuksetToteutuksetHakukohteetService, UserService}
//import fi.oph.ovara.backend.utils.{AuditLog, AuditOperation}
//import fi.vm.sade.auditlog.{Audit, Changes, Logger, Target, User}
//import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
//import org.mockito.ArgumentCaptor
//import org.mockito.ArgumentMatchers.any
//import org.mockito.Mockito.*
//import org.scalatest.flatspec.AnyFlatSpec
//import org.scalatest.matchers.should.Matchers
//
//import scala.jdk.CollectionConverters.*
//
//class ControllerSpec extends AnyFlatSpec with Matchers {
//
//  "koulutukset_toteutukset_hakukohteet" should "log audit parameters correctly" in {
//    val mockCommonService = mock(classOf[CommonService])
//    val mockKoulutuksetToteutuksetHakukohteetService = mock(classOf[KoulutuksetToteutuksetHakukohteetService])
//    val mockUserService = mock(classOf[UserService])
//    val mockRequest = mock(classOf[HttpServletRequest])
//    val mockResponse = mock(classOf[HttpServletResponse])
//    val mockAudit = mock(classOf[Audit])
//    val mockLogger = mock(classOf[Logger])
//    val mockUser = mock(classOf[User])
//
//    val controller = new Controller(mockCommonService, mockKoulutuksetToteutuksetHakukohteetService, mockUserService) {
//      override val auditLog = new AuditLog(mockLogger) {
//        override val audit = mockAudit
//        override def getUser(request: HttpServletRequest): User = mockUser
//      }
//    }
//
//    val alkamiskausi = List("2023").asJava
//    val haku = List("haku1").asJava
//    val koulutustoimija = "koulutustoimija1"
//    val oppilaitos = List("oppilaitos1").asJava
//    val toimipiste = List("toimipiste1").asJava
//    val koulutuksenTila = "tila1"
//    val toteutuksenTila = "tila2"
//    val hakukohteenTila = "tila3"
//    val valintakoe = "true"
//
//    controller.koulutukset_toteutukset_hakukohteet(
//      alkamiskausi,
//      haku,
//      koulutustoimija,
//      oppilaitos,
//      toimipiste,
//      koulutuksenTila,
//      toteutuksenTila,
//      hakukohteenTila,
//      valintakoe,
//      mockRequest,
//      mockResponse
//    )
//
//    val targetCaptor = ArgumentCaptor.forClass(classOf[Target])
//    verify(mockAudit).log(any[User], AuditOperation], targetCaptor.capture(), any[Changes])
//
//    val target: Target = targetCaptor.getValue
//    val targetJson: JsonObject = JsonParser.parseString(target.asJson().toString).getAsJsonObject
//
//    val expectedJson = """{"parametrit":"{\"alkamiskausi\":[\"2023\"],\"haku\":[\"haku1\"],\"koulutustoimija\":\"koulutustoimija1\",\"oppilaitos\":[\"oppilaitos1\"],\"toimipiste\":[\"toimipiste1\"],\"koulutuksenTila\":\"tila1\",\"toteutuksenTila\":\"tila2\",\"hakukohteenTila\":\"tila3\",\"valintakoe\":true}"}"""
//    targetJson.toString shouldEqual expectedJson
//  }
//}
