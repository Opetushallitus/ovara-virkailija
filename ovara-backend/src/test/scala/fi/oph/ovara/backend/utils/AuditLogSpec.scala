package fi.oph.ovara.backend.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.mockito.Mockito.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import fi.vm.sade.auditlog.{Audit, Changes, Logger, Operation, Target, User}
import jakarta.servlet.http.HttpServletRequest

class AuditLogSpec extends AnyFlatSpec with Matchers {

  "AuditLog" should "log parameters correctly" in {
    val mockLogger = mock(classOf[Logger])
    val mockAudit = mock(classOf[Audit])
    val mockRequest = mock(classOf[HttpServletRequest])
    val mockUser = mock(classOf[User])
    val auditLog = new AuditLog(mockLogger) {
      override val audit = mockAudit
      override def getUser(request: HttpServletRequest): User = mockUser
    }

    val params = List("param1" -> "value1", "param2" -> "value2")
    
    auditLog.logWithParams(mockRequest, AuditOperation.KoulutuksetToteutuksetHakukohteet, params)

    val targetCaptor = ArgumentCaptor.forClass(classOf[Target])
    verify(mockAudit).log(any[User], any[Operation], targetCaptor.capture(), any[Changes])

    val target: Target = targetCaptor.getValue
    val targetJson: JsonObject = JsonParser.parseString(target.asJson().toString).getAsJsonObject

    val expectedJson = """{"parametrit":"{\"param1\":\"value1\",\"param2\":\"value2\"}"}"""
    targetJson.toString shouldEqual expectedJson

  }
}
