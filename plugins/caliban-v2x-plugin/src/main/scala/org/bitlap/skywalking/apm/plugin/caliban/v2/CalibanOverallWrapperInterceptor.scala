package org.bitlap.skywalking.apm.plugin.caliban.v2

import java.lang.reflect.Method

import caliban.*
import caliban.wrappers.Wrapper.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.logging.api.LogManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

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
    if ContextManager.isActive then ContextManager.activeSpan.log(t)

end CalibanOverallWrapperInterceptor
