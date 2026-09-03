package fi.oph.ovara.backend.yos

import fi.oph.ovara.backend.domain.{
  Fi,
  KKPaatettavaOpiskeluoikeusEntity,
  KKSitovastiVastaanottanut,
  KoulutusKoodi,
  YosHenkilo,
  YosValintarekisteriTiedot
}

import java.time.LocalDate

object YosTestUtils {

  def ORG_OID               = "1.2.246.562.10.91392558028"
  def OPPIJA_OID            = "1.2.246.562.24.55412038042"
  def HAKUKOHDE_OID         = "1.2.246.562.20.00000000000000039609"
  def HAKEMUS_OID           = "1.2.246.562.11.00000000000002021955"
  def OPISKELU_OIKEUS_AVAIN = "virran-oikeus-avain"
  def MYONTAJA              = "10122"

  def OPISKELUOIKEUS: KKPaatettavaOpiskeluoikeusEntity = KKPaatettavaOpiskeluoikeusEntity(
    opiskelijaAvain = OPPIJA_OID,
    opiskeluoikeusAvain = OPISKELU_OIKEUS_AVAIN,
    opiskeluoikeudenNimi = Map(Fi -> "Sateentekijän tutkinto"),
    opiskeluoikeudenViimeisinTila = "2",
    koulutusaste = Some(YosConstants.KOULUTUSASTE_AMK),
    koulutusKoodi = Some("14"),
    linkitettyKoulutusAste = None,
    linkitetynKoulutuksenTila = None,
    myontaja = MYONTAJA
  )

  def VASTAANOTTO: KKSitovastiVastaanottanut = KKSitovastiVastaanottanut(
    oppijanumero = OPPIJA_OID,
    hakemusOid = HAKEMUS_OID,
    hakukohdeOid = HAKUKOHDE_OID,
    hakukohdeNimi = Map(Fi -> "Sadetanssijan korkeakoulutus"),
    vastaanottoAjankohta = Some(LocalDate.now()),
    hakuOid = "1.2.246.562.29.00000000000000036122",
    koulutusasteet = List(YosConstants.KOULUTUSASTE_AMK),
    haunNimi = Map(Fi -> "Korkeakoulujen kevään toinen yhteishaku 2026"),
    oppilaitosOid = ORG_OID,
    oppilaitosNimi = Map(Fi -> "Ratamon korkeakoulu"),
    koulutusKoodit = List(KoulutusKoodi(koodiArvo = "31010", koodiUri = "koulutus_31010#1"))
  )

  def HENKILO: YosHenkilo = YosHenkilo(
    sukunimi = "Nukettaja",
    etunimet = "Ruhtinas",
    kutsumanimi = "Ruhtinas",
    hetu = None,
    syntymaAika = Some(LocalDate.of(2000, 1, 1)),
    oppijanumero = OPPIJA_OID
  )

  def YOS_VALINTAREKISTERI: YosValintarekisteriTiedot = YosValintarekisteriTiedot(
    henkiloOid = OPPIJA_OID,
    hakukohdeOid = HAKUKOHDE_OID,
    hakemusOid = HAKEMUS_OID,
    paateltyAloitusPvm = Some(LocalDate.of(2026, 7, 15)),
    naytettyPaatettavaOikeus = s"${MYONTAJA}_$OPISKELU_OIKEUS_AVAIN"
  )
}
