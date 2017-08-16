package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectExplore {
  def injectMenu(result: ExportResult, rootDir: File) = {
    def queryFieldsFor(s: String) = {
      val newContent = result.models.flatMap { model =>
        if (model.provided) { None } else { Some(s"""  <li><a href="@${model.routesClass}.list()">${model.title}</a></li>""") }
      }.sorted.mkString("\n")
      InjectHelper.replaceBetween(
        original = s, start = "  <!-- Start model list routes -->", end = "  <!-- End model list routes -->", newContent = newContent
      )
    }

    val schemaSourceFile = rootDir / "app" / "views" / "layout" / "adminMenu.scala.html"
    val newContent = queryFieldsFor(schemaSourceFile.contentAsString)
    schemaSourceFile.overwrite(newContent)

    "adminMenu.scala.html" -> newContent
  }

  def injectHtml(result: ExportResult, rootDir: File) = {
    def queryFieldsFor(s: String) = {
      val newContent = result.models.flatMap { model =>
        if (model.provided) {
          None
        } else {
          Some(s"""      <li class="collection-item">
          <a class="theme-text" href="@${model.routesClass}.list()">${model.title} Management</a>
          <div><em>Manage the ${model.propertyName} of the system.</em></div>
        </li>""")
        }
      }.sorted.mkString("\n")

      InjectHelper.replaceBetween(
        original = s, start = "      <!-- Start model list routes -->", end = "      <!-- End model list routes -->", newContent = newContent
      )
    }

    val schemaSourceFile = rootDir / "app" / "views" / "admin" / "explore" / "explore.scala.html"
    val newContent = queryFieldsFor(schemaSourceFile.contentAsString)
    schemaSourceFile.overwrite(newContent)

    "explore.scala.html" -> newContent
  }
}
