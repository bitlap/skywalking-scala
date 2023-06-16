package org.bitlap.skywalking.apm.plugin.ce.v3

import java.lang.reflect.Method

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.AgentUtils

/** @author
 *    梦境迷离
 *  @version 1.0,2023/6/15
 */
class IOFiberResumeInterceptor extends InstanceMethodsAroundInterceptor {

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
    if ret.isInstanceOf[Boolean] && ret.equals(true) then {
      if objInst.getSkyWalkingDynamicField != null then {
        val span = ContextManager.createLocalSpan(generateFiberOperationName(method))
        span.setComponent(ComponentsDefine.JDK_THREADING)
        AgentUtils.continuedSnapshot(objInst)
        ContextManager.stopSpan()
      }
    }
    ret

  private def generateFiberOperationName(method: Method) =
    s"CatsIOFiberWrapper/${method.getName}/${Thread.currentThread.getName}"

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = AgentUtils.logError(t)

  end handleMethodException

}
