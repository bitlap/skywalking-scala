package org.bitlap.skywalking.apm.plugin.ziohttp.v2

import java.lang.reflect.Method

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.common.Utils

import zhttp.http.*
import zhttp.http.Middleware.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/17
 */
final class ZioHttpCollectHttpInterceptor extends InstanceMethodsAroundInterceptor:

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
        e.printStackTrace()
        result
    }
  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = Utils.logError(t)
