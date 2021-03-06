package models

import java.util.UUID

import models.query.{QueryResult, RowDataOptions, SavedQuery, SharedResult}
import util.JsonSerializers._

sealed trait RequestMessage

case class MalformedRequest(reason: String, content: String) extends RequestMessage

case class Ping(timestamp: Long) extends RequestMessage
case object GetVersion extends RequestMessage
case class DebugInfo(data: String) extends RequestMessage

case object RefreshSchema extends RequestMessage

case class GetTableDetail(name: String) extends RequestMessage
case class GetProcedureDetail(name: String) extends RequestMessage
case class GetViewDetail(name: String) extends RequestMessage
case class GetColumnDetail(owner: String, name: String, t: String) extends RequestMessage

case object BeginTransaction extends RequestMessage
case object RollbackTransaction extends RequestMessage
case object CommitTransaction extends RequestMessage

case class CheckQuery(queryId: UUID, sql: String) extends RequestMessage
case class SubmitQuery(queryId: UUID, sql: String, params: Seq[SavedQuery.Param], action: Option[String] = None, resultId: UUID) extends RequestMessage
case class GetRowData(key: QueryResult.SourceType, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) extends RequestMessage
case class CancelQuery(queryId: UUID, resultId: UUID) extends RequestMessage
case class CloseQuery(queryId: UUID) extends RequestMessage

case class ChartDataRequest(chartId: UUID, source: QueryResult.Source) extends RequestMessage

case class QuerySaveRequest(query: SavedQuery) extends RequestMessage
case class QueryDeleteRequest(id: UUID) extends RequestMessage

case class SharedResultSaveRequest(result: SharedResult) extends RequestMessage

case class CallProcedure(queryId: UUID, name: String, params: Map[String, String], resultId: UUID) extends RequestMessage
case class RowDelete(name: String, pk: Seq[(String, String)], resultId: UUID) extends RequestMessage
case class RowUpdate(name: String, pk: Seq[(String, String)], params: Map[String, String], resultId: UUID) extends RequestMessage

case class GetQueryHistory(limit: Int = 100, offset: Int = 0) extends RequestMessage
case class InsertAuditHistory(id: UUID) extends RequestMessage
case class RemoveAuditHistory(id: Option[UUID]) extends RequestMessage

object RequestMessage {
  implicit val jsonEncoder: Encoder[RequestMessage] = deriveEncoder
  implicit val jsonDecoder: Decoder[RequestMessage] = deriveDecoder
}
