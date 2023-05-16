package org.bitlap.skywalking.apm.plugin.zio

import java.lang.reflect.Method

import zio.internal.FiberRuntime

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioFiberRuntimeRunInterceptor extends InstanceMethodsAroundInterceptor:

  private def generateOperationName(objInst: EnhancedInstance, method: Method, id: Int) =
    s"ZIO/${objInst.getClass.getSimpleName}/${method.getName}#$id"

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    result: MethodInterceptResult
  ): Unit =
    if (objInst == null) {
      return
    }

    val fiberRuntime = objInst.asInstanceOf[FiberRuntime[Any, Any]]
    val span         = ContextManager.createLocalSpan(generateOperationName(objInst, method, fiberRuntime.id.id))
    ZioTag.setZioTags(span, fiberRuntime.id)
    val storedField = objInst.getSkyWalkingDynamicField
    if (storedField != null) {
      val contextSnapshot = storedField.asInstanceOf[ContextSnapshot]
      ContextManager.continued(contextSnapshot)
    }

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    ret: Object
  ): Object =
    ContextManager.stopSpan()
    ret

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    t: Throwable
  ): Unit =
    if (ContextManager.isActive) ContextManager.activeSpan.log(t)
