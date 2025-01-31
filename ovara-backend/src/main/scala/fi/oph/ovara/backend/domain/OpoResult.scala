package fi.oph.ovara.backend.domain

sealed trait OpoSisalto {
  val hakija: String
  val sahkoposti: Option[String]
  val puhelin: Option[String]
  val hakutoiveet: List[String]
  val oppijanumero: String
  val hakemusOid: String
  val paattoluokka: Option[String]
  val paasyJaSoveltuvuusKoe: Option[String]
  val lisanaytto: Option[String]
  val harkinnanvaraisuus: Option[String]
  val kokonaispisteet: Option[Int]
  val terveydellisiaSeikkoja: Option[String]
  val valintatieto: Option[String]
  val vastaanottotieto: Option[String]
  val varasija: Option[Int]
  val valinnanAlinPistemäärä: Option[Int]
}

case class OpoResult(hakija: String,
                     sahkoposti: Option[String],
                     puhelin: Option[String],
                     hakutoiveet: List[String],
                     oppijanumero: String,
                     hakemusOid: String,
                     paattoluokka: Option[String],
                     paasyJaSoveltuvuusKoe: Option[String],
                     lisanaytto: Option[String],
                     harkinnanvaraisuus: Option[String],
                     kokonaispisteet: Option[Int],
                     terveydellisiaSeikkoja: Option[String],
                     valintatieto: Option[String],
                     vastaanottotieto: Option[String],
                     varasija: Option[Int],
                     valinnanAlinPistemäärä: Option[Int],
                    ) extends OpoSisalto

case class OrganisaationOpoResult(
                                   organisaatio_oid: Option[String],
                                   opoResult: OpoResult
                                 )