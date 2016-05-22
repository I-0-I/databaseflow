package services.socket

import models._
import models.queries.query.SavedQueryQueries
import models.query.SavedQuery
import models.schema.Schema
import services.database.{ DatabaseWorkerPool, MasterDatabase }
import services.schema.SchemaService
import utils.{ ExceptionUtils, Logging }

import scala.util.{ Failure, Success, Try }

trait StartHelper extends Logging { this: SocketService =>
  protected[this] def attemptConnect() = MasterDatabase.databaseFor(connectionId) match {
    case Right(d) => Some(d)
    case Left(x) =>
      log.warn("Error attempting to connect to database.", x)
      out ! ServerError("Database Connect Failed", x.getMessage)
      None
  }

  protected[this] def onStart() = {
    log.info(s"Starting connection for user [${user.map(_.id)}: $currentUsername].")

    supervisor ! SocketStarted(user, id, self)
    out ! UserSettings(user.map(_.id), currentUsername, userPreferences)

    val sqq = SavedQueryQueries.getForUser(user.map(_.id), connectionId)
    def onSavedQueriesSuccess(savedQueries: Seq[SavedQuery]) { out ! SavedQueryResultResponse(savedQueries, 0) }
    def onSavedQueriesFailure(t: Throwable) { ExceptionUtils.actorErrorFunction(out, "SavedQueryLoadException", t) }
    DatabaseWorkerPool.submitQuery(sqq, MasterDatabase.conn, onSavedQueriesSuccess, onSavedQueriesFailure)

    refreshSchema()
  }

  protected[this] def refreshSchema() = {
    def onSchemaSuccess(t: Try[Schema]): Unit = t match {
      case Success(sch) =>
        schema = Some(sch)
        out ! SchemaResultResponse(sch)
        if (sch.detailsLoadedAt.isEmpty) {
          def onRefreshSuccess(s: Schema) = {
            schema = Some(s)
            out ! SchemaResultResponse(s)
          }
          def onRefreshFailure(x: Throwable) = ExceptionUtils.actorErrorFunction(out, "SchemaDetailError", x)
          SchemaService.refreshSchema(db, onRefreshSuccess, onRefreshFailure)
        }

      case Failure(x) => ExceptionUtils.actorErrorFunction(out, "SchemaLoadError", x)
    }
    def onSchemaFailure(t: Throwable) { ExceptionUtils.actorErrorFunction(out, "SchemaLoadException", t) }
    DatabaseWorkerPool.submitWork(() => SchemaService.getSchema(db), onSchemaSuccess, onSchemaFailure)
  }
}