package fi.oph.ovara.backend.raportointi

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import scala.jdk.CollectionConverters.*

// Speksin palvelinosoite tulee konfiguraatiosta eikä pyynnöstä, joka kuormantasaajan takana on http.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(Array("test"))
class SwaggerConfigTest {

  @Autowired
  private val mvc: MockMvc = null

  @Value("${ovara.backend.url}")
  private val ovaraBackendUrl: String = null

  @Test
  def apiDocsServerIsConfiguredBackendUrlNotRequestUrl(): Unit = {
    val body = mvc
      .perform(MockMvcRequestBuilders.get("/v3/api-docs/ovara-apis"))
      .andExpect(status.isOk)
      .andReturn()
      .getResponse
      .getContentAsString
    val servers = new ObjectMapper().readTree(body).path("servers").elements().asScala.map(_.path("url").asText).toList

    assertEquals(List(ovaraBackendUrl), servers)
  }
}
