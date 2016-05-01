package models.database

import java.sql.{ Connection, PreparedStatement, Types }

import utils.{ Logging, NullUtils }

import scala.annotation.tailrec

trait Queryable extends Logging {
  @tailrec
  @SuppressWarnings(Array("AsInstanceOf"))
  private[this] def prepare(stmt: PreparedStatement, values: Seq[Any], index: Int = 1) {
    if (values.nonEmpty) {
      values.headOption.getOrElse(throw new IllegalStateException()) match {
        case v if NullUtils.isNull(v) => stmt.setNull(index, Types.NULL)

        case Some(x) => stmt.setObject(index, Conversions.convert(x.asInstanceOf[AnyRef]))
        case None => stmt.setNull(index, Types.NULL)

        case v => stmt.setObject(index, Conversions.convert(v.asInstanceOf[AnyRef]))
      }
      prepare(stmt, values.tail, index + 1)
    }
  }

  def apply[A](connection: Connection, query: RawQuery[A]): A = {
    log.debug(s"${query.sql} with ${query.values.mkString("(", ", ", ")")}")
    val stmt = connection.prepareStatement(query.sql)
    try {
      prepare(stmt, query.values)
      val results = stmt.executeQuery()
      try {
        query.handle(results)
      } finally {
        results.close()
      }
    } finally {
      stmt.close()
    }
  }

  def executeUpdate(connection: Connection, statement: Statement): Int = {
    log.debug(s"${statement.sql} with ${statement.values.mkString("(", ", ", ")")}")
    val stmt = connection.prepareStatement(statement.sql)
    try {
      prepare(stmt, statement.values)
      stmt.executeUpdate()
    } finally {
      stmt.close()
    }
  }

  def executeUnknown[A](connection: Connection, query: Query[A]): Either[A, Int] = {
    log.debug(s"${query.sql} with ${query.values.mkString("(", ", ", ")")}")
    val stmt = connection.prepareStatement(query.sql)
    try {
      prepare(stmt, query.values)
      val isResultset = stmt.execute()
      if (isResultset) {
        Left(query.handle(stmt.getResultSet))
      } else {
        Right(stmt.getUpdateCount)
      }
    } finally {
      stmt.close()
    }
  }

  def executeUpdate(statement: Statement): Int
  def apply[A](query: RawQuery[A]): A
  def transaction[A](f: Transaction => A): A

  def query[A](query: RawQuery[A]): A = apply(query)
}
