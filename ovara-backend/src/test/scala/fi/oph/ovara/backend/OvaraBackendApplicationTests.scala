package fi.oph.ovara.backend;

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, status}

@SpringBootTest
@AutoConfigureMockMvc
class OvaraBackendApplicationTests {
    @Autowired
    private val mvc: MockMvc = null

    @Test
    @throws[Exception]
    def getResponseFromHealthcheck(): Unit = {
        mvc.perform(MockMvcRequestBuilders.get("/api/ping").accept(MediaType.APPLICATION_JSON))
          .andExpect(status.isOk)
          .andExpect(content.string(equalTo("Ovara application is running!")))
    }

}
