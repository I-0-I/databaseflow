package licensing

import enumeratum._

sealed abstract class LicenseEdition(val id: String, val title: String) extends EnumEntry {
  override def toString = id
}

object LicenseEdition extends Enum[LicenseEdition] {
  case object NonCommercial extends LicenseEdition("non-commercial", "Non-commercial")
  case object Personal extends LicenseEdition("personal", "Personal Edition")
  case object Team extends LicenseEdition("team", "Team Edition")

  override val values = findValues
}
