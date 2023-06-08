package org.bitlap.skywalking.apm.plugin.zio

import java.lang.reflect.Method

import zio.internal.FiberRuntime

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.InterceptorDSL

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/17
 */
final class ZioBlockingSubmitInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit = {
    if allArguments == null || allArguments.length < 1 then {
      return
    }
    val span: AbstractSpan = ContextManager.createLocalSpan(blockingOperationName)
    val fiberRuntime       = allArguments(0)
    val storedField        = objInst.getSkyWalkingDynamicField
    fiberRuntime match {
      case fiber: FiberRuntime[?, ?] =>
        ZioTags.setZioTags(span, fiber.id)
      case _ =>
    }
    if storedField != null then {
      val contextSnapshot = storedField.asInstanceOf[ContextSnapshot]
      InterceptorDSL.continuedSnapshot_(contextSnapshot)
    }
    span.setComponent(ComponentsDefine.JDK_THREADING)
  }

  private def blockingOperationName = "ZioBlockingSubmitWrapper/" + Thread.currentThread.getName

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    ContextManager.stopSpan()
    ret

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit =
    if ContextManager.isActive then ContextManager.activeSpan.log(t)
