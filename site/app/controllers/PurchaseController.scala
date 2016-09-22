package controllers

import licensing.{License, LicenseEdition, LicenseGenerator}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import services.notification.NotificationService
import services.payment.StripePaymentService
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
class PurchaseController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi, notification: NotificationService) extends BaseSiteController {
  def pricingPersonal() = act("pricing.personal") { implicit request =>
    Future.successful(Ok(views.html.purchase.purchase(
      "Personal",
      controllers.routes.PurchaseController.purchasePersonalEdition(),
      StripePaymentService.prices(LicenseEdition.Personal)
    )))
  }

  def purchasePersonalEdition() = act("purchase.personal") { implicit request =>
    PurchaseController.stripePurchaseForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(Redirect(controllers.routes.PurchaseController.pricingPersonal()).flashing(
          "error" -> PlayFormUtils.errorsToString(formWithErrors.errors)
        ))
      },
      f => {
        val license = License(name = f.name, email = f.email, edition = LicenseEdition.Personal)
        val licenseContent = LicenseGenerator.saveLicense(license)
        notification.onLicenseCreate(license.id, license.name, license.email, license.edition.title, license.issued, license.version, licenseContent)
        Future.successful(Redirect(controllers.routes.UserLicenseController.licenseView(license.id)))
      }
    )
  }

  def pricingTeam() = act("pricing.team") { implicit request =>
    Future.successful(Ok(views.html.purchase.purchase(
      "Team",
      controllers.routes.PurchaseController.purchaseTeamEdition(),
      StripePaymentService.prices(LicenseEdition.Team)
    )))
  }

  def purchaseTeamEdition() = act("purchase.team.edition") { implicit request =>
    PurchaseController.stripePurchaseForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(Redirect(controllers.routes.PurchaseController.pricingTeam()).flashing(
          "error" -> PlayFormUtils.errorsToString(formWithErrors.errors)
        ))
      },
      f => {
        val license = License(name = f.name, email = f.email, edition = LicenseEdition.Team)
        val licenseContent = LicenseGenerator.saveLicense(license)
        notification.onLicenseCreate(license.id, license.name, license.email, license.edition.title, license.issued, license.version, licenseContent)
        Future.successful(Redirect(controllers.routes.UserLicenseController.licenseView(license.id)))
      }
    )
  }
}
