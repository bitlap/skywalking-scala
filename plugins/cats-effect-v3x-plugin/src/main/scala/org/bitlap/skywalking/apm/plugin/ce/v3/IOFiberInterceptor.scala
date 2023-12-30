package org.bitlap.skywalking.apm.plugin.ce.v3

import java.lang.reflect.Method

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.AgentUtils
import org.bitlap.skywalking.apm.plugin.common.CustomTag

final class IOFiberInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    if objInst.getSkyWalkingDynamicField == null then return
    val currentSpan = ContextManager.createLocalSpan(
      "CERunnableWrapper/" + Thread.currentThread().getName
    )
    currentSpan.setComponent(ComponentsDefine.JDK_THREADING)
    AgentUtils.continuedSnapshot(objInst)

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    if objInst.getSkyWalkingDynamicField == null then return ret
    AgentUtils.stopIfActive()
    ret

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = if ContextManager.isActive then ContextManager.activeSpan.log(t)

  end handleMethodException

end IOFiberInterceptor
