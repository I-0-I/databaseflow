import Dependencies._
import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.SbtScalariform.{ ScalariformKeys, scalariformSettings }
import net.virtualvoid.sbt.graph.DependencyGraphSettings.graphSettings
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import webscalajs.ScalaJSWeb
import sbt.Keys._
import sbt._

object Shared {
  val projectId = "databaseflow"
  val projectName = "Database Flow"

  lazy val commonSettings = Seq(
    version := Shared.Versions.app,
    scalaVersion := Shared.Versions.scala,

    scalacOptions ++= Seq(
      "-encoding", "UTF-8", "-feature", "-deprecation", "-unchecked", "-target:jvm-1.8", "–Xcheck-null", "-Xfatal-warnings", "-Xlint",
      "-Ywarn-adapted-args", "-Ywarn-dead-code", "-Ywarn-inaccessible", "-Ywarn-nullary-override", "-Ywarn-numeric-widen",
      "-Ybackend:GenBCode", "-Ydelambdafy:method"
    ),
    scalacOptions in Test ++= Seq("-Yrangepos"),

    publishMavenStyle := false,

    // Prevent Scaladoc
    doc in Compile <<= target.map(_ / "none"),
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,

    // Code Quality
    scapegoatVersion := Utils.scapegoatVersion,
    scapegoatDisabledInspections := Seq("MethodNames", "MethodReturningAny", "DuplicateImport"),
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
  ) ++ graphSettings ++ scalariformSettings


  object Versions {
    val app = "1.0.0"
    val scala = "2.11.8"
  }

  def withProjects(p: Project, includes: Seq[Project]) = includes.foldLeft(p) { (proj, inc) =>
    proj.aggregate(inc).dependsOn(inc)
  }

  lazy val sharedJs = (crossProject.crossType(CrossType.Pure) in file("shared")).settings(commonSettings: _*).settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % Serialization.version,
      "com.beachape" %%% "enumeratum-upickle" % Utils.enumeratumVersion
    )
  ).enablePlugins(ScalaJSWeb).settings(scalaJSStage in Global := FastOptStage).js

  lazy val sharedJvm = (project in file("shared")).settings(commonSettings: _*).settings(
    libraryDependencies ++= Seq(Serialization.uPickle, Utils.enumeratum)
  )
}
