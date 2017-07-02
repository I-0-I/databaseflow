package models.schema

import enumeratum._

sealed abstract class ColumnType(val key: String, val asScala: String, val isNumeric: Boolean = false) extends EnumEntry {
  override def toString = key
}

object ColumnType extends Enum[ColumnType] {
  case object StringType extends ColumnType("string", "String")
  case object BigDecimalType extends ColumnType("decimal", "BigDecimal", isNumeric = true)
  case object BooleanType extends ColumnType("boolean", "Boolean")
  case object ByteType extends ColumnType("byte", "Byte")
  case object ShortType extends ColumnType("short", "Short", isNumeric = true)
  case object IntegerType extends ColumnType("integer", "Int", isNumeric = true)
  case object LongType extends ColumnType("long", "Long", isNumeric = true)
  case object FloatType extends ColumnType("float", "Float", isNumeric = true)
  case object DoubleType extends ColumnType("double", "Double", isNumeric = true)
  case object ByteArrayType extends ColumnType("bytearray", "Array[Byte]")
  case object DateType extends ColumnType("date", "LocalDate")
  case object TimeType extends ColumnType("time", "LocalTime")
  case object TimestampType extends ColumnType("timestamp", "Timestamp")

  case object RefType extends ColumnType("ref", "String")
  case object XmlType extends ColumnType("xml", "String")
  case object UuidType extends ColumnType("uuid", "UUID")

  case object ObjectType extends ColumnType("object", "String")
  case object StructType extends ColumnType("struct", "String")
  case object ArrayType extends ColumnType("array", "Array[Any]")

  case object UnknownType extends ColumnType("unknown", "Any")

  override val values = findValues
}
