package models.connection

import java.util.UUID

import models.engine.DatabaseEngine
import models.engine.DatabaseEngine.PostgreSQL

object ConnectionSettings {
  val defaultEngine = PostgreSQL
  val empty = ConnectionSettings()
}

case class ConnectionSettings(
    id: UUID = UUID.randomUUID,
    name: String = "",
    owner: Option[UUID] = None,
    read: String = "visitor",
    edit: String = "private",
    description: String = "",
    engine: DatabaseEngine = ConnectionSettings.defaultEngine,
    host: Option[String] = None,
    port: Option[Int] = None,
    dbName: Option[String] = None,
    extra: Option[String] = None,
    urlOverride: Option[String] = None,
    username: String = "",
    password: String = ""
) {
  val url = urlOverride.getOrElse(engine.url(host, port, dbName, extra))
}
