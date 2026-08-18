package fi.oph.ovara.backend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import fi.oph.ovara.backend.opiskelijavalintatieto.ValidationError
import fi.oph.ovara.backend.service.UserService
import fi.oph.ovara.backend.utils.Constants.OPH_PAAKAYTTAJA_AUTHORITY
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.http.{HttpHeaders, HttpStatus}
import org.springframework.web.bind.annotation.{ExceptionHandler, ResponseStatus}
import org.springframework.web.server.ResponseStatusException

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util
import scala.jdk.CollectionConverters.*

trait ControllerUtils(auditLog: AuditLog) {
  private val LOG = LoggerFactory.getLogger(classOf[ControllerUtils])

  def userService: UserService

  def getListParamAsScalaList(listParam: util.Collection[String]): List[String] = {
    if (listParam == null) List() else listParam.asScala.toList
  }

  def withPaakayttajaRole[T](f: => T): T = {
    val authorities = userService.getAuthorities

    if (authorities.contains(OPH_PAAKAYTTAJA_AUTHORITY)) {
      f
    } else {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }
  }

  def validate(f: => Iterable[String]): Unit = {
    val errors = f
    if (errors.nonEmpty) {
      throw ValidationException(errors.toList)
    }
  }

  def handleApiRequest[T](
    request: HttpServletRequest,
    auditOperation: AuditOperation,
    params: Map[String, Any],
    block: => Either[String, T]
  ): T = {
    block match {
      case Right(null) =>
        throw ResponseStatusException(HttpStatus.NOT_FOUND)
      case Right(result) =>
        auditLog.logWithParams(request, auditOperation, params)
        result
      case Left(errorMessage) =>
        // odottamattomista virheistä vain virheviesti
        throw ApiException(errorMessage)
    }
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(Array(classOf[ValidationException]))
  def validationException(ex: ValidationException): ValidationError = {
    ValidationError(
      status = HttpServletResponse.SC_BAD_REQUEST,
      message = "virhe.validointi",
      details = ex.validationErrors.asJava
    )
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Array(classOf[ApiException]))
  def validationException(ex: ApiException): String = {
    ObjectMapper().writeValueAsString(ex.errorMessage)
  }

  def handleExcelRequest(
    validationErrors: List[String],
    response: HttpServletResponse,
    request: HttpServletRequest,
    id: String,
    raporttiParams: Map[String, Any],
    auditOperation: AuditOperation,
    mapper: ObjectMapper,
    auditLog: AuditLog
  )(block: => Either[String, XSSFWorkbook]): Unit = {
    if (validationErrors.nonEmpty) {
      LOG.warn(s"Excel parameter validation failed: ${validationErrors.mkString(", ")}")
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
      response.setContentType("application/json")
      val errorJson = mapper.writeValueAsString(
        Map(
          "status"  -> HttpServletResponse.SC_BAD_REQUEST,
          "message" -> "virhe.validointi",
          "details" -> validationErrors.asJava
        )
      )
      response.getWriter.write(errorJson)
    } else {
      try {
        block match {
          case Right(wb) =>
            auditLog.logWithParams(request, auditOperation, raporttiParams)
            LOG.info(s"Sending Excel report: $id")
            val dateTimeStr = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val out         = response.getOutputStream
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
}

case class ValidationException(validationErrors: List[String]) extends RuntimeException

case class ApiException(errorMessage: String) extends RuntimeException
