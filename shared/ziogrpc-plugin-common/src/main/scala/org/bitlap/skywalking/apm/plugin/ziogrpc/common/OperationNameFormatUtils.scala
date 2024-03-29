package org.bitlap.skywalking.apm.plugin.ziogrpc.common

import io.grpc.MethodDescriptor

object OperationNameFormatUtils:

  def formatOperationName(methodDescriptor: MethodDescriptor[?, ?]): String = {
    val fullMethodName = methodDescriptor.getFullMethodName
    formatServiceName(fullMethodName) + "." + formatMethodName(fullMethodName)
  }

  private def formatServiceName(requestMethodName: String) = {
    val splitIndex = requestMethodName.lastIndexOf("/")
    requestMethodName.substring(0, splitIndex)
  }

  private def formatMethodName(requestMethodName: String) = {
    val splitIndex = requestMethodName.lastIndexOf("/")
    var methodName = requestMethodName.substring(splitIndex + 1)
    methodName = methodName.substring(0, 1).toLowerCase + methodName.substring(1)
    methodName
  }
