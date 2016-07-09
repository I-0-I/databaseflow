package controllers

import licensing.{License, LicenseEdition, LicenseGenerator}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.notification.NotificationService
import utils.web.PlayFormUtils

import scala.concurrent.Future

object PurchaseController {
  case class StripePurchaseForm(name: String, email: String, stripeToken: String, stripeTokenType: String)

  val stripePurchaseForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> email,
      "stripeToken" -> nonEmptyText,
      "stripeTokenType" -> nonEmptyText
    )(StripePurchaseForm.apply)(StripePurchaseForm.unapply)
  )
}

@javax.inject.Singleton
class PurchaseController @javax.inject.Inject() (
    implicit
    override val messagesApi: MessagesApi, notificationService: NotificationService
) extends Controller with I18nSupport {
  def pricing() = Action.async { implicit request =>
    Future.successful(Ok(views.html.purchase.purchase()))
  }

  def purchaseTeamEdition() = Action.async { implicit request =>
    PurchaseController.stripePurchaseForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(Redirect(controllers.routes.PurchaseController.pricing()).flashing("error" -> PlayFormUtils.errorsToString(formWithErrors.errors)))
      },
      f => {
        val license = License(name = f.name, email = f.email, edition = LicenseEdition.Team)

        val licenseContent = LicenseGenerator.saveLicense(license)
        notificationService.onLicenseCreate(license.id, license.name, license.email, license.edition.title, license.issued, license.version, licenseContent)

        Future.successful(Redirect(controllers.routes.UserLicenseController.licenseView(license.id)))
      }
    )
  }
}