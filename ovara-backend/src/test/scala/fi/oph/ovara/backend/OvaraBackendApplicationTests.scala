package fi.oph.ovara.backend;

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, status}

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(Array("test"))
class OvaraBackendApplicationTests {
    @Autowired
    private val mvc: MockMvc = null

    @Test
    @throws[Exception]
    def get200ResponseFromHealthcheckUnautheticated(): Unit = {
        mvc.perform(MockMvcRequestBuilders.get("/api/healthcheck").accept(MediaType.APPLICATION_JSON))
          .andExpect(status.isOk)
          .andExpect(content.string(equalTo("Ovara application is running!")))
    }

    @Test
    @throws[Exception]
    def get401ResponseFromAuthenticatedApi(): Unit = {
        mvc.perform(MockMvcRequestBuilders.get("/api/alkamisvuodet").accept(MediaType.APPLICATION_JSON))
          .andExpect(status.isUnauthorized)
    }

    @Test
    @WithMockUser(username = "testuser", roles = Array("USER"))
    def getAuthenticatedUserGets200ResponseFromAuthenticatedApi(): Unit = {
        mvc.perform(MockMvcRequestBuilders.get("/api/alkamisvuodet"))
          .andExpect(status().isOk)
          .andExpect(content().string("[ \"2024\", \"2023\", \"2022\" ]"))
    }

}
