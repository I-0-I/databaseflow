package models.queries.connection

import java.util.UUID

import models.connection.ConnectionSettings
import models.database.{Row, Statement}
import models.engine.DatabaseEngine
import models.queries.BaseQueries
import models.user.{Permission, Role, User}
import utils.{JdbcUtils, PasswordEncryptUtils}

object ConnectionSettingsQueries extends BaseQueries[ConnectionSettings] {
  override protected val tableName = "connections"
  override protected val columns = Seq(
    "id", "name", "owner", "read", "edit", "description", "engine", "host", "db_name", "extra", "url_override", "username", "password"
  )
  override protected val searchColumns = columns

  val insert = Insert
  def removeById(id: UUID) = RemoveById(Seq(id))
  def getAll(orderBy: String = "\"name\"") = GetAll(orderBy = orderBy)
  def getVisible(owner: User, orderBy: String = "\"name\"") = {
    val readPerms = if (owner.role == Role.Admin) {
      "\"read\" in ('visitor', 'user', 'admin')"
    } else if (owner.role == Role.User) {
      "\"read\" in ('visitor', 'user')"
    } else {
      "\"read\" = 'visitor'"
    }
    val ownerPerms = " or \"owner\" = ?"
    val values = Seq(owner.id)
    GetAll(
      whereClause = Some(readPerms + ownerPerms),
      orderBy = orderBy,
      values = values
    )
  }
  def getById(id: UUID) = GetById(Seq(id))
  val search = Search
  val removeById = RemoveById

  case class Update(cs: ConnectionSettings) extends Statement {
    override val sql = updateSql(Seq(
      "name", "owner", "read", "edit", "description", "engine", "host", "db_name", "extra", "url_override", "username", "password"
    ))
    override val values = Seq(
      cs.name, cs.owner, cs.read, cs.edit, cs.description, cs.engine.id, cs.host, cs.dbName, cs.extra, cs.urlOverride, cs.username, cs.password, cs.id
    )
  }

  override protected def fromRow(row: Row) = ConnectionSettings(
    id = row.as[UUID]("id"),
    name = row.as[String]("name"),
    owner = row.as[UUID]("owner"),
    read = Permission.withName(row.as[String]("read")),
    edit = Permission.withName(row.as[String]("edit")),
    description = JdbcUtils.extractString(row.as[Any]("description")),
    engine = DatabaseEngine.withName(row.as[String]("engine")),
    host = row.asOpt[Any]("host").map(JdbcUtils.extractString),
    dbName = row.asOpt[Any]("db_name").map(JdbcUtils.extractString),
    extra = row.asOpt[Any]("extra").map(JdbcUtils.extractString),
    urlOverride = row.asOpt[Any]("url_override").map(JdbcUtils.extractString),
    username = row.as[String]("username"),
    password = PasswordEncryptUtils.decrypt(JdbcUtils.extractString(row.as[Any]("password")))
  )

  override protected def toDataSeq(q: ConnectionSettings) = Seq[Any](
    q.id, q.name, q.owner, q.read.toString, q.edit.toString, q.description, q.engine.toString,
    q.host, q.dbName, q.extra, q.urlOverride, q.username, PasswordEncryptUtils.encrypt(q.password)
  )
}
