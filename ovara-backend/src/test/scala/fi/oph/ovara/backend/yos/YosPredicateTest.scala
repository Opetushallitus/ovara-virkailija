package fi.oph.ovara.backend.yos

import fi.oph.ovara.backend.domain.{Fi, KKPaatettavaOpiskeluoikeusEntity, KKSitovastiVastaanottanut}
import fi.oph.ovara.backend.yos.YosConstants.{
  KOULUTUSASTE_ALEMPI_KORKEAKOULU_TUTKINTO,
  KOULUTUSASTE_AMK,
  KOULUTUSASTE_YAMK,
  KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO
}
import org.junit.jupiter.api.Assertions.{assertFalse, assertTrue}
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.{Test, TestInstance}

@Test
@TestInstance(Lifecycle.PER_CLASS)
class YosPredicateTest {

  def OPISKELUOIKEUS: KKPaatettavaOpiskeluoikeusEntity = KKPaatettavaOpiskeluoikeusEntity(
    opiskelijaAvain = "",
    opiskeluoikeusAvain = "",
    opiskeluoikeudenNimi = Map(Fi -> "Sateentekijän tutkinto"),
    opiskeluoikeudenViimeisinTila = "2",
    koulutusaste = Some(YosConstants.KOULUTUSASTE_AMK),
    koulutusKoodi = Some("14"),
    linkitettyKoulutusAste = None
  )

  def VASTAANOTTO: KKSitovastiVastaanottanut = KKSitovastiVastaanottanut(
    oppijanumero = "opiskelija-avain",
    hakemusOid = "",
    hakukohdeOid = "",
    hakukohdeNimi = Map(Fi -> "Sadetanssijan korkeakoulutus"),
    vastaanottoAjankohta = None,
    hakuOid = "",
    koulutusasteet = List(YosConstants.KOULUTUSASTE_AMK),
    haunNimi = Map(Fi -> "Korkeakoulujen kevään toinen yhteishaku 2026"),
    oppilaitosOid = "",
    oppilaitosNimi = Map(Fi -> "Ratamon korkeakoulu")
  )

  @Test
  def opiskeluOikeusKuuluuYosinPiiriinKoulutusAsteenMukaan(): Unit = {
    assertTrue(YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(OPISKELUOIKEUS, VASTAANOTTO))
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS,
        VASTAANOTTO.copy(koulutusasteet = List(KOULUTUSASTE_YAMK))
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_YAMK)),
        VASTAANOTTO.copy(koulutusasteet = List(KOULUTUSASTE_AMK, KOULUTUSASTE_YAMK))
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO)),
        VASTAANOTTO.copy(koulutusasteet = List(KOULUTUSASTE_AMK, KOULUTUSASTE_YAMK))
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_ALEMPI_KORKEAKOULU_TUTKINTO)),
        VASTAANOTTO
      )
    )
  }

  @Test
  def opiskeluOikeusEiKuuluYosinPiiriinKoulutusAsteenMukaan(): Unit = {
    assertFalse(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_YAMK)),
        VASTAANOTTO
      )
    )
    assertFalse(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO)),
        VASTAANOTTO
      )
    )
    assertFalse(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(OPISKELUOIKEUS.copy(koulutusaste = None), VASTAANOTTO)
    )
    assertFalse(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS,
        VASTAANOTTO.copy(koulutusasteet = List.empty)
      )
    )
  }

  @Test
  def opiskeluoikeusKuuluuYosinPiiriinLinkitetynOpiskeluOikeudenKautta(): Unit = {
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(koulutusaste = Some(KOULUTUSASTE_YAMK), linkitettyKoulutusAste = Some(KOULUTUSASTE_AMK)),
        VASTAANOTTO
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS.copy(
          koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO),
          linkitettyKoulutusAste = Some(KOULUTUSASTE_ALEMPI_KORKEAKOULU_TUTKINTO)
        ),
        VASTAANOTTO
      )
    )
  }

  @Test
  def opiskeluikeusKuuluuYosinPiiriinKoulutuskoodinKautta(): Unit = {
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772101")),
        VASTAANOTTO
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772100")),
        VASTAANOTTO
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772301")),
        VASTAANOTTO
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772300")),
        VASTAANOTTO
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772201")),
        VASTAANOTTO
      )
    )
    assertTrue(
      YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
        OPISKELUOIKEUS
          .copy(koulutusaste = Some(KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO), koulutusKoodi = Some("772200")),
        VASTAANOTTO
      )
    )
  }

}
