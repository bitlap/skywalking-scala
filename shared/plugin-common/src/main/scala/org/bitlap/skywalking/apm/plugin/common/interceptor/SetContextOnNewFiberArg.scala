package org.bitlap.skywalking.apm.plugin.common.interceptor

import java.lang.reflect.Method

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.common.AgentUtils

/** @author
 *    梦境迷离
 *  @version 1.0,2023/6/16
 */
final class SetContextOnNewFiberArg extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    if ContextManager.isActive && allArguments(0).isInstanceOf[EnhancedInstance] then {
      allArguments(0).asInstanceOf[EnhancedInstance].setSkyWalkingDynamicField(ContextManager.capture())
    }

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    ret

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = AgentUtils.logError(t)

  end handleMethodException

end SetContextOnNewFiberArg
