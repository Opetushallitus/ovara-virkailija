package fi.oph.ovara.backend.domain

sealed trait HakeneetHyvaksytytVastaanottaneet {
  val hakijat: Int
  val ensisijaiset: Int
  val varasijalla: Int
  val hyvaksytyt: Int
  val vastaanottaneet: Int
  val lasna: Int
  val poissa: Int
  val ilmoittautuneetYhteensa: Int
  val aloituspaikat: Int
}

case class HakeneetHyvaksytytVastaanottaneetResult(
                                                    hakukohdeNimi: Kielistetty,
                                                    hakijat: Int,
                                                    ensisijaiset: Int,
                                                    varasijalla: Int,
                                                    hyvaksytyt: Int,
                                                    vastaanottaneet: Int,
                                                    lasna: Int,
                                                    poissa: Int,
                                                    ilmoittautuneetYhteensa: Int,
                                                    aloituspaikat: Int) extends HakeneetHyvaksytytVastaanottaneet {

}

case class HakeneetHyvaksytytVastaanottaneetSummary(
                                                    otsikko: String,
                                                    hakijat: Int,
                                                    ensisijaiset: Int,
                                                    varasijalla: Int,
                                                    hyvaksytyt: Int,
                                                    vastaanottaneet: Int,
                                                    lasna: Int,
                                                    poissa: Int,
                                                    ilmoittautuneetYhteensa: Int,
                                                    aloituspaikat: Int) extends HakeneetHyvaksytytVastaanottaneet {

}