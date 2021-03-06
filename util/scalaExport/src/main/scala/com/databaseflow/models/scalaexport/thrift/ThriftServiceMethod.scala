package com.databaseflow.models.scalaexport.thrift

import com.facebook.swift.parser.model.ThriftMethod
import com.databaseflow.services.scalaexport.ExportHelper
import com.databaseflow.services.scalaexport.thrift.file.ThriftFileHelper

import scala.collection.JavaConverters._

case class ThriftServiceMethod(f: ThriftMethod) {
  val name = ExportHelper.toIdentifier(f.getName)
  val arguments = f.getArguments.asScala.map(ThriftStructField.apply)
  val returnValue = f.getReturnType

  def sig(metadata: ThriftMetadata) = {
    val retVal = ThriftFileHelper.columnTypeFor(returnValue, metadata)._1
    val argVals = arguments.map(arg => arg.name + ": " + ThriftFileHelper.columnTypeFor(arg.t, metadata)._1)
    s"$name(${argVals.mkString(", ")}): $retVal"
  }
}
