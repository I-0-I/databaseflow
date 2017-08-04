import Dependencies.Utils
import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbtcrossproject.CrossPlugin.autoImport._
import scalajscrossproject.ScalaJSCrossPlugin.autoImport.{toScalaJSGroupID => _, _}

import webscalajs.ScalaJSWeb
import sbt.Keys._
import sbt._

object Client {
  private[this] val clientSettings = Shared.commonSettings ++ Seq(
    libraryDependencies ++= Seq(
      "be.doeraene" %%% "scalajs-jquery" % Dependencies.ScalaJS.jQueryVersion,
      "com.lihaoyi" %%% "scalatags" % Dependencies.ScalaJS.scalaTagsVersion
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    scapegoatIgnoredFiles := Seq(".*/Messages.scala", ".*/JsonUtils.scala", ".*/JsonSerializers.scala")
  )

  lazy val client = (project in file("client"))
    .settings(clientSettings: _*)
    .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
    .dependsOn(Shared.sharedJs)

  private[this] val chartingSettings = Shared.commonSettings ++ Seq(
    libraryDependencies ++= Seq(
      "be.doeraene" %%% "scalajs-jquery" % Dependencies.ScalaJS.jQueryVersion,
      "com.lihaoyi" %%% "scalatags" % Dependencies.ScalaJS.scalaTagsVersion,
      "com.beachape" %%% "enumeratum" % Utils.enumeratumVersion
    ),
    scapegoatIgnoredFiles := Seq(".*/JsonUtils.scala", ".*/JsonSerializers.scala")
  )

  lazy val charting = (project in file("charting"))
    .settings(chartingSettings: _*)
    .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
}
