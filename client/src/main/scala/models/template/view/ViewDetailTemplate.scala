package models.template.view

import java.util.UUID

import models.engine.DatabaseEngine
import models.template.{Icons, StaticPanelTemplate}

import scalatags.Text.all._

object ViewDetailTemplate {
  private[this] def linksFor(engine: DatabaseEngine) = Seq(
    Some(a(cls := "view-data-link theme-text", href := "#")("View First 100 Rows")),
    if (engine.explain.isDefined) { Some(a(cls := "explain-view-link theme-text", href := "#")("Explain")) } else { None },
    if (engine.analyze.isDefined) { Some(a(cls := "analyze-view-link theme-text", href := "#")("Analyze")) } else { None },
    Some(a(cls := "right export-link theme-text first-right-link", href := "#")("Export"))
  ).flatten

  def forView(engine: DatabaseEngine, queryId: UUID, tableName: String) = {
    val content = div(
      div(cls := "description")(""),
      ul(cls := "collapsible table-options", data("collapsible") := "expandable")(
        li(cls := "definition-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.definition}"), "Definition"),
          div(cls := "collapsible-body")(div(cls := "section-content")("Loading..."))
        ),
        li(cls := "columns-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.columns}"), "Columns", span(cls := "badge")("")),
          div(cls := "collapsible-body")(div(cls := "section-content")("Loading..."))
        )
      )
    )

    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.cardRow(content, iconAndTitle = Some(Icons.view -> tableName), actions = linksFor(engine)),
      div(id := s"workspace-$queryId")
    )
  }
}
