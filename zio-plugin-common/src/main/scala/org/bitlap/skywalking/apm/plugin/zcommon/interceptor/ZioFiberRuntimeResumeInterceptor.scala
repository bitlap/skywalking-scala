package org.bitlap.skywalking.apm.plugin.zcommon.interceptor

import java.lang.reflect.Method

import zio.internal.FiberRuntime

import org.apache.skywalking.apm.agent.core.context.{ ContextManager, ContextSnapshot }
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.AgentUtils
import org.bitlap.skywalking.apm.plugin.zcommon.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/6/16
 */
final class ZioFiberRuntimeResumeInterceptor extends InstanceMethodsAroundInterceptor {

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
  ): Object = {
    if objInst.getSkyWalkingDynamicField != null && ContextManager.isActive then {
      val contextSnapshot = objInst.getSkyWalkingDynamicField.asInstanceOf[ContextSnapshot]
      ContextManager.continued(contextSnapshot)
    }
    ret
  }

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = AgentUtils.logError(t)

  end handleMethodException

}
