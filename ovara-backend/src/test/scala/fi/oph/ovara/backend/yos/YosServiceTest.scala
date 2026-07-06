package fi.oph.ovara.backend.yos

import fi.oph.ovara.backend.raportointi.dto.KkPaatettavatOpiskeluoikeudetParams
import fi.oph.ovara.backend.repository.{KkPaatettavatOpiskeluoikeudetRepository, ReadOnlyDatabase}
import fi.oph.ovara.backend.yos.YosTestUtils.{HENKILO, OPISKELUOIKEUS, ORG_OID, VASTAANOTTO}
import org.junit.jupiter.api.Assertions.{assertEquals, assertNull, assertTrue}
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.{Test, TestInstance}
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentMatchers, Mockito}
import org.mockito.Mockito.when

@Test
@TestInstance(Lifecycle.PER_CLASS)
class YosServiceTest {

  private val db = Mockito.mock(classOf[ReadOnlyDatabase])

  private val repository = KkPaatettavatOpiskeluoikeudetRepository()

  private val service: YosService = YosService(repository, db)

  private val params = KkPaatettavatOpiskeluoikeudetParams(oppilaitos = ORG_OID)

  private val orgs = List(ORG_OID)

  @Test
  def eiPalautaMitaanJosPaatettaviaOikeuksiaEiLoydy(): Unit = {
    when(db.run(any(), any())).thenReturn(List.empty)
    assertTrue(service.getPaattyvatOpiskeluOikeudet(orgs, params).isEmpty)
  }

  @Test
  def eiPalautaMitaanJosSitovastiVastaanottaneitaEiLoydy(): Unit = {
    when(db.run(any(), ArgumentMatchers.eq("opiskeluoikeudetQuery"))).thenReturn(List(OPISKELUOIKEUS))
    when(db.run(any(), ArgumentMatchers.eq("sitovastiVastaanottaneetQuery"))).thenReturn(List.empty)
    assertTrue(service.getPaattyvatOpiskeluOikeudet(orgs, params).isEmpty)
  }

  @Test
  def palauttaaPaattyvatOpiskeluoikeudet(): Unit = {
    when(db.run(any(), ArgumentMatchers.eq("opiskeluoikeudetQuery"))).thenReturn(List(OPISKELUOIKEUS))
    when(db.run(any(), ArgumentMatchers.eq("sitovastiVastaanottaneetQuery"))).thenReturn(List(VASTAANOTTO))
    when(db.run(any(), ArgumentMatchers.eq("yosHenkilotQuery"))).thenReturn(List(HENKILO))
    val oikeudet = service.getPaattyvatOpiskeluOikeudet(orgs, params)
    assertEquals(1, oikeudet.size)
    val oikeus = oikeudet.head
    assertEquals("Ruhtinas", oikeus.etunimet)
    assertEquals("Nukettaja", oikeus.sukunimi)
    assertEquals("Ruhtinas", oikeus.kutsumanimi)
    assertEquals(None, oikeus.hetu)
    assertEquals(HENKILO.syntymaAika.get, oikeus.syntymaAika)
    assertEquals(VASTAANOTTO.haunNimi, oikeus.hakuNimi)
    assertEquals(VASTAANOTTO.hakemusOid, oikeus.hakemusOid)
    assertEquals(VASTAANOTTO.vastaanottoAjankohta.get, oikeus.vastaanottoAjankohta)
    assertEquals(VASTAANOTTO.oppijanumero, oikeus.oppijanumero)
    assertEquals(VASTAANOTTO.hakukohdeOid, oikeus.hakukohdeOid)
    assertEquals(VASTAANOTTO.hakuOid, oikeus.hakuOid)
    assertEquals(VASTAANOTTO.hakukohdeNimi, oikeus.hakukohdeNimi)
    assertEquals(VASTAANOTTO.oppilaitosOid, oikeus.oppilaitosOid)
    assertEquals(VASTAANOTTO.oppilaitosNimi, oikeus.oppilaitosNimi)
    assertEquals(OPISKELUOIKEUS.opiskeluoikeudenNimi, oikeus.opiskeluoikeudenNimi)
    assertEquals(OPISKELUOIKEUS.opiskeluoikeudenViimeisinTila, oikeus.opiskeluoikeudenViimeisinTila)
    assertEquals(OPISKELUOIKEUS.opiskeluoikeusAvain, oikeus.opiskeluoikeusAvain)
  }

}
