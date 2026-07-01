package fi.oph.ovara.backend.yos

object YosConstants {
  def KOULUTUSASTE_ALEMPI_KORKEAKOULU_TUTKINTO = "63"

  def KOULUTUSASTE_AMK = "62"

  def KOULUTUSASTE_YAMK = "71"

  def KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO = "72"

  def KOULUTUSASTE_ALEMMAT: Seq[String] = Seq(KOULUTUSASTE_ALEMPI_KORKEAKOULU_TUTKINTO, KOULUTUSASTE_AMK)

  def KOULUTUSASTE_YLEMMAT: Seq[String] = Seq(KOULUTUSASTE_YAMK, KOULUTUSASTE_YLEMPI_KORKEAKOULU_TUTKINTO)

  def KOULUTUSASTE_YLEMMAT_JA_ALEMMAT: Seq[String] = KOULUTUSASTE_ALEMMAT ++ KOULUTUSASTE_YLEMMAT

  /* Seuraavat koulutuskoodit on luetultu tähän ja niitä käytetään tutkinnonaste vertailussa (tulkitaan ylempi+alempi):
       - Lääketieteen lisensiaatti (772101, 772100)
       - Eläinlääketieteen lisensiaatti (772301, 772300)
       - Hammaslääketieteen lisensiaatti (772201, 772200)
     */
  def LAAKETIETEEN_LISENSIAATIT_KOULUTUSKOODIT: Seq[String] = Seq("772101", "772100", "772301", "772300", "772201", "772200")
}
