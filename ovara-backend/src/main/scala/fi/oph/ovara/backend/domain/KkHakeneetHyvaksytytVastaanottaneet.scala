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

case class KkHakeneetHyvaksytytVastaanottaneetHakukohteittain(hakukohdeNimi: Kielistetty,
                                                              hakuNimi: Kielistetty,
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

case class KkHakeneetHyvaksytytVastaanottaneetHauittain(
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


case class KkHakeneetHyvaksytytVastaanottaneetToimipisteittain(
                                                                toimipiste: String,
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
  def apply(hakukohteittain: KkHakeneetHyvaksytytVastaanottaneetHauittain): KkHakeneetHyvaksytytVastaanottaneetResult = {
    val combinedName: Kielistetty = mergeKielistetty(hakukohteittain.otsikko, hakukohteittain.organisaatioNimi)
    new KkHakeneetHyvaksytytVastaanottaneetResult(
      otsikko = combinedName,
      hakijat = hakukohteittain.hakijat,
      ensisijaisia = hakukohteittain.ensisijaisia,
      ensikertalaisia = hakukohteittain.ensikertalaisia,
      hyvaksytyt = hakukohteittain.hyvaksytyt,
      vastaanottaneet = hakukohteittain.vastaanottaneet,
      lasna = hakukohteittain.lasna,
      poissa = hakukohteittain.poissa,
      ilmYht = hakukohteittain.ilmYht,
      maksuvelvollisia = hakukohteittain.maksuvelvollisia,
      valinnanAloituspaikat = hakukohteittain.valinnanAloituspaikat,
      aloituspaikat = hakukohteittain.aloituspaikat,
      toive1 = hakukohteittain.toive1,
      toive2 = hakukohteittain.toive2,
      toive3 = hakukohteittain.toive3,
      toive4 = hakukohteittain.toive4,
      toive5 = hakukohteittain.toive5,
      toive6 = hakukohteittain.toive6,
    )
  }

  def apply(toimipisteittain: KkHakeneetHyvaksytytVastaanottaneetToimipisteittain): KkHakeneetHyvaksytytVastaanottaneetResult = {
    new KkHakeneetHyvaksytytVastaanottaneetResult(
      otsikko = toimipisteittain.organisaatioNimi,
      hakijat = toimipisteittain.hakijat,
      ensisijaisia = toimipisteittain.ensisijaisia,
      ensikertalaisia = toimipisteittain.ensikertalaisia,
      hyvaksytyt = toimipisteittain.hyvaksytyt,
      vastaanottaneet = toimipisteittain.vastaanottaneet,
      lasna = toimipisteittain.lasna,
      poissa = toimipisteittain.poissa,
      ilmYht = toimipisteittain.ilmYht,
      maksuvelvollisia = toimipisteittain.maksuvelvollisia,
      valinnanAloituspaikat = toimipisteittain.valinnanAloituspaikat,
      aloituspaikat = toimipisteittain.aloituspaikat,
      toive1 = toimipisteittain.toive1,
      toive2 = toimipisteittain.toive2,
      toive3 = toimipisteittain.toive3,
      toive4 = toimipisteittain.toive4,
      toive5 = toimipisteittain.toive5,
      toive6 = toimipisteittain.toive6,
    )
  }
}





