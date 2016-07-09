package controllers.admin

import java.util.UUID

import controllers.BaseController
import models.user.Role
import services.user.{UserSearchService, UserService}
import utils.ApplicationContext
import utils.web.FormUtils

import scala.concurrent.Future

@javax.inject.Singleton
class UserEditController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    userService: UserService,
    userSearchService: UserSearchService
) extends BaseController {
  def users = withAdminSession("admin-users") { implicit request =>
    Future.successful(Ok(views.html.admin.user.list(request.identity, ctx.config.debug, userService.getAll)))
  }

  def view(id: UUID) = withAdminSession("admin-user-view") { implicit request =>
    val user = userSearchService.retrieve(id).getOrElse(throw new IllegalStateException(s"Invalid user [$id]."))
    Future.successful(Ok(views.html.admin.user.view(request.identity, ctx.config.debug, user)))
  }

  def edit(id: UUID) = withAdminSession("admin-user-edit") { implicit request =>
    val user = userSearchService.retrieve(id).getOrElse(throw new IllegalStateException(s"Invalid user [$id]."))
    Future.successful(Ok(views.html.admin.user.edit(request.identity, ctx.config.debug, user)))
  }

  def save(id: UUID) = withAdminSession("admin-user-save") { implicit request =>
    val form = FormUtils.getForm(request)
    val user = userSearchService.retrieve(id).getOrElse(throw new IllegalStateException(s"Invalid user [$id]."))
    val isSelf = request.identity.exists(_.id == id)

    val newUsername = form("username")
    val newEmail = form("email")
    val newPassword = form.get("password") match {
      case Some(x) if x != "original" => Some(x)
      case _ => None
    }
    val role = form.get("role") match {
      case Some("admin") => Role.Admin
      case Some("user") => Role.User
      case Some("visitor") => Role.Visitor
      case x => throw new IllegalStateException(s"Missing role: [$x].")
    }

    if (newUsername.isEmpty) {
      Future.successful(Redirect(controllers.admin.routes.UserEditController.edit(id)).flashing("error" -> "Username was empty."))
    } else if (newEmail.isEmpty) {
      Future.successful(Redirect(controllers.admin.routes.UserEditController.edit(id)).flashing("error" -> "Email Address was empty."))
    } else if (isSelf && (role != Role.Admin) && user.role == Role.Admin) {
      Future.successful(Redirect(controllers.admin.routes.UserEditController.edit(id)).flashing("error" -> "You cannot remove your own admin role."))
    } else {
      userService.update(id, newUsername, newEmail, newPassword, role, user.profile.providerKey)
      Future.successful(Redirect(controllers.admin.routes.UserEditController.view(id)))
    }
  }

  def remove(id: UUID) = withAdminSession("admin-user-remove") { implicit request =>
    userService.remove(id)
    Future.successful(Redirect(controllers.admin.routes.UserEditController.users()))
  }
}