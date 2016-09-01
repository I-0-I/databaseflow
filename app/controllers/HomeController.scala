package controllers

import java.net.URL

import models.user.Language
import play.api.i18n.Messages
import play.api.mvc.Action
import services.connection.ConnectionSettingsService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def home() = withSession("home") { implicit request =>
    val connections = ConnectionSettingsService.getVisible(request.identity)
    Future.successful(Ok(views.html.index(request.identity, ctx.config.debug, connections)))
  }

  def untrail(path: String) = Action.async {
    Future.successful(MovedPermanently(s"/$path"))
  }

  def externalLink(url: String) = withSession("external.link") { implicit request =>
    Future.successful(Redirect(if (url.startsWith("http")) { url } else { "http://" + url }))
  }

  def ping(timestamp: Long) = withSession("ping") { implicit request =>
    Future.successful(Ok(timestamp.toString))
  }

  def robots() = withSession("robots") { implicit request =>
    Future.successful(Ok("User-agent: *\nDisallow: /"))
  }
}
