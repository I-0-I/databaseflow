package models.scalaexport

case class ExportResult(
  id: String,
  files: Seq[(String, String)]
)
