package fi.oph.ovara.backend.domain

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
                                                    toive7: Int) extends HakeneetHyvaksytytVastaanottaneetBase

case class HakeneetHyvaksytytVastaanottaneetToimipisteittain(
                                                             toimipiste: String,
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
                                                             toive7: Int) extends HakeneetHyvaksytytVastaanottaneetBase

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
                                                             toive7: Int) extends HakeneetHyvaksytytVastaanottaneetBase {
}

object HakeneetHyvaksytytVastaanottaneetResult {
  def apply(hakukohteittain: HakeneetHyvaksytytVastaanottaneetHakukohteittain): HakeneetHyvaksytytVastaanottaneetResult = {
    val combinedName: Kielistetty = mergeKielistetty(hakukohteittain.hakukohdeNimi, hakukohteittain.organisaatioNimi)
    new HakeneetHyvaksytytVastaanottaneetResult(
      otsikko = combinedName,
      hakijat = hakukohteittain.hakijat,
      ensisijaisia = hakukohteittain.ensisijaisia,
      varasija = hakukohteittain.varasija,
      hyvaksytyt = hakukohteittain.hyvaksytyt,
      vastaanottaneet = hakukohteittain.vastaanottaneet,
      lasna = hakukohteittain.lasna,
      poissa = hakukohteittain.poissa,
      ilmYht = hakukohteittain.ilmYht,
      aloituspaikat = hakukohteittain.aloituspaikat,
      toive1 = hakukohteittain.toive1,
      toive2 = hakukohteittain.toive2,
      toive3 = hakukohteittain.toive3,
      toive4 = hakukohteittain.toive4,
      toive5 = hakukohteittain.toive5,
      toive6 = hakukohteittain.toive6,
      toive7 = hakukohteittain.toive7
    )
  }

  def apply(toimipisteittain: HakeneetHyvaksytytVastaanottaneetToimipisteittain): HakeneetHyvaksytytVastaanottaneetResult = {
    new HakeneetHyvaksytytVastaanottaneetResult(
      otsikko = toimipisteittain.organisaatioNimi,
      hakijat = toimipisteittain.hakijat,
      ensisijaisia = toimipisteittain.ensisijaisia,
      varasija = toimipisteittain.varasija,
      hyvaksytyt = toimipisteittain.hyvaksytyt,
      vastaanottaneet = toimipisteittain.vastaanottaneet,
      lasna = toimipisteittain.lasna,
      poissa = toimipisteittain.poissa,
      ilmYht = toimipisteittain.ilmYht,
      aloituspaikat = toimipisteittain.aloituspaikat,
      toive1 = toimipisteittain.toive1,
      toive2 = toimipisteittain.toive2,
      toive3 = toimipisteittain.toive3,
      toive4 = toimipisteittain.toive4,
      toive5 = toimipisteittain.toive5,
      toive6 = toimipisteittain.toive6,
      toive7 = toimipisteittain.toive7
    )
  }

  def mergeKielistetty(a: Kielistetty, b: Kielistetty): Kielistetty = {
    (a.keys ++ b.keys).map { key =>
      key -> List(a.getOrElse(key, ""), b.getOrElse(key, "")).filter(_.nonEmpty).mkString("\n")
    }.toMap
  }
}