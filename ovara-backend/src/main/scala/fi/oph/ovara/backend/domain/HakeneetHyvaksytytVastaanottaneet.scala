package fi.oph.ovara.backend.domain

case class HakeneetHyvaksytytVastaanottaneet(
                                                    hakukohdeOid: String,
                                                    hakukohdeNimi: Kielistetty,
                                                    hakijat: Int,
                                                    ensisijaisia: Int,
                                                    varasija: Int,
                                                    hyvaksytyt: Int,
                                                    vastaanottaneet: Int,
                                                    lasna: Int,
                                                    poissa: Int,
                                                    ilmYht: Int,
                                                    aloituspaikat: Int) {
}

case class HakeneetHyvaksytytVastaanottaneetSummary(yhteensa: Int,
                                                    yksittaisetHakijat: Int) {

}