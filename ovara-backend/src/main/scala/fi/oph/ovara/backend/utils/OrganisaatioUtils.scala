package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*

object OrganisaatioUtils {
  def mapOrganisaationHakukohteetToParent(
      hierarkia: OrganisaatioHierarkia,
      organisaatiotWithHakukohteet: Map[Option[String], Vector[KoulutuksetToteutuksetHakukohteetResult]]
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
      organisaatiotWithHakukohteet: Map[Option[String], Vector[KoulutuksetToteutuksetHakukohteetResult]]
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
}
