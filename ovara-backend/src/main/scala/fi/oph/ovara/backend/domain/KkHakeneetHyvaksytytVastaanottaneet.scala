package fi.oph.ovara.backend.domain

import fi.oph.ovara.backend.domain.DomainUtils.mergeKielistetty

abstract class KkHakeneetHyvaksytytVastaanottaneetBase {
  val hakijat: Int
  val ensisijaisia: Int
  val ensikertalaisia: Int
  val hyvaksytyt: Int
  val vastaanottaneet: Int
  val lasna: Int
  val poissa: Int
  val ilmYht: Int
  val maksuvelvollisia: Int
  val valinnanAloituspaikat: Int
  val aloituspaikat: Int
  val toive1: Int
  val toive2: Int
  val toive3: Int
  val toive4: Int
  val toive5: Int
  val toive6: Int
}

case class KkHakeneetHyvaksytytVastaanottaneetOrganisaatioNimella(
                                                                   otsikko: Kielistetty,
                                                                   organisaatioNimi: Kielistetty,
                                                                   hakijat: Int,
                                                                   ensisijaisia: Int,
                                                                   ensikertalaisia: Int,
                                                                   hyvaksytyt: Int,
                                                                   vastaanottaneet: Int,
                                                                   lasna: Int,
                                                                   poissa: Int,
                                                                   ilmYht: Int,
                                                                   maksuvelvollisia: Int,
                                                                   valinnanAloituspaikat: Int,
                                                                   aloituspaikat: Int,
                                                                   toive1: Int,
                                                                   toive2: Int,
                                                                   toive3: Int,
                                                                   toive4: Int,
                                                                   toive5: Int,
                                                                   toive6: Int) extends KkHakeneetHyvaksytytVastaanottaneetBase

case class KkHakeneetHyvaksytytVastaanottaneetResult(
                                                      otsikko: Kielistetty,
                                                      hakijat: Int,
                                                      ensisijaisia: Int,
                                                      ensikertalaisia: Int,
                                                      hyvaksytyt: Int,
                                                      vastaanottaneet: Int,
                                                      lasna: Int,
                                                      poissa: Int,
                                                      ilmYht: Int,
                                                      maksuvelvollisia: Int,
                                                      valinnanAloituspaikat: Int,
                                                      aloituspaikat: Int,
                                                      toive1: Int,
                                                      toive2: Int,
                                                      toive3: Int,
                                                      toive4: Int,
                                                      toive5: Int,
                                                      toive6: Int) extends KkHakeneetHyvaksytytVastaanottaneetBase

object KkHakeneetHyvaksytytVastaanottaneetResult {
  def apply(tilastoOrgNimella: KkHakeneetHyvaksytytVastaanottaneetOrganisaatioNimella): KkHakeneetHyvaksytytVastaanottaneetResult = {
    val combinedName: Kielistetty = mergeKielistetty(tilastoOrgNimella.otsikko, tilastoOrgNimella.organisaatioNimi)
    new KkHakeneetHyvaksytytVastaanottaneetResult(
      otsikko = combinedName,
      hakijat = tilastoOrgNimella.hakijat,
      ensisijaisia = tilastoOrgNimella.ensisijaisia,
      ensikertalaisia = tilastoOrgNimella.ensikertalaisia,
      hyvaksytyt = tilastoOrgNimella.hyvaksytyt,
      vastaanottaneet = tilastoOrgNimella.vastaanottaneet,
      lasna = tilastoOrgNimella.lasna,
      poissa = tilastoOrgNimella.poissa,
      ilmYht = tilastoOrgNimella.ilmYht,
      maksuvelvollisia = tilastoOrgNimella.maksuvelvollisia,
      valinnanAloituspaikat = tilastoOrgNimella.valinnanAloituspaikat,
      aloituspaikat = tilastoOrgNimella.aloituspaikat,
      toive1 = tilastoOrgNimella.toive1,
      toive2 = tilastoOrgNimella.toive2,
      toive3 = tilastoOrgNimella.toive3,
      toive4 = tilastoOrgNimella.toive4,
      toive5 = tilastoOrgNimella.toive5,
      toive6 = tilastoOrgNimella.toive6,
    )
  }
}





