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
  hakemusOid: String,
  hakukohdeNimi: Kielistetty,
  hakukohdeOid: String,
  oppilaitosNimi: Kielistetty,
  oppilaitosOid: String,
  uudenOpiskeluoikeudenAlkamispvm: LocalDate,
  vastaanottoAjankohta: LocalDate,
  hakuNimi: Kielistetty,
  hakuOid: String,
  koulutusluokitusKoodit: String
)

case class KKPaatettavaOpiskeluoikeusEntity(
  opiskelijaAvain: String,
  opiskeluoikeusAvain: String,
  opiskeluoikeudenNimi: Kielistetty,
  opiskeluoikeudenViimeisinTila: String,
  koulutusaste: Option[String],
)
