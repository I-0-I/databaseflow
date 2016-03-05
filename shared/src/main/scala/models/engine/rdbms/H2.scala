/* Generated Code */
// scalastyle:off
package models.engine.rdbms

import models.engine.DatabaseEngine

object H2 extends DatabaseEngine(
  id = "h2",
  name = "H2",
  driverClass = "org.h2.Driver",
  exampleUrl = "jdbc:h2:~/database.h2db",

  builtInFunctions = Seq(
    "abs",
    "acos",
    "ascii",
    "asin",
    "atan",
    "atan2",
    "avg",
    "bit_length",
    "bitand",
    "bitor",
    "bitxor",
    "cast",
    "ceiling",
    "char",
    "coalesce",
    "compress",
    "concat",
    "cos",
    "cot",
    "count",
    "curdate",
    "current_date",
    "current_time",
    "current_timestamp",
    "curtime",
    "curtimestamp",
    "database",
    "datediff",
    "day",
    "dayname",
    "dayofmonth",
    "dayofweek",
    "dayofyear",
    "decrypt",
    "degrees",
    "difference",
    "encrypt",
    "exp",
    "expand",
    "extract",
    "floor",
    "hash",
    "hextoraw",
    "hour",
    "insert",
    "lcase",
    "left",
    "length",
    "locate",
    "log",
    "log10",
    "lower",
    "ltrim",
    "max",
    "min",
    "minute",
    "mod",
    "month",
    "monthname",
    "now",
    "nullif",
    "octet_length",
    "pi",
    "position",
    "power",
    "quarter",
    "radians",
    "rand",
    "rawtohex",
    "repeat",
    "replace",
    "right",
    "round",
    "roundmagic",
    "rtrim",
    "second",
    "sign",
    "sin",
    "soundex",
    "space",
    "sqrt",
    "str",
    "stringdecode",
    "stringencode",
    "stringtoutf8",
    "substring",
    "sum",
    "tan",
    "trim",
    "truncate",
    "ucase",
    "upper",
    "user",
    "utf8tostring",
    "week",
    "year"
  ),

  columnTypes = Seq(
    "bigint",
    "binary",
    "boolean",
    "blob",
    "boolean",
    "char($l)",
    "clob",
    "date",
    "decimal($p,$s)",
    "double",
    "float",
    "integer",
    "nvarchar($l)",
    "longvarbinary",
    "longvarchar",
    "nchar($l)",
    "nclob",
    "decimal($p,$s)",
    "nvarchar($l)",
    "real",
    "smallint",
    "time",
    "timestamp",
    "tinyint",
    "binary($l)",
    "varchar($l)"
  )
) {
  override val varchar = "varchar"
}
// scalastyle:on
