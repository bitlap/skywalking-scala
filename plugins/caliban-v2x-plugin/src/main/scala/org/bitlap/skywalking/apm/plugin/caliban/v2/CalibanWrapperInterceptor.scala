package org.bitlap.skywalking.apm.plugin.caliban.v2

import java.lang.reflect.Method

import scala.util.*

import caliban.*
import caliban.execution.ExecutionRequest
import caliban.parsing.adt.Document
import caliban.wrappers.Wrapper.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.logging.api.LogManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.caliban.v2.TracingCaliban
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.zcommon.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class CalibanWrapperInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    val span = allArguments(2) match {
      case request: GraphQLRequest =>
        TracingCaliban.beforeGraphQLRequest(request)
      case doc: Document => TracingCaliban.beforeValidate(doc)
      case query: String => TracingCaliban.beforeParseQuery(query)
      case _             => None
    }

    span.foreach(a => objInst.setSkyWalkingDynamicField(a))

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    if objInst.getSkyWalkingDynamicField == null then return ret
    val span = objInst.getSkyWalkingDynamicField.asInstanceOf[AbstractSpan]
    allArguments(2) match {
      case _: GraphQLRequest =>
        val result = ret.asInstanceOf[ZIO[?, Nothing, GraphQLResponse[CalibanError]]]
        TracingCaliban.afterRequest(Option(span), result)
      case _ =>
        val result = ret.asInstanceOf[ZIO[?, ?, ?]]
        result.ensuring(ZIO.attempt {
          AgentUtils.stopAsync(span)
          ContextManager.stopSpan()
        }.ignore)
    }

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit =
    AgentUtils.logError(t)

end CalibanWrapperInterceptor
