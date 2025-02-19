package fi.oph.ovara.backend.domain

case class HakeneetHyvaksytytVastaanottaneet(
                                                    hakukohdeOid: String,
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
                                                    toive7: Int) {
}

case class HakeneetHyvaksytytVastaanottaneetSummary(yhteensa: Int,
                                                    yksittaisetHakijat: Int) {

}