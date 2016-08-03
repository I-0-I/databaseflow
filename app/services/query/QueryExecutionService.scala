package services.query

import java.sql.PreparedStatement
import java.util.UUID

import akka.actor.ActorRef
import models._
import models.audit.AuditType
import models.database.Queryable
import models.query.{QueryResult, SavedQuery, SqlParser}
import models.result.{CachedResult, CachedResultQuery}
import models.user.User
import services.audit.AuditRecordService
import services.database.DatabaseWorkerPool
import utils.{DateUtils, ExceptionUtils, JdbcUtils, Logging}

import scala.util.control.NonFatal

object QueryExecutionService extends Logging {
  private[this] val activeQueries = collection.mutable.HashMap.empty[UUID, PreparedStatement]

  def handleQuerySaveRequest(user: Option[User], sq: SavedQuery, out: ActorRef) = {
    log.info(s"Saving query as [${sq.id}].")
    try {
      val result = SavedQueryService.save(sq, user.map(_.id))
      AuditRecordService.create(AuditType.SaveQuery, user.map(_.id), None, Some(result.id.toString))
      out ! QuerySaveResponse(savedQuery = result)
    } catch {
      case NonFatal(x) => out ! QuerySaveResponse(error = Some(x.getMessage), savedQuery = sq)
    }
  }

  def handleQueryDeleteRequest(user: Option[User], id: UUID, out: ActorRef) = {
    log.info(s"Deleting query [$id].")
    try {
      SavedQueryService.delete(id, user.map(_.id))
      AuditRecordService.create(AuditType.DeleteQuery, user.map(_.id), None, Some(id.toString))
      out ! QueryDeleteResponse(id = id)
    } catch {
      case NonFatal(x) => out ! QueryDeleteResponse(id = id, error = Some(x.getMessage))
    }
  }

  def handleRunQuery(db: Queryable, queryId: UUID, sql: String, resultId: UUID, connectionId: UUID, owner: Option[UUID], out: ActorRef) = {
    val statements = SqlParser.split(sql)
    statements.zipWithIndex.foreach { statement =>
      // TODO
      handleRunStatement(db, queryId, statement._1._1, statement._1._2, resultId, connectionId, owner, out)
    }
  }

  def handleRunStatement(db: Queryable, queryId: UUID, sql: String, idx: Int, resultId: UUID, connectionId: UUID, owner: Option[UUID], out: ActorRef) = {
    val startMs = DateUtils.nowMillis
    val auditId = UUID.randomUUID

    def work() = {
      log.info(s"Performing query action [run] with resultId [$resultId] for query [$queryId] with sql [$sql].")
      val startMs = DateUtils.nowMillis
      JdbcUtils.sqlCatch(queryId, sql, startMs, resultId) { () =>
        val model = CachedResult(resultId, queryId, connectionId, owner, sql = sql)
        AuditRecordService.start(auditId, AuditType.Query, owner, Some(connectionId), Some(sql))
        val result = db.executeUnknown(CachedResultQuery(model, Some(out)), Some(resultId))

        val durationMs = (DateUtils.nowMillis - startMs).toInt
        result match {
          case Left(rowCount) => rowCount
          case Right(i) => QueryResultResponse(resultId, idx, QueryResult(
            queryId = queryId,
            sql = sql,
            isStatement = true,
            rowsAffected = i,
            occurred = startMs
          ), durationMs)
        }
      }
    }

    def onSuccess(rm: ResponseMessage) = {
      activeQueries.remove(resultId)
      rm match {
        case m: QueryResultResponse =>
          val newType = if (m.result.isStatement) { AuditType.Execute } else { AuditType.Query }
          AuditRecordService.complete(auditId, newType, m.result.rowsAffected, (DateUtils.nowMillis - startMs).toInt)
        case m: QueryResultRowCount => AuditRecordService.complete(auditId, AuditType.Query, m.count, (DateUtils.nowMillis - startMs).toInt)
        case m: QueryErrorResponse => AuditRecordService.error(auditId, m.error.message, (DateUtils.nowMillis - startMs).toInt)
        case _ => throw new IllegalStateException(rm.getClass.getSimpleName)
      }
      out ! rm
    }

    def onFailure(t: Throwable) = {
      AuditRecordService.error(auditId, t.getMessage, (DateUtils.nowMillis - startMs).toInt)
      ExceptionUtils.actorErrorFunction(out, "PlanError", t)
    }

    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }

  def handleCancelQuery(queryId: UUID, resultId: UUID, out: ActorRef) = {
    activeQueries.get(resultId).foreach(_.cancel())
    out ! QueryCancelledResponse(queryId, resultId)
  }

  def registerRunningStatement(resultId: UUID, stmt: PreparedStatement) = activeQueries(resultId) = stmt
}
