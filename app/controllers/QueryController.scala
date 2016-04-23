package controllers

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.mohiva.play.silhouette.api.HandlerResult
import models.{ RequestMessage, ResponseMessage }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.streams.ActorFlow
import play.api.mvc.{ AnyContentAsEmpty, Request, WebSocket }
import services.connection.{ ConnectionService, ConnectionSettingsService }
import utils.ApplicationContext
import utils.web.MessageFrameFormatter

import scala.concurrent.Future

@javax.inject.Singleton
class QueryController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    implicit val system: ActorSystem,
    implicit val materializer: Materializer
) extends BaseController {
  def main(connectionId: UUID) = withSession(s"connection-$connectionId") { implicit request =>
    val activeDb = ConnectionSettingsService.getById(connectionId).map(c => c.name -> c.id)
    Future.successful(Ok(views.html.query.main(request.identity, ctx.config.debug, activeDb.map(_._1).getOrElse("..."), UUID.randomUUID)))
  }

  val mff = new MessageFrameFormatter(ctx.config.debug)
  implicit val t = mff.transformer

  def connect(connectionId: UUID) = WebSocket.acceptOrResult[RequestMessage, ResponseMessage] { request =>
    implicit val req = Request(request, AnyContentAsEmpty)
    ctx.silhouette.UserAwareRequestHandler { userAwareRequest =>
      Future.successful(HandlerResult(Ok, Some(userAwareRequest.identity)))
    }.map {
      case HandlerResult(r, Some(user)) => Right(ActorFlow.actorRef { out =>
        ConnectionService.props(None, ctx.supervisor, connectionId, user, out, request.remoteAddress)
      })
    }
  }
}
