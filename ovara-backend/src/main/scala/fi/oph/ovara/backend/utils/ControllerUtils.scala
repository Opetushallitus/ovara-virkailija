package fi.oph.ovara.backend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import fi.oph.ovara.backend.opiskelijavalintatieto.ValidationError
import fi.oph.ovara.backend.service.UserService
import fi.oph.ovara.backend.utils.Constants.OPH_PAAKAYTTAJA_AUTHORITY
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.{ExceptionHandler, ResponseStatus}
import org.springframework.web.server.ResponseStatusException

import java.util
import scala.jdk.CollectionConverters.*

trait ControllerUtils {
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

}

case class ValidationException(validationErrors: List[String]) extends RuntimeException

case class ApiException(errorMessage: String) extends RuntimeException
