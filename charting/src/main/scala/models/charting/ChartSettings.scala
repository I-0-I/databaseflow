package models.charting

import scala.scalajs.js

case class ChartSettings(
    t: ChartType = ChartType.Line,
    selects: Map[String, String] = Map.empty,
    flags: Map[String, Boolean] = Map.empty
) {
  def merge(s: ChartSettings) = this.copy(selects = selects ++ s.selects, flags = flags ++ s.flags)

  lazy val asJson = {
    val selectsJson = selects.map(s => s""""${s._1}": "${s._2}"""").mkString(", ")
    val flagsJson = flags.map(s => s""""${s._1}": ${s._2}""").mkString(", ")
    s"""{"t": "${t.id}", "selects": { $selectsJson }, "flags": { $flagsJson } }"""
  }
}

object ChartSettings {
  def fromJs(settings: js.Dynamic) = {
    val t = ChartType.withNameOption(settings.t.toString) match {
      case Some(x) => x
      case None => ChartType.Line
    }
    val selects = settings.selects.asInstanceOf[js.Dictionary[String]].toMap
    val flags = settings.flags.asInstanceOf[js.Dictionary[Boolean]].toMap
    ChartSettings(t = t, selects = selects, flags = flags)
  }
}
