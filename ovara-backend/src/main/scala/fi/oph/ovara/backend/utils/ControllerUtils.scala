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
}
