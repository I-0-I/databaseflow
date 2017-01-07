package ui.query

import java.util.UUID

import models.query.SavedQuery
import models.template.Icons
import models.template.query.QueryEditorTemplate
import models.{CloseQuery, QueryDeleteRequest, QuerySaveRequest}
import org.scalajs.jquery.{jQuery => $}
import ui.metadata.MetadataManager
import ui.modal.{ConfirmManager, SavedQueryFormManager}
import ui.{TabManager, UserManager}
import utils.{NetworkMessage, TemplateUtils}

trait SavedQueryChangeManager {
  var savedQueries = Map.empty[UUID, SavedQuery]
  var openSavedQueries = Set.empty[UUID]
  var usernameMap = Map.empty[UUID, String]

  protected[this] def addSavedQuery(savedQuery: SavedQuery) = {
    val engine = MetadataManager.engine.getOrElse(throw new IllegalStateException("No Engine"))
    val userId = UserManager.userId.getOrElse(throw new IllegalStateException("Missing user details."))
    QueryManager.workspace.append(QueryEditorTemplate.forSavedQuery(engine, savedQuery, userId).toString)

    def close() = if (QueryManager.activeQueries.contains(savedQuery.id)) {
      QueryManager.closeQuery(savedQuery.id)
      openSavedQueries = openSavedQueries - savedQuery.id
      NetworkMessage.sendMessage(CloseQuery(savedQuery.id))
    }

    TabManager.addTab(savedQuery.id, "saved-query-" + savedQuery.id, savedQuery.name, Icons.savedQuery, close)

    val queryPanel = $(s"#panel-${savedQuery.id}")
    TemplateUtils.clickHandler($(".settings-query-link", queryPanel), jq => SavedQueryFormManager.show(savedQuery.copy(
      sql = SqlManager.getSql(savedQuery.id)
    )))
    TemplateUtils.clickHandler($(".save-as-query-link", queryPanel), jq => SavedQueryFormManager.show(savedQuery.copy(
      id = UUID.randomUUID,
      name = "Copy of " + savedQuery.name,
      sql = SqlManager.getSql(savedQuery.id),
      params = ParameterManager.getParamsOpt(savedQuery.id).getOrElse(Map.empty)
    )))
    TemplateUtils.clickHandler($(".save-query-link", queryPanel), jq => {
      val newSavedQuery = savedQuery.copy(
        sql = SqlManager.getSql(savedQuery.id),
        params = ParameterManager.getParamsOpt(savedQuery.id).getOrElse(Map.empty)
      )
      NetworkMessage.sendMessage(QuerySaveRequest(newSavedQuery))
    })
    TemplateUtils.clickHandler($(".delete-query-link", queryPanel), jq => {
      def callback(b: Boolean): Unit = {
        if (b) {
          NetworkMessage.sendMessage(QueryDeleteRequest(savedQuery.id))
        }
        ConfirmManager.close()
      }
      val msg = "Are you sure you want to delete this saved query?"
      ConfirmManager.show(callback = callback, content = msg, trueButton = "Delete")
    })

    val runQueryLink = $(".run-query-link", queryPanel)
    val runQueryAllLink = $(".run-query-all-link", queryPanel)

    def onChange(s: String): Unit = {
      $(".unsaved-status", queryPanel).css("display", if (s == savedQuery.sql) { "none" } else { "inline" })
      SqlManager.updateLinks(savedQuery.id, runQueryLink, runQueryAllLink)
    }

    QueryManager.addQuery(savedQuery.id, savedQuery.name, queryPanel, savedQuery.sql, savedQuery.params, onChange)
  }
}
