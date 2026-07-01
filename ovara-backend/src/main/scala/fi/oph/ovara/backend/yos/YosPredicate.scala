package fi.oph.ovara.backend.yos

import fi.oph.ovara.backend.domain.{
  KKPaatettavaOpiskeluoikeusEntity,
  KKSitovastiVastaanottanut,
  KkPaatettavaOpiskeluoikeus
}
import fi.oph.ovara.backend.yos.YosConstants.{
  KOULUTUSASTE_ALEMMAT,
  KOULUTUSASTE_YLEMMAT,
  LAAKETIETEEN_LISENSIAATIT_KOULUTUSKOODIT
}
import fi.oph.ovara.backend.yos.YosKoulutusAsteLuokka.{
  ALEMMAT_ASTEET,
  EI_YOS_KOULUTUSASTETTA,
  YLEMMAT_ASTEET,
  YLEMMAT_JA_ALEMMAT_ASTEET
}

object YosPredicate {
  def onkoOikeusKoulutusAsteenMukaanYosinPiirissa(
    oikeus: KKPaatettavaOpiskeluoikeusEntity,
    vastaanotto: KKSitovastiVastaanottanut
  ): Boolean = {
    val oikeudenAste       = getKoulutusAsteOpiskeluOikeudelle(oikeus)
    val vastaanottanutAste = getKoulutusAsteHakutoiveelle(vastaanotto)
    kuuluukoOpiskeluOikeusYosinPiiriinKoulutusAsteenMukaan(vastaanottanutAste, oikeudenAste)
  }

  private def kuuluukoOpiskeluOikeusYosinPiiriinKoulutusAsteenMukaan(
    vastaanotettavanAste: YosKoulutusAsteLuokka,
    oikeudenAste: YosKoulutusAsteLuokka
  ): Boolean = {
    (vastaanotettavanAste, oikeudenAste) match
      case (YLEMMAT_JA_ALEMMAT_ASTEET, YLEMMAT_ASTEET) =>
        true
      case (YLEMMAT_JA_ALEMMAT_ASTEET, ALEMMAT_ASTEET) =>
        true
      case (ALEMMAT_ASTEET, ALEMMAT_ASTEET) =>
        true
      case _ =>
        false
  }

  private def getKoulutusAsteHakutoiveelle(hakutoive: KKSitovastiVastaanottanut): YosKoulutusAsteLuokka = {
    val containsAlempi: Boolean = hakutoive.koulutusaste.exists(k => KOULUTUSASTE_ALEMMAT.contains(k))
    val containsYlempi: Boolean = hakutoive.koulutusaste.exists(k => KOULUTUSASTE_YLEMMAT.contains(k))
    (containsAlempi, containsYlempi) match {
      case (_, true) =>
        YLEMMAT_JA_ALEMMAT_ASTEET
      case (true, false) =>
        ALEMMAT_ASTEET
      case _ =>
        EI_YOS_KOULUTUSASTETTA
    }
  }

  private def getKoulutusAsteOpiskeluOikeudelle(oikeus: KKPaatettavaOpiskeluoikeusEntity): YosKoulutusAsteLuokka = {
    if (LAAKETIETEEN_LISENSIAATIT_KOULUTUSKOODIT.contains(oikeus.koulutusKoodi.getOrElse(""))) {
      ALEMMAT_ASTEET
    } else {
      val containsAlempi: Boolean = oikeus.koulutusaste.exists(k => KOULUTUSASTE_ALEMMAT.contains(k))
      val containsYlempi: Boolean = oikeus.koulutusaste.exists(k => KOULUTUSASTE_YLEMMAT.contains(k))

      (containsAlempi, containsYlempi) match {
        case (true, _) =>
          ALEMMAT_ASTEET
        case (false, true) =>
          YLEMMAT_ASTEET
        case _ =>
          EI_YOS_KOULUTUSASTETTA
      }
    }
  }
}
