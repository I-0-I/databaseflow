package services.sandbox

import enumeratum._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.twirl.api.Html
import utils.{ApplicationContext, Logging}

import scala.concurrent.Future

object SandboxTask extends Enum[SandboxTask] {
  case object Metrics extends SandboxTask("metrics", "Metrics Dump", "Lists all of the metrics for the running server.") {
    override def run(ctx: ApplicationContext) = {
      val url = "http://localhost:4261/metrics?pretty=true"
      val call = ctx.ws.url(url).withHeaders("Accept" -> "application/json")
      call.get().map { json =>
        views.html.admin.sandbox.metrics(json.body)
      }
    }
  }

  case object ChartSandbox extends SandboxTask("charts", "Charting Sandbox", "Just toying with some graphing libraries.") {
    override def run(ctx: ApplicationContext) = {
      Future.successful(views.html.admin.sandbox.chart(ctx.config.debug))
    }
  }

  case object Testbed extends SandboxTask("testbed", "Testbed", "A simple sandbox for messin' around.") {
    override def run(ctx: ApplicationContext) = {
      Future.successful(Html("Hello!"))
    }
  }

  override def values = findValues
}

sealed abstract class SandboxTask(val id: String, val name: String, val description: String) extends EnumEntry with Logging {
  def run(ctx: ApplicationContext): Future[Html]
  override def toString = id
}
