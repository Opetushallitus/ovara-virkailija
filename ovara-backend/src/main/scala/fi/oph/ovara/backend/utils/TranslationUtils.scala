package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.{Kieli, Kielistetty}

object TranslationUtils {
  def getKielistettyValue(kielistetty: Kielistetty, kieli: String): String =
    kielistetty.getOrElse(Kieli.withName(kieli), "")
}
