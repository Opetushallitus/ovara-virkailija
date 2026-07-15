package fi.oph.ovara.backend.yos

import fi.oph.ovara.backend.domain.{Fi, KKPaatettavaOpiskeluoikeusEntity, KKSitovastiVastaanottanut, YosHenkilo}

import java.time.LocalDate

object YosTestUtils {

  def ORG_OID = "1.2.246.562.10.91392558028"

  def OPISKELUOIKEUS: KKPaatettavaOpiskeluoikeusEntity = KKPaatettavaOpiskeluoikeusEntity(
    opiskelijaAvain = "1.2.246.562.24.55412038042",
    opiskeluoikeusAvain = "virran-oikeus-avain",
    opiskeluoikeudenNimi = Map(Fi -> "Sateentekijän tutkinto"),
    opiskeluoikeudenViimeisinTila = "2",
    koulutusaste = Some(YosConstants.KOULUTUSASTE_AMK),
    koulutusKoodi = Some("14"),
    linkitettyKoulutusAste = None
  )

  def VASTAANOTTO: KKSitovastiVastaanottanut = KKSitovastiVastaanottanut(
    oppijanumero = "1.2.246.562.24.55412038042",
    hakemusOid = "1.2.246.562.11.00000000000002021955",
    hakukohdeOid = "1.2.246.562.20.00000000000000039609",
    hakukohdeNimi = Map(Fi -> "Sadetanssijan korkeakoulutus"),
    vastaanottoAjankohta = Some(LocalDate.now()),
    hakuOid = "1.2.246.562.29.00000000000000036122",
    koulutusasteet = List(YosConstants.KOULUTUSASTE_AMK),
    haunNimi = Map(Fi -> "Korkeakoulujen kevään toinen yhteishaku 2026"),
    oppilaitosOid = ORG_OID,
    oppilaitosNimi = Map(Fi -> "Ratamon korkeakoulu"),
    koulutusKoodiArvot = Some("koulutus_31010")
  )

  def HENKILO: YosHenkilo = YosHenkilo(
    sukunimi = "Nukettaja",
    etunimet = "Ruhtinas",
    kutsumanimi = "Ruhtinas",
    hetu = None,
    syntymaAika = Some(LocalDate.of(2000, 1, 1)),
    oppijanumero = "1.2.246.562.24.55412038042"
  )
}
