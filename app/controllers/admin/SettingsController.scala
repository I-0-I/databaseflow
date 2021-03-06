package controllers.admin

import controllers.BaseController
import models.settings.SettingKey
import services.settings.SettingsService
import util.ApplicationContext
import util.web.FormUtils

import scala.concurrent.Future

@javax.inject.Singleton
class SettingsController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def settings = withAdminSession("admin-settings") { implicit request =>
    Future.successful(Ok(views.html.admin.settings(request.identity)))
  }

  def saveSettings = withAdminSession("admin-settings-save") { implicit request =>
    val form = FormUtils.getForm(request)
    form.foreach { x =>
      SettingKey.withNameOption(x._1) match {
        case Some(settingKey) => SettingsService.set(settingKey, x._2)
        case None => log.warn(messagesApi("admin.settings.invalid", x._1)(request.lang))
      }
    }
    Future.successful(Redirect(controllers.routes.HomeController.home()))
  }
}
