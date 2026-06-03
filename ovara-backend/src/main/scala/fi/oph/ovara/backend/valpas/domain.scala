package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.domain.Kielistetty

import java.time.{LocalDateTime, OffsetDateTime}

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
  aktiivinenHaku: Option[Boolean],
  haunAlkamispaivamaara: Option[LocalDateTime],
  oppijaOid: String,
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
  hakukohdeKoulutuskoodi: Seq[KoodistoArvo],
  vastaanottotieto: String,
  valintatila: String,
  ilmoittautumistila: String,
  harkinnanvaraisuus: String,
  alinHyvaksyttyPistemaara: Option[BigDecimal],
  pisteet: Option[BigDecimal],
  varasijanumero: Option[Int]
)

case class ValpasHakuaika(
  alkaa: Option[LocalDateTime],
  paattyy: Option[LocalDateTime]
)

case class KoodistoArvo(
  versioituUri: String,
  koodiarvo: String,
  koodistoUri: String,
  koodistoVersio: Int,
  nimi: Kielistetty
)
