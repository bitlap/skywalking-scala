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
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.caliban.v2.TracingCaliban
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.zcommon.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class CalibanExecutorInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    val span = allArguments(0) match {
      case executionRequest: ExecutionRequest =>
        TracingCaliban.beforeExecutorExecuteRequest(executionRequest)
      case _ => None
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
    val span   = objInst.getSkyWalkingDynamicField.asInstanceOf[AbstractSpan]
    val result = ret.asInstanceOf[ZIO[?, ?, ?]]
    result.ensuring(ZIO.attempt {
      AgentUtils.stopAsync(span)
      ContextManager.stopSpan()
    }.ignore)

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit =
    AgentUtils.logError(t)

end CalibanExecutorInterceptor
