package com.databaseflow.services.scalaexport.db.inject

import better.files.File
import com.databaseflow.models.scalaexport.db.ExportResult

object InjectAuditRoutes {
  def inject(result: ExportResult, rootDir: File) = {
    def routeFieldsFor(s: String) = {
      val newContent = result.models.filterNot(_.provided).filter(_.pkFields.nonEmpty).map { model =>
        s"""    case "${model.propertyName.toLowerCase}" => ${model.routesClass}.view(${model.pkArgs})"""
      }.sorted.mkString("\n")

      InjectHelper.replaceBetween(original = s, start = "    /* Start audit calls */", end = "    /* End audit calls */", newContent = newContent)
    }

    val file = rootDir / "app" / (result.config.pkgPrefix :+ "services").mkString("/") / "audit" / "AuditRoutes.scala"
    val newContent = routeFieldsFor(file.contentAsString)
    file.overwrite(newContent)

    "AuditRoutes.scala" -> newContent
  }
}
