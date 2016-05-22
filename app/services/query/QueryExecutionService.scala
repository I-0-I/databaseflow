package services.query

import java.sql.PreparedStatement
import java.util.UUID

import akka.actor.ActorRef
import models._
import models.queries.DynamicQuery
import models.query.{ QueryResult, SavedQuery }
import models.user.User
import services.database.{ DatabaseConnection, DatabaseWorkerPool }
import utils.{ DateUtils, ExceptionUtils, JdbcUtils, Logging }

import scala.util.control.NonFatal

object QueryExecutionService extends Logging {
  private[this] val activeQueries = collection.mutable.HashMap.empty[UUID, PreparedStatement]

  def handleQuerySaveRequest(user: Option[User], sq: SavedQuery, out: ActorRef) = {
    log.info(s"Saving query as [${sq.id}].")
    try {
      val result = SavedQueryService.save(sq, user.map(_.id))
      out ! QuerySaveResponse(savedQuery = result)
    } catch {
      case NonFatal(x) => out ! QuerySaveResponse(error = Some(x.getMessage), savedQuery = sq)
    }
  }

  def handleQueryDeleteRequest(user: Option[User], id: UUID, out: ActorRef) = {
    log.info(s"Deleting query [$id].")
    try {
      SavedQueryService.delete(id, user.map(_.id))
      out ! QueryDeleteResponse(id = id)
    } catch {
      case NonFatal(x) => out ! QueryDeleteResponse(id = id, error = Some(x.getMessage))
    }
  }

  def handleRunQuery(db: DatabaseConnection, queryId: UUID, sql: String, resultId: UUID, out: ActorRef) = {
    def work() = {
      log.info(s"Performing query action [run] with resultId [$resultId] for query [$queryId] with sql [$sql].")
      val startMs = DateUtils.nowMillis
      JdbcUtils.sqlCatch(queryId, sql, startMs, resultId) { () =>
        val result = db.executeUnknown(DynamicQuery(sql), Some(resultId))

        val durationMs = (DateUtils.nowMillis - startMs).toInt
        result match {
          case Left(rs) => QueryResultResponse(resultId, Seq(QueryResult(
            queryId = queryId,
            sql = sql,
            columns = rs.cols,
            data = rs.data,
            rowsAffected = rs.data.length,
            occurred = startMs
          )), durationMs)
          case Right(i) => QueryResultResponse(resultId, Seq(QueryResult(
            queryId = queryId,
            sql = sql,
            isStatement = true,
            rowsAffected = i,
            occurred = startMs
          )), durationMs)
        }
      }
    }
    def onSuccess(rm: ResponseMessage) = {
      activeQueries.remove(resultId)
      out ! rm
    }
    def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "PlanError", t)
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }

  def handleCancelQuery(db: DatabaseConnection, queryId: UUID, resultId: UUID, out: ActorRef) = {
    activeQueries.get(resultId).foreach(_.cancel())
  }

  def registerRunningStatement(resultId: UUID, stmt: PreparedStatement) = activeQueries(resultId) = stmt
}