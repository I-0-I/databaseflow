package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportEnum
import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.services.scalaexport.ExportHelper

object EnumFile {
  def export(enum: ExportEnum) = {
    val file = ScalaFile(pkg = enum.modelPackage, key = enum.className, core = true)

    file.addImport("enumeratum.values", "StringEnumEntry")
    file.addImport("enumeratum.values", "StringEnum")
    file.addImport("enumeratum.values", "StringCirceEnum")

    file.add(s"sealed abstract class ${enum.className}(override val value: String) extends StringEnumEntry")
    file.add()

    file.add(s"object ${enum.className} extends StringEnum[${enum.className}] with StringCirceEnum[${enum.className}] {", 1)
    addFields(enum, file)
    file.add()
    file.add("override val values = findValues")
    file.add("}", -1)

    file
  }

  private[this] def addFields(model: ExportEnum, file: ScalaFile) = model.values.foreach { v =>
    val cn = ExportHelper.toClassName(ExportHelper.toIdentifier(v))
    file.add(s"""case object $cn extends ${model.className}("$v")""")
  }
}
