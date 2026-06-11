package fi.oph.ovara.backend.domain

case class KkPaatettavaOpiskeluoikeus(
  oppijanumero: String,
  hetu: Option[String],
  syntymaaika: String,
  sukunimi: String,
  etunimet: String,
  kutsumanimi: String,
  opiskelijaAvain: String,
  opiskeluoikeusAvain: String,
  opiskeluoikeudenNimi: Kielistetty,
  opiskeluoikeudenPaattymispvm: Option[String],
  opiskeluoikeudenViimeisinTila: String,
  hakemusOid: String,
  hakuOid: String,
  hakuNimi: Kielistetty,
  hakukohdeOid: String,
  hakukohdeNimi: Kielistetty,
  oppilaitosOid: String,
  oppilaitosNimi: Kielistetty,
  vastaanottoAjankohta: String,
  koulutusluokitusKoodi: String,
  uudenOpiskeluoikeudenAlkamispvm: String
)
