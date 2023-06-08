package org.bitlap.skywalking.apm.plugin.caliban

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
    if graphQLRequest == null || graphQLRequest.query.isEmpty then return
    val span = TracingGraphQL.beforeRequest(graphQLRequest)
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

    result.onExit(cleanup =>
      cleanup match
        case Exit.Success(value) =>
          ZIO.attempt {
            span.asyncFinish()
            if value.errors.nonEmpty then {
              val ex: Option[CalibanError] = value.errors.headOption
              span.log(ex.getOrElse(CalibanError.ExecutionError("Effect failure")))
            }
            ContextManager.stopSpan()
          }.ignore
        case Exit.Failure(cause) =>
          ZIO.attempt {
            ContextManager.stopSpan()
            if ContextManager.isActive then ContextManager.activeSpan.log(cause.squash)
          }.ignore
    )

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit =
    if ContextManager.isActive then ContextManager.activeSpan.log(t)

end CalibanInterceptor
