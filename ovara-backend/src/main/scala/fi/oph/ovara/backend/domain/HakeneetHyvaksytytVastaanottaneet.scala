package fi.oph.ovara.backend.domain

import fi.oph.ovara.backend.domain.DomainUtils.mergeKielistetty

abstract class HakeneetHyvaksytytVastaanottaneetBase {
  val hakijat: Int
  val ensisijaisia: Int
  val varasija: Int
  val hyvaksytyt: Int
  val vastaanottaneet: Int
  val lasna: Int
  val poissa: Int
  val ilmYht: Int
  val aloituspaikat: Int
  val toive1: Int
  val toive2: Int
  val toive3: Int
  val toive4: Int
  val toive5: Int
  val toive6: Int
  val toive7: Int
}

case class HakeneetHyvaksytytVastaanottaneetHakukohteittain(
    hakukohdeNimi: Kielistetty,
    hakuNimi: Kielistetty,
    organisaatioNimi: Kielistetty,
    hakijat: Int,
    ensisijaisia: Int,
    varasija: Int,
    hyvaksytyt: Int,
    vastaanottaneet: Int,
    lasna: Int,
    poissa: Int,
    ilmYht: Int,
    aloituspaikat: Int,
    toive1: Int,
    toive2: Int,
    toive3: Int,
    toive4: Int,
    toive5: Int,
    toive6: Int,
    toive7: Int
) extends HakeneetHyvaksytytVastaanottaneetBase

case class HakeneetHyvaksytytVastaanottaneetTunnisteella(
    tunniste: String,
    otsikko: Kielistetty,
    hakijat: Int,
    ensisijaisia: Int,
    varasija: Int,
    hyvaksytyt: Int,
    vastaanottaneet: Int,
    lasna: Int,
    poissa: Int,
    ilmYht: Int,
    aloituspaikat: Int,
    toive1: Int,
    toive2: Int,
    toive3: Int,
    toive4: Int,
    toive5: Int,
    toive6: Int,
    toive7: Int
) extends HakeneetHyvaksytytVastaanottaneetBase {}

case class HakeneetHyvaksytytVastaanottaneetResult(
    otsikko: Kielistetty,
    hakijat: Int,
    ensisijaisia: Int,
    varasija: Int,
    hyvaksytyt: Int,
    vastaanottaneet: Int,
    lasna: Int,
    poissa: Int,
    ilmYht: Int,
    aloituspaikat: Int,
    toive1: Int,
    toive2: Int,
    toive3: Int,
    toive4: Int,
    toive5: Int,
    toive6: Int,
    toive7: Int
) extends HakeneetHyvaksytytVastaanottaneetBase {}

object HakeneetHyvaksytytVastaanottaneetResult {

  def apply(
             tunnisteella: HakeneetHyvaksytytVastaanottaneetTunnisteella
           ): HakeneetHyvaksytytVastaanottaneetResult = {
    new HakeneetHyvaksytytVastaanottaneetResult(
      otsikko = tunnisteella.otsikko,
      hakijat = tunnisteella.hakijat,
      ensisijaisia = tunnisteella.ensisijaisia,
      varasija = tunnisteella.varasija,
      hyvaksytyt = tunnisteella.hyvaksytyt,
      vastaanottaneet = tunnisteella.vastaanottaneet,
      lasna = tunnisteella.lasna,
      poissa = tunnisteella.poissa,
      ilmYht = tunnisteella.ilmYht,
      aloituspaikat = tunnisteella.aloituspaikat,
      toive1 = tunnisteella.toive1,
      toive2 = tunnisteella.toive2,
      toive3 = tunnisteella.toive3,
      toive4 = tunnisteella.toive4,
      toive5 = tunnisteella.toive5,
      toive6 = tunnisteella.toive6,
      toive7 = tunnisteella.toive7
    )
  }

}
