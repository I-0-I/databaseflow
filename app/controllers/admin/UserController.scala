package controllers.admin

import java.util.UUID

import controllers.BaseController
import services.user.{UserSearchService, UserService}
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class UserController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    userService: UserService,
    userSearchService: UserSearchService
) extends BaseController {
  def users = withSession("admin-users") { implicit request =>
    Future.successful(Ok(views.html.admin.user.list(request.identity, ctx.config.debug, userService.getAll)))
  }

  def view(id: UUID) = withSession("admin-user-view") { implicit request =>
    val user = userSearchService.retrieve(id).getOrElse(throw new IllegalStateException(s"Invalid user [$id]."))
    Future.successful(Ok(views.html.admin.user.view(request.identity, ctx.config.debug, user)))
  }

  def edit(id: UUID) = withSession("admin-user-edit") { implicit request =>
    val user = userSearchService.retrieve(id).getOrElse(throw new IllegalStateException(s"Invalid user [$id]."))
    Future.successful(Ok(views.html.admin.user.edit(request.identity, ctx.config.debug, user)))
  }

  def remove(id: UUID) = withSession("admin-user-remove") { implicit request =>
    userService.remove(id)
    Future.successful(Redirect(controllers.admin.routes.UserController.users()))
  }
}
