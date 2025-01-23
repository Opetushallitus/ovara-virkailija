package fi.oph.ovara.backend.domain

case class Organisaatio(organisaatio_oid: String, organisaatio_nimi: Kielistetty, organisaatiotyypit: List[String])

case class OrganisaatioParentChild(parent_oid: String, child_oid: String, organisaatio: Organisaatio)

case class OrganisaatioHierarkia(
    organisaatio_oid: String,
    organisaatio_nimi: Kielistetty,
    organisaatiotyypit: List[String],
    oppilaitostyyppi: Option[String] = None,
    tila: String,
    parent_oids: List[String],
    koulutustoimijaParent: Option[Organisaatio] = None,
    children: List[OrganisaatioHierarkia]
)

case class OrganisaatioHierarkiaWithHakukohteet(
    organisaatio_oid: String,
    organisaatio_nimi: Kielistetty,
    organisaatiotyypit: List[String],
    parent_oids: List[String],
    koulutustoimijaParent: Option[Organisaatio] = None,
    children: List[OrganisaatioHierarkiaWithHakukohteet] = List(),
    hakukohteet: List[OrganisaationKoulutusToteutusHakukohde]
)
