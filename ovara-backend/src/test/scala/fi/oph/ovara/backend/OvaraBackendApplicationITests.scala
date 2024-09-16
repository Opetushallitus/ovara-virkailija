package fi.oph.ovara.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.ResponseEntity
import org.assertj.core.api.Assertions.assertThat


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) class OvaraBackendApplicationITests {
  @Autowired private val template: TestRestTemplate = null

  @Test
  @throws[Exception]
  def getResponseFromHealthcheck(): Unit = {
    val response: ResponseEntity[String] = template.getForEntity("/api/ping", classOf[String])
    assertThat(response.getBody).isEqualTo("Ovara application is running!")
  }
}
