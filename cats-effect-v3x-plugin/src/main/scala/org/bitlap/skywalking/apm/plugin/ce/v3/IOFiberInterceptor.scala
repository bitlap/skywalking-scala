package org.bitlap.skywalking.apm.plugin.ce.v3

import java.lang.reflect.Method

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.AgentUtils
import org.bitlap.skywalking.apm.plugin.common.CustomTag

/** @author
 *    梦境迷离
 *  @version 1.0,2023/6/15
 */
class IOFiberInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    val span = ContextManager.createLocalSpan(AgentUtils.generateFiberOperationName)
    span.setComponent(ComponentsDefine.JDK_THREADING)
    CustomTag.FiberIOType.tag.set(span, "cats-effect")
    if ContextManager.isActive then AgentUtils.continuedSnapshot(objInst)

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
  ): Unit = AgentUtils.logError(t)

  end handleMethodException

end IOFiberInterceptor
