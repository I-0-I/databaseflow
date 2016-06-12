package models.queries.result

import java.sql.Timestamp
import java.util.UUID

import models.database.{Row, Statement}
import models.queries.BaseQueries
import models.result.CachedResult
import services.schema.JdbcHelper
import utils.{DateUtils, JdbcUtils}

object CachedResultQueries extends BaseQueries[CachedResult] {
  override protected val tableName = "query_results"
  override protected val columns = Seq(
    "id", "query_id", "connection_id", "owner", "status", "sql", "columns", "rows", "first_message", "duration", "last_accessed", "created"
  )
  override protected val searchColumns = Seq("id", "query_id", "connection_id", "status", "sql")

  val insert = Insert
  def removeById(id: UUID) = RemoveById(Seq(id))
  def getById(id: UUID) = GetById(Seq(id))
  val getAll = GetAll
  def findBy(queryId: UUID, owner: Option[UUID]) = owner match {
    case Some(o) => GetAll(whereClause = Some(""""query_id" = ? and "owner" = ?"""), values = Seq(queryId, o))
    case None => GetAll(whereClause = Some(""""query_id" = ? and "owner" is null"""), values = Seq(queryId))
  }
  val search = Search
  val removeById = RemoveById

  case class UpdateLastAccessed(id: UUID, lastAccessed: Option[Long]) extends Statement {
    override val sql = updateSql(Seq("last_accessed"))
    override val values = Seq[Any](lastAccessed, id)
  }

  case class SetFirstMessageDuration(id: UUID, firstMessageElapsed: Int) extends Statement {
    override val sql = updateSql(Seq("first_message"))
    override val values = Seq[Any](firstMessageElapsed, id)
  }

  case class Complete(id: UUID, rowCount: Int, duration: Int) extends Statement {
    override val sql = updateSql(Seq("rows", "status", "duration"))
    override val values = Seq[Any](rowCount, "complete", duration, id)
  }

  override protected def fromRow(row: Row) = {
    CachedResult(
      resultId = row.as[UUID]("id"),
      queryId = row.as[UUID]("query_id"),
      connectionId = row.as[UUID]("connection_id"),
      owner = row.asOpt[UUID]("owner"),
      status = row.as[String]("status"),
      sql = JdbcHelper.stringVal(row.as[Any]("sql")),
      columns = row.as[Int]("columns"),
      rows = row.as[Int]("rows"),
      firstMessage = row.as[Int]("first_message"),
      duration = row.as[Int]("duration"),
      lastAccessed = JdbcUtils.toLocalDateTime(row, "last_accessed"),
      created = JdbcUtils.toLocalDateTime(row, "created")
    )
  }

  override protected def toDataSeq(q: CachedResult) = Seq[Any](
    q.resultId, q.queryId, q.connectionId, q.owner, q.status,
    q.sql, q.columns, q.rows, q.duration, q.firstMessage,
    new Timestamp(DateUtils.toMillis(q.lastAccessed)), new Timestamp(DateUtils.toMillis(q.created))
  )
}
