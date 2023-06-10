package org.bitlap.skywalking.apm.plugin.zio

import java.lang.reflect.Method

import zio.internal.FiberRuntime

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer
import org.apache.skywalking.apm.agent.core.logging.api.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.InterceptorDSL

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioFiberRuntimeInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    val fiberRuntime = objInst.asInstanceOf[FiberRuntime[?, ?]]
    val span         = ContextManager.createLocalSpan(generateFiberOperationName(method))
    span.setComponent(ComponentsDefine.JDK_THREADING)
    Utils.setZioTags(span, fiberRuntime.id, objInst)
    Utils.continuedSnapshot(objInst)

  private def generateFiberOperationName(method: Method) =
    s"ZioFiberWrapper/${method.getName}/${Thread.currentThread.getName}"

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    if ContextManager.isActive then ContextManager.stopSpan()
    ret

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit =
    if ContextManager.isActive then ContextManager.activeSpan.log(t)
  end handleMethodException

end ZioFiberRuntimeInterceptor
