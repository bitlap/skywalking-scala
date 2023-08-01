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
final class CalibanOverallWrapperInterceptor extends InstanceMethodsAroundInterceptor:

  private val LOGGER = LogManager.getLogger(classOf[CalibanWrapperInterceptor])

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit = ()

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    if !ret.isInstanceOf[EffectfulWrapper[?]] then return ret
    try {
      val wrapper = ret.asInstanceOf[EffectfulWrapper[Any]]
      EffectfulWrapper(wrapper.wrapper.map(_ |+| TracingCaliban.traceOverall))
    } catch {
      case e: Throwable =>
        LOGGER.error("Caliban Tracer initialization failed", e)
        ret
    }

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit =
    AgentUtils.logError(t)

end CalibanOverallWrapperInterceptor
