package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportConfig
import services.scalaexport.{ExportHelper, ExportTable}

object ForeignKeysHelper {
  def writeQueries(et: ExportTable, file: ScalaFile) = et.t.foreignKeys.foreach { fk =>
    fk.references.toList match {
      case h :: Nil =>
        val col = et.t.columns.find(_.name == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        val typ = col.columnType.asScala
        val propId = ExportHelper.toIdentifier(h.source)
        val propCls = ExportHelper.toClassName(h.source)
        file.add(s"""case class GetBy$propCls($propId: $typ) extends SeqQuery("where \\"${h.source}\\" = ?", Seq($propId))""")
        file.add(s"""case class GetBy${propCls}Seq(${propId}Seq: Seq[$typ]) extends ColSeqQuery("${h.source}", ${propId}Seq)""")
        file.add()
      case _ => // noop
    }
  }

  def writeService(et: ExportTable, file: ScalaFile) = et.t.foreignKeys.foreach { fk =>
    fk.references.toList match {
      case h :: Nil =>
        val col = et.t.columns.find(_.name == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        val typ = col.columnType.asScala
        col.columnType.requiredImport.foreach(pkg => file.addImport(pkg, typ))
        val propId = ExportHelper.toIdentifier(h.source)
        val propCls = ExportHelper.toClassName(h.source)
        file.add(s"""def getBy$propCls($propId: $typ) = Database.query(${et.className}Queries.GetBy$propCls($propId))""")
        file.add(s"""def getBy${propCls}Seq(${propId}Seq: Seq[$typ]) = Database.query(${et.className}Queries.GetBy${propCls}Seq(${propId}Seq))""")
        file.add()
      case _ => // noop
    }
  }

  def writeSchema(et: ExportTable, file: ScalaFile) = if (et.t.foreignKeys.nonEmpty) {
    file.addImport("sangria.execution.deferred", "Relation")
    file.addImport("sangria.execution.deferred", "Fetcher")

    et.t.foreignKeys.foreach { fk =>
      fk.references.toList match {
        case h :: Nil =>
          val col = et.t.columns.find(_.name == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
          val typ = col.columnType.asScala
          col.columnType.requiredImport.foreach(pkg => file.addImport(pkg, typ))
          val propName = ExportHelper.toIdentifier(h.source)
          val srcClass = ExportHelper.toClassName(h.source)
          val seq = if (col.notNull) { s"Seq(x.$propName)" } else { s"x.$propName.toSeq" }
          file.addMarker("fetcher", (file.pkg :+ s"${et.className}Schema" :+ s"${et.propertyName}By${srcClass}Fetcher").mkString("."))
          file.add(s"""val ${et.propertyName}By$srcClass = Relation[${et.className}, $typ]("by$srcClass", x => $seq)""")
          val relType = s"GraphQLContext, ${et.className}, ${et.className}, ${et.pkType.getOrElse("String")}"
          file.add(s"val ${et.propertyName}By${srcClass}Fetcher = Fetcher.rel[$relType](", 1)
          file.add(s"(_, ids) => ${et.className}Service.getByIdSeq(ids),")
          file.add(s"(_, rels) => ${et.className}Service.getBy${srcClass}Seq(rels(${et.propertyName}By$srcClass))")
          file.add(")", -1)

          file.add()
        case _ => // noop
      }
    }
  }

  def writeFields(et: ExportTable, file: ScalaFile, config: ExportConfig.Result) = if (et.t.foreignKeys.nonEmpty) {
    et.t.foreignKeys.foreach { fk =>
      val targetTable = et.s.getTable(fk.targetTable).getOrElse(throw new IllegalStateException(s"Missing table [${fk.targetTable}]."))
      fk.references.toList match {
        case h :: Nil =>
          val col = et.t.columns.find(_.name == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
          val typ = col.columnType.asScala
          col.columnType.requiredImport.foreach(pkg => file.addImport(pkg, typ))
          val tgtClass = ExportHelper.toClassName(targetTable.name)
          val tgtProp = ExportHelper.toIdentifier(tgtClass)
          val srcProp = ExportHelper.toIdentifier(h.source)
          val p = ExportHelper.toIdentifier(fk.targetTable)
          val cls = config.classNames.getOrElse(p, ExportHelper.toClassName(fk.targetTable))
          val pkg = config.packages.get(p).map(x => x.split("\\.").toList).getOrElse(Nil)
          if (pkg != et.pkg) {
            file.addImport(("models" +: pkg).mkString("."), cls + "Schema")
          }

          file.add("Field(", 1)
          file.add(s"""name = "${srcProp}Rel",""")
          if (col.notNull) {
            file.add(s"""fieldType = ${tgtClass}Schema.${tgtProp}Type,""")
          } else {
            file.add(s"""fieldType = OptionType(${tgtClass}Schema.${tgtProp}Type),""")
          }

          val fetcherRef = s"${tgtClass}Schema.${tgtProp}By${ExportHelper.toClassName(h.target)}Fetcher"
          if (col.notNull) {
            file.add(s"resolve = ctx => $fetcherRef.defer(ctx.value.$srcProp)")
          } else {
            file.add(s"resolve = ctx => $fetcherRef.deferOpt(ctx.value.$srcProp)")
          }
          val comma = if (et.t.foreignKeys.lastOption.contains(fk)) { "" } else { "," }
          file.add(")" + comma, -1)
        case _ => // noop
      }
    }
  }
}