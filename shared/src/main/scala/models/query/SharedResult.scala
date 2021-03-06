package models.query

import java.util.UUID

import models.user.Permission
import util.JsonSerializers._

object SharedResult {
  implicit val jsonEncoder: Encoder[SharedResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[SharedResult] = deriveDecoder
}

case class SharedResult(
    id: UUID = UUID.randomUUID,
    title: String = "",
    description: Option[String] = None,
    owner: UUID,
    viewableBy: Permission = Permission.User,
    connectionId: UUID,
    sql: String,
    source: QueryResult.Source,
    chart: Option[String] = None,
    lastAccessed: Long = System.currentTimeMillis,
    created: Long = System.currentTimeMillis
)
