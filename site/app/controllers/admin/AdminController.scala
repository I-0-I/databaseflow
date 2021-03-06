package controllers.admin

import controllers.BaseSiteController
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.twirl.api.Html
import services.logging.LogService

import scala.concurrent.Future

@javax.inject.Singleton
class AdminController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  private[this] val secretKey = Option(System.getenv("SECRET_KEY")).getOrElse("np")

  def index() = withAdminSession("admin-index") { (username, request) =>
    implicit val req = request
    Future.successful(Ok(views.html.admin.index(username)))
  }

  def enable(id: String) = Action.async { implicit request =>
    if (request.getQueryString("p").contains(secretKey)) {
      Future.successful(Redirect(controllers.admin.routes.AdminController.index()).withSession("admin-role" -> id))
    } else {
      Future.successful(Redirect(controllers.routes.SiteController.index()).withNewSession)
    }
  }

  def sandbox() = withAdminSession("admin-sandbox") { (_, request) =>
    implicit val req = request
    Future.successful(Ok(Html("Ok!")))
  }

  def toggleLogging() = withAdminSession("toggle-logging") { (_, _) =>
    LogService.enabled = !LogService.enabled
    Future.successful(Ok(s"OK: ${LogService.enabled}"))
  }
}
