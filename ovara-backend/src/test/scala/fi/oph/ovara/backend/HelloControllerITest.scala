package fi.oph.ovara.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.ResponseEntity
import org.assertj.core.api.Assertions.assertThat


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) class HelloControllerITest {
  @Autowired private val template: TestRestTemplate = null

  @Test
  @throws[Exception]
  def getHello(): Unit = {
    val response: ResponseEntity[String] = template.getForEntity("/", classOf[String])
    assertThat(response.getBody).isEqualTo("Greetings from Spring Boot!")
  }
}