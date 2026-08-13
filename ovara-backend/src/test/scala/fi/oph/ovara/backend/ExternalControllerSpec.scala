package fi.oph.ovara.backend

import fi.oph.ovara.backend.raportointi.ExternalController
import fi.oph.ovara.backend.service.{KkPaatettavatOpiskeluoikeudetService, UserService}
import fi.oph.ovara.backend.utils.AuditLog
import fi.vm.sade.auditlog.*
import jakarta.servlet.http.HttpServletRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ExternalControllerSpec extends AnyFlatSpec with Matchers {

  "kkPaatettavatOpiskeluoikeudet" should "return 404 when organisaatio is not found" in {
    val mockKkPaatettavatOpiskeluoikeudetService = mock(classOf[KkPaatettavatOpiskeluoikeudetService])
    val mockUserService                          = mock(classOf[UserService])
    val mockRequest                              = mock(classOf[HttpServletRequest])
    val mockAudit                                = mock(classOf[Audit])
    val mockLogger                               = mock(classOf[Logger])
    val mockUser                                 = mock(classOf[User])

    val mockAuditLog = new AuditLog(mockLogger) {
      override val audit                                      = mockAudit
      override def getUser(request: HttpServletRequest): User = mockUser
    }

    val controller = new ExternalController(
      mockUserService,
      mockKkPaatettavatOpiskeluoikeudetService,
      mockAuditLog
    )

    when(mockUserService.getAuthorities)
      .thenReturn(List("ROLE_APP_OVARA-VIRKAILIJA_KK_YOS_1.2.246.562.10.278170642010"))
    when(mockKkPaatettavatOpiskeluoikeudetService.organisaatioExists(any())).thenReturn(false)

    val exception = intercept[ResponseStatusException] {
      controller.kkPaatettavatOpiskeluoikeudet(
        "1.2.246.562.10.278170642010",
        null,
        null,
        null,
        null,
        null,
        mockRequest
      )
    }

    exception.getStatusCode shouldEqual HttpStatus.NOT_FOUND
    verify(mockKkPaatettavatOpiskeluoikeudetService, never()).getData(any())
  }

  "kkPaatettavatOpiskeluoikeudet" should "return 404 when rajapinta is disabled" in {
    val mockKkPaatettavatOpiskeluoikeudetService = mock(classOf[KkPaatettavatOpiskeluoikeudetService])
    val mockUserService                          = mock(classOf[UserService])
    val mockRequest                              = mock(classOf[HttpServletRequest])
    val mockAudit                                = mock(classOf[Audit])
    val mockLogger                               = mock(classOf[Logger])
    val mockUser                                 = mock(classOf[User])

    val mockAuditLog = new AuditLog(mockLogger) {
      override val audit                                      = mockAudit
      override def getUser(request: HttpServletRequest): User = mockUser
    }

    val controller = new ExternalController(
      mockUserService,
      mockKkPaatettavatOpiskeluoikeudetService,
      mockAuditLog,
      yosJsonRajapintaEnabled = false
    )

    val exception = intercept[ResponseStatusException] {
      controller.kkPaatettavatOpiskeluoikeudet(
        "1.2.246.562.10.278170642010",
        null,
        null,
        null,
        null,
        null,
        mockRequest
      )
    }

    exception.getStatusCode shouldEqual HttpStatus.NOT_FOUND
    verify(mockUserService, never()).getAuthorities
    verify(mockKkPaatettavatOpiskeluoikeudetService, never()).getData(any())
  }
}
