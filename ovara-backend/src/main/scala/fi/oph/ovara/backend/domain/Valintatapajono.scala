package fi.oph.ovara.backend.domain

case class Valintatapajono(
    valintatapajonoOid: String,
    valintatapajononNimi: String,
    valinnanTila: String,
    valinnanTilanKuvaus: Kielistetty
)
