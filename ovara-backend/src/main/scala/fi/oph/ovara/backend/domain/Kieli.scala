package fi.oph.ovara.backend.domain

sealed trait Kieli extends EnumType with Product with Serializable

object Kieli extends Enum[Kieli] {
  override def name: String = "kieli"
  def values: List[Kieli] = List(Fi, Sv, En)
}

case object Fi extends Kieli { val name = "fi" }
case object Sv extends Kieli { val name = "sv" }
case object En extends Kieli { val name = "en" }

