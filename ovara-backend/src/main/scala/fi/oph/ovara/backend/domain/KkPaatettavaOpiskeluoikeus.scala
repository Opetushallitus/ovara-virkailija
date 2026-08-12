package fi.oph.ovara.backend.domain

import java.time.LocalDate

case class KkPaatettavaOpiskeluoikeus(
  sukunimi: String,
  etunimet: String,
  kutsumanimi: String,
  hetu: Option[String],
  syntymaAika: LocalDate,
  oppijanumero: String,
  opiskeluoikeudenNimi: Kielistetty,
  opiskeluoikeudenPaattymispvm: Option[LocalDate],
  opiskeluoikeudenViimeisinTila: String,
  opiskelijaAvain: String,
  opiskeluoikeusAvain: String,
  naytettyHakijalle: Boolean,
  hakemusOid: String,
  hakukohdeNimi: Kielistetty,
  hakukohdeOid: String,
  oppilaitosNimi: Kielistetty,
  oppilaitosOid: String,
  uudenOpiskeluoikeudenAlkamispvm: Option[LocalDate],
  vastaanottoAjankohta: LocalDate,
  hakuNimi: Kielistetty,
  hakuOid: String,
  koulutusluokitusKoodit: List[String]
)

case class KKPaatettavaOpiskeluoikeusEntity(
  opiskelijaAvain: String,
  opiskeluoikeusAvain: String,
  opiskeluoikeudenNimi: Kielistetty,
  opiskeluoikeudenViimeisinTila: String,
  koulutusaste: Option[String],
  koulutusKoodi: Option[String],
  linkitettyKoulutusAste: Option[String],
  linkitettyOpiskeluoikeus: Option[String]
)

case class KKSitovastiVastaanottanut(
  oppijanumero: String,
  hakemusOid: String,
  hakukohdeOid: String,
  hakukohdeNimi: Kielistetty,
  vastaanottoAjankohta: Option[LocalDate],
  hakuOid: String,
  koulutusasteet: List[String],
  haunNimi: Kielistetty,
  oppilaitosOid: String,
  oppilaitosNimi: Kielistetty,
  koulutusKoodiArvot: List[String]
)

case class YosHenkilo(
  sukunimi: String,
  etunimet: String,
  kutsumanimi: String,
  hetu: Option[String],
  syntymaAika: Option[LocalDate],
  oppijanumero: String
)

case class YosValintarekisteriTiedot(
  henkiloOid: String,
  hakukohdeOid: String,
  hakemusOid: String,
  paateltyAloitusPvm: Option[LocalDate],
  naytettyPaatettavaOikeus: String
)
