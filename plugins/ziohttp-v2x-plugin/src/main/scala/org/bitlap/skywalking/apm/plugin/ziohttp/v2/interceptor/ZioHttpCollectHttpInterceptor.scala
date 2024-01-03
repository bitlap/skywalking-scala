package org.bitlap.skywalking.apm.plugin.ziohttp.v2.interceptor

import java.lang.reflect.Method

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.apache.skywalking.apm.agent.core.logging.api.LogManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ziohttp.v2.TracingMiddleware

import zhttp.http.*
import zhttp.http.Middleware.*

final class ZioHttpCollectHttpInterceptor extends InstanceMethodsAroundInterceptor:

  private val LOGGER = LogManager.getLogger(classOf[ZioHttpCollectHttpInterceptor])

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit = {}

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: Object
  ): Object =
    if !result.isInstanceOf[Http[?, ?, ?, ?]] then return result
    try {
      val http = result.asInstanceOf[Http[?, ?, Request, Response]]
      http @@ TracingMiddleware.middleware
    } catch {
      case e: Throwable =>
        LOGGER.error("ZIO-HTTP Tracer initialization failed", e)
        result
    }
  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = if ContextManager.isActive then ContextManager.activeSpan.log(t)
