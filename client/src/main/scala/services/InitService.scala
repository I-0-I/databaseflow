package services

import java.net.URLDecoder
import java.util.UUID

import models.RequestMessage
import org.scalajs.dom
import org.scalajs.jquery.{jQuery => $}
import scribe.Logging
import services.query.TransactionService
import ui._
import ui.editor.EditorCreationHelper
import ui.metadata.{MetadataManager, ModelListManager}
import ui.modal._
import ui.query._
import ui.search.SearchManager
import ui.tabs.TabManager
import util.{LogHelper, NetworkMessage, TemplateUtils}

import scala.scalajs.js

object InitService extends Logging {
  def init(sendMessage: (RequestMessage) => Unit, connect: () => Unit): Unit = {
    LogHelper.init(debug = false)
    NetworkMessage.register(sendMessage)
    wireSideNav()
    installTimers()

    TemplateUtils.clickHandler($("#commit-button"), _ => TransactionService.commitTransaction())
    TemplateUtils.clickHandler($("#rollback-button"), _ => TransactionService.rollbackTransaction())

    js.Dynamic.global.$("select").material_select()

    EditorCreationHelper.initEditorFramework()
    SearchManager.init()

    ShortcutService.init()
    TextChangeService.init()
    ConfirmManager.init()
    ReconnectManager.init()
    SavedQueryFormManager.init()
    RowDetailManager.init()
    RowUpdateManager.init()
    SharedResultFormManager.init()
    QueryExportFormManager.init()
    PlanNodeDetailManager.init()
    ColumnDetailManager.init()
    connect()
  }

  private[this] def wireSideNav() = {
    TemplateUtils.clickHandler($("#begin-tx-link"), _ => TransactionService.beginTransaction())
    TemplateUtils.clickHandler($("#new-query-link"), _ => AdHocQueryManager.addNewQuery())
    TemplateUtils.clickHandler($(".show-list-link"), jq => ModelListManager.showList(jq.data("key").toString))
    TemplateUtils.clickHandler($("#sidenav-help-link"), _ => HelpManager.show())
    TemplateUtils.clickHandler($("#sidenav-feedback-link"), _ => FeedbackManager.show())
    TemplateUtils.clickHandler($("#sidenav-refresh-link"), _ => MetadataManager.refreshSchema())
    TemplateUtils.clickHandler($("#sidenav-history-link"), _ => HistoryManager.show())
    js.Dynamic.global.$(".button-collapse").sideNav()
  }

  def performInitialAction() = {
    TabManager.initIfNeeded()
    NavigationService.initialMessage match {
      case ("help", None) => HelpManager.show()
      case ("feedback", None) => FeedbackManager.show()
      case ("history", None) => HistoryManager.show()
      case ("list", Some(key)) => ModelListManager.showList(key)
      case ("new", None) => AdHocQueryManager.addNewQuery()
      case ("new", Some(id)) => AdHocQueryManager.addNewQuery(queryId = UUID.fromString(id))
      case ("saved-query", Some(id)) => SavedQueryManager.savedQueryDetail(UUID.fromString(id))
      case ("shared-result", Some(id)) => SharedResultManager.sharedResultDetail(UUID.fromString(id))
      case ("table", Some(id)) => TableManager.forString(id)
      case ("view", Some(id)) => ViewManager.viewDetail(id)
      case ("procedure", Some(id)) => ProcedureManager.procedureDetail(id)
      case ("sql", Some(sql)) => AdHocQueryManager.addNewQuery(initialSql = Some(URLDecoder.decode(sql, "UTF-8")))
      case (key, id) =>
        logger.info(s"Unhandled initial message [$key:${id.getOrElse("")}].")
        AdHocQueryManager.addNewQuery()
    }
  }

  def installTimers() = {
    dom.window.setInterval(() => TemplateUtils.relativeTime(), 1000)
  }
}
