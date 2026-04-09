package fi.oph.ovara.backend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import fi.oph.ovara.backend.raportointi.ErrorResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity

import java.util
import scala.jdk.CollectionConverters.*

object ControllerUtils {

  def getListParamAsScalaList(listParam: util.Collection[String]): List[String] = {
    if (listParam == null) List() else listParam.asScala.toList
  }

  def handleRequest[T](
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
        case Right(null) =>
          ResponseEntity.notFound().build()
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
}
