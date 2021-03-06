package controllers.admin

import better.files._
import controllers.BaseController
import models.schema.Schema
import services.connection.ConnectionSettingsService
import com.databaseflow.models.scalaexport.db.config.{ExportConfiguration, ExportConfigurationDefault}
import com.databaseflow.services.scalaexport.{ExportFiles, ExportHelper}
import com.databaseflow.services.scalaexport.db.ScalaExportService
import com.databaseflow.services.scalaexport.thrift.ThriftParseService
import services.schema.SchemaService
import util.ApplicationContext
import util.FutureUtils.defaultContext
import util.web.FormUtils
import util.JsonSerializers._

import scala.concurrent.Future

@javax.inject.Singleton
class ScalaExportController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def exportThrift(conn: String, filename: Option[String]) = withAdminSession("export.form") { implicit request =>
    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => filename match {
        case Some(fn) => SchemaService.getSchemaWithDetails(None, cs).map { schema =>
          val loc = cs.projectLocation.getOrElse(throw new IllegalStateException("Please configure a project location before exporting."))
          val config = getConfig(loc, schema)
          ExportFiles.prepareRoot()
          val flags = Set("rest", "graphql", "extras")
          val result = ThriftParseService.exportThrift(
            filename = fn, persist = true, projectLocation = config.projectLocation, flags = flags, configLocation = "./tmp/thrift"
          )
          Ok(views.html.admin.scalaExport.exportThrift(request.identity, fn.substring(fn.lastIndexOf('/') + 1), result._1, result._2))
        }
        case None => Future.successful(Ok(views.html.admin.scalaExport.thriftForm(request.identity, cs, File("./tmp/thrift"))))
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  def exportForm(conn: String) = withAdminSession("export.form") { implicit request =>
    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(None, cs).map { schema =>
        val loc = cs.projectLocation.getOrElse(throw new IllegalStateException("Please configure a project location before exporting."))
        Ok(views.html.admin.scalaExport.schemaForm(request.identity, cs, getConfig(loc, schema), schema))
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  def export(conn: String) = withAdminSession("export.project") { implicit request =>
    val form = FormUtils.getForm(request)

    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(None, cs).flatMap { schema =>
        val corePackage = form.get("project.corePackage").filter(_.nonEmpty)
        val pkgPrefix = corePackage.getOrElse("").split('.').filter(_.nonEmpty).toList
        val enums = schema.enums.map { e =>
          ScalaExportHelper.enumFor(e, form.filter(_._1.startsWith("enum." + e.key)).map(x => x._1.stripPrefix("enum." + e.key + ".") -> x._2), pkgPrefix)
        }
        val config = ExportConfiguration(
          key = ExportHelper.toIdentifier(schema.id),
          projectId = form("project.id"),
          projectTitle = form("project.title"),
          flags = form.getOrElse("project.flags", "").split(',').map(_.trim).filterNot(_.isEmpty).toSet,
          enums = enums,
          models = schema.tables.map { t =>
            val prefix = s"model.${t.name}."
            ScalaExportHelper.modelForTable(schema, t, form.filter(_._1.startsWith(prefix)).map(x => x._1.stripPrefix(prefix) -> x._2), enums, pkgPrefix)
          } ++ schema.views.map { v =>
            val prefix = s"model.${v.name}."
            ScalaExportHelper.modelForView(schema, v, form.filter(_._1.startsWith(prefix)).map(x => x._1.stripPrefix(prefix) -> x._2), enums, pkgPrefix)
          },
          source = form("project.source"),
          projectLocation = form.get("project.location").filter(_.nonEmpty),
          providedPackage = form.get("project.providedPackage").filter(_.nonEmpty),
          corePackage = corePackage,
          coreLocation = form.get("project.coreLocation").filter(_.nonEmpty),
          modelLocationOverride = form.get("model.location").filter(_.nonEmpty),
          thriftLocationOverride = form.get("thrift.location").filter(_.nonEmpty)
        )

        val x = config.asJson.spaces2
        val loc = cs.projectLocation.getOrElse(throw new IllegalStateException("Please configure a project location before exporting."))
        (loc.toFile / "databaseflow.json").overwrite(x)

        ScalaExportService(config).export(persist = true).map { result =>
          Ok(views.html.admin.scalaExport.export(result.er, result.files, result.out))
        }
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  private[this] def getConfig(projectLocation: String, schema: Schema): ExportConfiguration = {
    val f = projectLocation.toFile / "databaseflow.json"
    if (f.exists) {
      ScalaExportHelper.merge(schema, decodeJson[ExportConfiguration](f.contentAsString) match {
        case Right(x) => x
        case Left(x) => throw x
      })
    } else {
      ExportConfigurationDefault.forSchema(schema)
    }
  }
}
