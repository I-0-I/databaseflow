package com.databaseflow.services.scalaexport.thrift.file

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.thrift.{ThriftIntegerEnum, ThriftStringEnum}
import com.databaseflow.services.scalaexport.ExportHelper

object ThriftEnumSchemaFile {
  def exportString(pkg: Seq[String], e: ThriftStringEnum) = {
    export(pkg, e.name, e.values, "String")
  }

  def exportInt(pkg: Seq[String], e: ThriftIntegerEnum) = {
    export(pkg, e.name, e.fields.map(f => f._1), "Int")
  }

  def export(pkg: Seq[String], name: String, vals: Seq[String], t: String) = {
    val file = ScalaFile(pkg :+ "graphql", name + "Schema")

    file.addImport(pkg.mkString("."), name)
    file.addImport("graphql.CommonSchema", s"derive${t}EnumeratumType")
    file.addImport("sangria.schema", "EnumType")
    file.add(s"""object ${name}Schema {""", 1)
    file.add(s"""implicit val ${ExportHelper.toIdentifier(name)}Type: EnumType[$name] = derive${t}EnumeratumType("$name", $name.values)""")
    file.add("}", -1)

    file
  }
}
