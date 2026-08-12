package fi.oph.ovara.backend.yos

import fi.oph.ovara.backend.domain.{Fi, KKPaatettavaOpiskeluoikeusEntity, KKSitovastiVastaanottanut}
import fi.oph.ovara.backend.yos.YosConstants.{
  KOULUTUSASTE_ALEMPI_KORKEAKOULU_TUTKINTO,
  KOULUTUSASTE_AMK,
  KOULUTUSASTE_YAMK,
  KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO
}
import fi.oph.ovara.backend.yos.YosTestUtils.{OPISKELUOIKEUS, VASTAANOTTO}
import org.junit.jupiter.api.Assertions.{assertFalse, assertTrue}
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.{Test, TestInstance}

@Test
@TestInstance(Lifecycle.PER_CLASS)
class YosPredicateTest {

  @Test
  def opiskeluOikeusKuuluuYosinPiiriinKoulutusAsteenMukaan(): Unit = {
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(OPISKELUOIKEUS, VASTAANOTTO, List(OPISKELUOIKEUS))
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS,
        VASTAANOTTO.copy(koulutusasteet = List(KOULUTUSASTE_YAMK)),
        List(OPISKELUOIKEUS)
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_YAMK)),
        VASTAANOTTO.copy(koulutusasteet = List(KOULUTUSASTE_AMK, KOULUTUSASTE_YAMK)),
        List(OPISKELUOIKEUS)
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO)),
        VASTAANOTTO.copy(koulutusasteet = List(KOULUTUSASTE_AMK, KOULUTUSASTE_YAMK)),
        List(OPISKELUOIKEUS)
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_ALEMPI_KORKEAKOULU_TUTKINTO)),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
  }

  @Test
  def opiskeluOikeusEiKuuluYosinPiiriinKoulutusAsteenMukaan(): Unit = {
    assertFalse(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_YAMK)),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
    assertFalse(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO)),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
    assertFalse(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = None),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
    assertFalse(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS,
        VASTAANOTTO.copy(koulutusasteet = List.empty),
        List(OPISKELUOIKEUS)
      )
    )
  }

  @Test
  def opiskeluoikeusKuuluuYosinPiiriinLinkitetynOpiskeluOikeudenKautta(): Unit = {
    val linkitetty = OPISKELUOIKEUS.copy(
      koulutusaste = Some(KOULUTUSASTE_AMK),
      linkitettyOpiskeluoikeus = Some(OPISKELUOIKEUS.opiskeluoikeusAvain),
      opiskeluoikeusAvain = "alempi"
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_YAMK), linkitettyKoulutusAste = Some(KOULUTUSASTE_AMK)),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(
          koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO),
          linkitettyKoulutusAste = Some(KOULUTUSASTE_ALEMPI_KORKEAKOULU_TUTKINTO)
        ),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
  }

  @Test
  def opiskeluoikeusKuuluuYosinPiiriinLinkitetynOpiskeluOikeudenKauttaVainAlemmallaLinkki(): Unit = {
    val linkitetty = OPISKELUOIKEUS.copy(
      koulutusaste = Some(KOULUTUSASTE_AMK),
      linkitettyOpiskeluoikeus = Some(OPISKELUOIKEUS.opiskeluoikeusAvain),
      opiskeluoikeusAvain = "alempi"
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_YAMK)),
        VASTAANOTTO,
        List(OPISKELUOIKEUS, linkitetty)
      )
    )
  }

  @Test
  def opiskeluikeusKuuluuYosinPiiriinKoulutuskoodinKautta(): Unit = {
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772101")),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772100")),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772301")),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772300")),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772201")),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772200")),
        VASTAANOTTO,
        List(OPISKELUOIKEUS)
      )
    )
  }

}
