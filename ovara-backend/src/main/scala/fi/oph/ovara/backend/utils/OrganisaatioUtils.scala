package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*

object OrganisaatioUtils {
  def recursiveListParentsAndSelf(parentOid: String, orgs: Vector[OrganisaatioParentChild], foundParents: List[Organisaatio] = List()): List[Organisaatio] = {
    if (orgs.isEmpty) {
      foundParents
    } else {
      val firstOrg = orgs.head
      val orgOid = firstOrg.organisaatio.organisaatio_oid
      val orgParentOid = firstOrg.parent_oid
      if (parentOid == orgOid) {
        firstOrg.organisaatio :: recursiveListParentsAndSelf(orgParentOid, orgs.tail)
      } else if (orgOid == orgParentOid) {
        recursiveListParentsAndSelf(parentOid, orgs.tail) ::: recursiveListParentsAndSelf(orgParentOid, orgs)
      } else {
        recursiveListParentsAndSelf(parentOid, orgs.tail)
      }
    }
  }

  def mapOrganisaationHakukohteetToParent(orgs: Vector[OrganisaatioParentChild], organisaatioWithHakukohteet: Map[Option[String], Vector[KoulutuksetToteutuksetHakukohteetResult]]): List[(Organisaatio, OrganisaationKoulutuksetToteutuksetHakukohteet)] = {
    organisaatioWithHakukohteet.map(org =>
      val orgOid = org._1
      val parents = recursiveListParentsAndSelf(orgOid.get, orgs)
      val koulutustoimija = parents.filter(p => p.organisaatiotyypit.contains(KOULUTUSTOIMIJAORGANISAATIOTYYPPI))
      orgs.find(o => o.organisaatio.organisaatio_oid == orgOid.getOrElse(false)) match
        case Some(organisaatio) =>
          Some((koulutustoimija.head, OrganisaationKoulutuksetToteutuksetHakukohteet(Some(organisaatio.organisaatio), org._2)))
        case _ =>
          None
    ).toList.flatten
  }
}
