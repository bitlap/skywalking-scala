package org.bitlap.skywalking.apm.plugin.caliban.v2

import java.lang.reflect.Method

import caliban.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

final class CalibanInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit = {
    val graphQLRequest = allArguments(0).asInstanceOf[GraphQLRequest]
    val span           = TracingCaliban.beforeRequest(graphQLRequest)
    span.foreach(a => objInst.setSkyWalkingDynamicField(a))
  }

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    val span = objInst.getSkyWalkingDynamicField.asInstanceOf[AbstractSpan]
    if span == null then return ret
    val result = ret.asInstanceOf[URIO[?, GraphQLResponse[CalibanError]]]
    TracingCaliban.afterRequest(Some(span), result)

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit =
    if ContextManager.isActive then ContextManager.activeSpan.log(t)

end CalibanInterceptor
