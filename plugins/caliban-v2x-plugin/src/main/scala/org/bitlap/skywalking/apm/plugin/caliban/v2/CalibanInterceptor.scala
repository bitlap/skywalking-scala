package org.bitlap.skywalking.apm.plugin.caliban.v2

import java.lang.reflect.Method

import scala.util.*

import caliban.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.zcommon.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
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
    AgentUtils.logError(t)

end CalibanInterceptor
