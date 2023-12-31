package org.bitlap.skywalking.apm.plugin.ziocache.interceptor

import java.lang.reflect.Method

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.common.AgentUtils
import org.bitlap.skywalking.apm.plugin.ziocache.ZioCacheOperationConvertor

final class ZioCacheInterceptor extends InstanceMethodsAroundInterceptor {

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Any],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit = {
    val methodName = method.getName
    val span       = ContextManager.createLocalSpan("ZioCache/" + method.getName)
    if allArguments != null && allArguments.length > 0 && allArguments(0).isInstanceOf[String] then
      Tags.CACHE_KEY.set(span, allArguments(0).toString)
    Tags.CACHE_TYPE.set(span, "ZioCache")
    Tags.CACHE_CMD.set(span, methodName)
    ZioCacheOperationConvertor.parseOperation(methodName).foreach(op => Tags.CACHE_OP.set(span, op))
    SpanLayer.asCache(span)
  }

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Any],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object = {
    AgentUtils.stopIfActive()
    ret
  }

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Any],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit =
    if ContextManager.isActive then ContextManager.activeSpan.log(t)
}
