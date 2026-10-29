package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.{Test, TestInstance}
import org.mockito.Mockito

@Test
@TestInstance(Lifecycle.PER_CLASS)
class ValpasRepositoryTest {

  private val db = Mockito.mock(classOf[ReadOnlyDatabase])

  private val repository = ValpasRepository(db)

  @Test
  def eiTeeKyselyaTyhjallaListalla(): Unit = {
    assertTrue(repository.selectHakemukset(List.empty).isEmpty)
    assertTrue(repository.selectHakutoiveet(Set.empty).isEmpty)
    assertTrue(repository.selectKoodistot(Set.empty).isEmpty)
    Mockito.verifyNoInteractions(db)
  }
}
