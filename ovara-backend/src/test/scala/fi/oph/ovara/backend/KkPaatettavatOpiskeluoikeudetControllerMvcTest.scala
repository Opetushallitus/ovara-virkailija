package fi.oph.ovara.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(Array("test"))
class KkPaatettavatOpiskeluoikeudetControllerMvcTest {
  @Autowired
  private val mvc: MockMvc = null

  @Test
  @WithMockUser(username = "testuser", roles = Array("USER"))
  def test403ResponseWithoutProperOvaraRoleJson(): Unit = {
    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/kk-paatettavat-opiskeluoikeudet/json")
          .param("ovara_oppilaitos", "1.2.246.562.10.278170642010")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isForbidden)
  }

  @Test
  @WithMockUser(username = "testuser", roles = Array("APP_OVARA-VIRKAILIJA_KK_YOS"))
  def test400ResponseWithInvalidInputJson(): Unit = {
    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/kk-paatettavat-opiskeluoikeudet/json")
          .param("ovara_oppilaitos", "foo")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest)
  }

  @Test
  @WithMockUser(username = "testuser", roles = Array("APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA_1.2.246.562.10.00000000001"))
  def testdoesNotReturn403ResponseForOphPaakayttajaJson(): Unit = {
    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/kk-paatettavat-opiskeluoikeudet/json")
          .param("ovara_oppilaitos", "foo")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest)
  }

  @Test
  @WithMockUser(username = "testuser", roles = Array("USER"))
  def test403ResponseWithoutProperOvaraRoleExcel(): Unit = {
    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/kk-paatettavat-opiskeluoikeudet/excel")
          .param("ovara_oppilaitos", "1.2.246.562.10.278170642010")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isForbidden)
  }

  @Test
  @WithMockUser(username = "testuser", roles = Array("APP_OVARA-VIRKAILIJA_KK_YOS"))
  def test400ResponseWithInvalidInputExcel(): Unit = {
    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/kk-paatettavat-opiskeluoikeudet/excel")
          .param("ovara_oppilaitos", "foo")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest)
  }

  @Test
  @WithMockUser(username = "testuser", roles = Array("APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA_1.2.246.562.10.00000000001"))
  def testdoesNotReturn403ResponseForOphPaakayttajaExcel(): Unit = {
    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/kk-paatettavat-opiskeluoikeudet/excel")
          .param("ovara_oppilaitos", "foo")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest)
  }
}
