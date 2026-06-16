package fi.oph.ovara.backend.domain

case class KkPaatettavaOpiskeluoikeus(
  sukunimi: String,
  etunimet: String,
  kutsumanimi: String,
  hetu: Option[String],
  syntymaAika: String,
  oppijanumero: String,
  opiskeluoikeudenNimi: Kielistetty,
  opiskeluoikeudenPaattymispvm: Option[String],
  opiskeluoikeudenViimeisinTila: String,
  opiskelijaAvain: String,
  opiskeluoikeusAvain: String,
  hakemusOid: String,
  hakukohdeNimi: Kielistetty,
  hakukohdeOid: String,
  oppilaitosNimi: Kielistetty,
  oppilaitosOid: String,
  uudenOpiskeluoikeudenAlkamispvm: String,
  vastaanottoAjankohta: String,
  hakuNimi: Kielistetty,
  hakuOid: String,
  koulutusluokitusKoodit: String
)
