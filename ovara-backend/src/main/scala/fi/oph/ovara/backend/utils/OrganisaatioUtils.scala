package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*

object OrganisaatioUtils {
  def mapOrganisaationHakukohteetToParent(
      hierarkia: OrganisaatioHierarkia,
      organisaatiotWithHakukohteet: Map[Option[String], Vector[OrganisaationKoulutusToteutusHakukohde]]
  ): OrganisaatioHierarkiaWithHakukohteet = {
    val organisaatioOid = hierarkia.organisaatio_oid
    val kths = organisaatiotWithHakukohteet.find(o => {
      o._1.getOrElse("") == organisaatioOid
    }) match {
      case Some((o, kths)) => kths.toList
      case None            => List()
    }

    val children = hierarkia.children
    val childHierarkiat = if (children.isEmpty) {
      List()
    } else {
      children.map(child => mapOrganisaationHakukohteetToParent(child, organisaatiotWithHakukohteet))
    }

    OrganisaatioHierarkiaWithHakukohteet(
      hierarkia.organisaatio_oid,
      hierarkia.organisaatio_nimi,
      hierarkia.organisaatiotyypit,
      hierarkia.parent_oids,
      childHierarkiat,
      kths
    )
  }

  def mapOrganisaationHakukohteetToParents(
      hierarkiat: List[OrganisaatioHierarkia],
      organisaatiotWithHakukohteet: Map[Option[String], Vector[OrganisaationKoulutusToteutusHakukohde]]
  ): List[OrganisaatioHierarkiaWithHakukohteet] = {
    hierarkiat.map(hierarkia => mapOrganisaationHakukohteetToParent(hierarkia, organisaatiotWithHakukohteet))
  }

  def getDescendantOids(hierarkia: OrganisaatioHierarkia): List[String] = {
    val organisaatioOid = hierarkia.organisaatio_oid
    val children        = hierarkia.children
    if (children.isEmpty) {
      List(organisaatioOid)
    } else {
      List(organisaatioOid) ::: children.flatMap(child => getDescendantOids(child))
    }
  }

  def getKayttooikeusDescendantAndSelfOids(hierarkia: OrganisaatioHierarkia, organisaatioOids: List[String]): List[String] = {
    val children   = hierarkia.children
    val parentOids = hierarkia.parent_oids

    if (parentOids.exists(parentOid => organisaatioOids.contains(parentOid))) {
      List(hierarkia.organisaatio_oid) ::: children.flatMap(child =>
        getKayttooikeusDescendantAndSelfOids(child, organisaatioOids)
      )
    } else {
      children.flatMap(child => getKayttooikeusDescendantAndSelfOids(child, organisaatioOids))
    }
  }
  
  def filterExistingOrgs(hierarkia: OrganisaatioHierarkia): Option[OrganisaatioHierarkia] = {
    val children = hierarkia.children

    if (hierarkia.tila == "POISTETTU") {
      None
    } else {
      val filteredChildHierarkiat = children.flatMap(child => filterExistingOrgs(child))
      Some(hierarkia.copy(children = filteredChildHierarkiat))
    }
  }
 }
