package com.databaseflow.services.scalaexport.db.inject

import better.files.File
import com.databaseflow.models.scalaexport.db.ExportResult

object InjectAuditLookup {
  def inject(result: ExportResult, rootDir: File) = {
    def serviceFieldsFor(s: String) = {
      val newContent = result.models.filterNot(_.provided).filterNot(_.propertyName == "audit").filter(_.pkFields.nonEmpty).map { model =>
        val svc = model.serviceReference.replaceAllLiterally("services.", "registry.")
        s"""    case "${model.propertyName.toLowerCase}" => $svc.getByPrimaryKey(creds, ${model.pkArgs})"""
      }.sorted.mkString("\n")

      InjectHelper.replaceBetween(original = s, start = "    /* Start registry lookups */", end = "    /* End registry lookups */", newContent = newContent)
    }

    val file = rootDir / "app" / (result.config.pkgPrefix :+ "services").mkString("/") / "audit" / "AuditLookup.scala"
    val newContent = serviceFieldsFor(file.contentAsString)
    file.overwrite(newContent)

    "AuditLookup.scala" -> newContent
  }
}
