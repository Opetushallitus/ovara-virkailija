package fi.oph.ovara.backend.domain

case class Organisaatio(organisaatio_oid: String, organisaatio_nimi: Kielistetty, organisaatiotyyppi: String)

case class OrganisaatioParentChild(parent_oid: String, child_oid: String)
