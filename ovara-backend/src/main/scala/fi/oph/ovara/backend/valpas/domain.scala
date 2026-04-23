package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.domain.{En, Fi, Kielistetty, Sv}

import java.time.{LocalDate, OffsetDateTime}

case class Hakemus(
    hakemusOid: String,
    hakemuksenMuokkauksenAikaleima: Option[OffsetDateTime],
    email: String,
    matkapuhelin: String,
    lahiosoite: String,
    postinumero: String,
    postitoimipaikka: String,
    maa: KoodistoArvo,
    hakuOid: String,
    hakuNimi: Kielistetty,
    hakutapa: KoodistoArvo,
    hakutyyppi: KoodistoArvo,
    aktiivinenHaku: Option[Boolean],
    haunAlkamispaivamaara: Option[LocalDate],
    oppijaOid: String,
    huoltajanNimi: Option[String],
    huoltajanPuhelinnumero: Option[String],
    huoltajanSahkoposti: Option[String],
    hakutoiveet: Seq[Hakutoive]
)

case class Hakutoive(
                      hakukohdeOid: String,
                      hakukohdeNimi: Kielistetty,
                      hakutoivenumero: Int,
                      hakukohdeOrganisaatio: String,
                      organisaatioNimi: Kielistetty,
                      koulutusOid: String,
                      koulutusNimi: Kielistetty,
                      hakukohdeKoulutuskoodi: Seq[KoodistoArvo], // Koutan toteutus.koulutuksetKoodiUri? koulutus.koulutuksetKoodiUri?
                      vastaanottotieto: String,
                      valintatila: String,
                      ilmoittautumistila: String,
                      harkinnanvaraisuus: String,
    paasykoe: Option[Paasykoe],       // ???
    kielikoe: Option[Paasykoe],       // ???
    lisanaytto: Option[Paasykoe],     // ???
    liitteetTarkastettu: Boolean,     // ???
    valintakoe: Seq[Valintakoe],      // Kaikki hakukohteen valintakokeet?
    alinHyvaksyttyPistemaara: String, // ???
    alinValintaPistemaara: Int,       // ???
    pisteet: Int,                     // ???
    varasijanumero: Int,              // ???
)

case class Paasykoe(
    tunniste: String,
    arvo: String,
    osallistuminen: String
)
case class Valintakoe(
    osallistuminen: String,
    laskentatila: String,
    valintakoeOid: String,
    valintakoeTunniste: String,
    nimi: String,
    valinnanVaiheOid: String,
    valinnanVaiheJarjestysluku: Int,
    arvo: String
)

case class KoodistoArvo(
    versioituUri: String,
    koodiarvo: String,
    koodistoUri: String,
    koodistoVersio: Int,
    nimi: Kielistetty,
    lyhytNimi: Kielistetty
)
