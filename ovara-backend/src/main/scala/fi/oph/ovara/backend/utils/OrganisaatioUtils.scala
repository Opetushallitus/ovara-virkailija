package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.service.CommonService

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
      hierarkia.koulutustoimijaParent,
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

  def getKayttooikeusDescendantAndSelfOids(
      hierarkia: OrganisaatioHierarkia,
      organisaatioOids: List[String]
  ): List[String] = {
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

  def filterActiveOrgsWithoutPeruskoulu(hierarkia: OrganisaatioHierarkia): Option[OrganisaatioHierarkia] = {
    val children = hierarkia.children

    if (hierarkia.tila != "AKTIIVINEN" || 
      hierarkia.oppilaitostyyppi.contains("oppilaitostyyppi_11#1") ||
      hierarkia.oppilaitostyyppi.contains("oppilaitostyyppi_12#1")) {
      None
    } else {
      val filteredChildHierarkiat = children.flatMap(child => filterActiveOrgsWithoutPeruskoulu(child))
      Some(hierarkia.copy(children = filteredChildHierarkiat))
    }
  }

  def filterOnlyWantedOrgs(hierarkiat: List[OrganisaatioHierarkia]): List[OrganisaatioHierarkia] = {
    hierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterActiveOrgsWithoutPeruskoulu(hierarkia))
  }

  def addKoulutustoimijaParentToHierarkiaDescendants(
      organisaatioHierarkia: OrganisaatioHierarkia,
      koulutustoimija: Option[Organisaatio]
  ): OrganisaatioHierarkia = {
    val childrenWithKoulutustoimija = organisaatioHierarkia.children.map(child =>
      addKoulutustoimijaParentToHierarkiaDescendants(child, koulutustoimija)
    )

    organisaatioHierarkia.copy(koulutustoimijaParent = koulutustoimija, children = childrenWithKoulutustoimija)
  }
}
