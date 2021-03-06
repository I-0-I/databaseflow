package ui.editor

import java.util.UUID

import models.query.QueryCheckResult
import org.scalajs.jquery.{jQuery => $}
import scribe.Logging
import ui.query.{ParameterManager, SqlManager}
import ui.tabs.TabManager

import scala.scalajs.js

object EditorManager extends Logging {
  def onSave(id: UUID) = {
    val queryPanel = $(s"#panel-$id")
    val saveLink = $(".save-query-link", queryPanel)
    if (saveLink.length != 1) {
      logger.warn(s"Found [${queryPanel.length}] panels and [${saveLink.length}] links for query [$id].")
    }
    saveLink.click()
  }

  def onRun(id: UUID) = {
    val queryPanel = $(s"#panel-$id")
    val runLink = $(".run-query-link", queryPanel)
    if (runLink.length != 1) {
      logger.warn(s"Found [${queryPanel.length}] panels and [${runLink.length}] links for query [$id].")
    }
    runLink.click()
  }

  def onRunAll(id: UUID) = {
    val queryPanel = $(s"#panel-$id")
    val runAllLink = $(".run-query-all-link", queryPanel)
    if (runAllLink.length != 1) {
      logger.warn(s"Found [${queryPanel.length}] panels and [${runAllLink.length}] links for query [$id].")
    }
    runAllLink.click()
  }

  def focusActive() = TabManager.getActiveTab.foreach { id =>
    SqlManager.getEditor(id).foreach(x => $(x).focus())
  }

  def highlightErrors(queryId: UUID, results: Seq[QueryCheckResult]) = SqlManager.getEditor(queryId).foreach { editor =>
    val doc = editor.getSession().getDocument()
    val fullSql = editor.getValue().toString
    val params = ParameterManager.getParams(fullSql, queryId)._2
    val merged = ParameterManager.merge(fullSql, params)
    val errors = results.filter(_.error.isDefined)
    if (errors.isEmpty) {
      editor.getSession().clearAnnotations()
    } else {
      val errorMarkers = errors.flatMap(r => getError(doc, merged, r.sql, r.error.getOrElse(throw new IllegalArgumentException), r.index))
      editor.getSession().setAnnotations(js.Array(errorMarkers: _*))
    }
  }

  private[this] def getError(doc: js.Dynamic, fullSql: String, sql: String, error: String, index: Option[Int]) = {
    fullSql.indexOf(sql) match {
      case -1 => None
      case sqlIndex =>
        val startIndex = sqlIndex + index.getOrElse(0)
        val endIndex = fullSql.indexOf(';', startIndex) match {
          case -1 => fullSql.indexOf('\n', startIndex) match {
            case -1 => fullSql.indexOf(' ', startIndex) match {
              case -1 => fullSql.length
              case x => x
            }
            case x => x
          }
          case x => x
        }

        val pos = doc.indexToPosition(endIndex)

        Some(js.Dynamic.literal(
          row = pos.row,
          column = pos.column,
          text = error,
          `type` = "error"
        ))
    }
  }
}
