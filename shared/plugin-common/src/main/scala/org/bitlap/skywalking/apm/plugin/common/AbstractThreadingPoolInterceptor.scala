package org.bitlap.skywalking.apm.plugin.common

import java.lang.reflect.Method

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.logging.api.LogManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

abstract class AbstractThreadingPoolInterceptor extends InstanceMethodsAroundInterceptor {

  private lazy val LOGGER = LogManager.getLogger(classOf[AbstractThreadingPoolInterceptor])

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Any],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit = {
    LOGGER.info(s"ThreadingPool name: ${objInst.getClass.getName}, method: ${method.getName}")
    if !ContextManager.isActive then return
    if allArguments == null || allArguments.length < 1 then return
    val argument = allArguments(0)
    // Avoid duplicate enhancement, such as the case where it has already been enhanced by RunnableWrapper or CallableWrapper with toolkit.
    argument match
      case instance: EnhancedInstance if instance.getSkyWalkingDynamicField.isInstanceOf[ContextSnapshot] => return
      case _                                                                                              =>
    val wrappedObject = wrap(argument, objInst.getClass.getName, method.getName)
    if wrappedObject != null then allArguments.update(0, wrappedObject)
  }

  def wrap(param: Any, className: String, methodName: String): Any

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Any],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): AnyRef = ret

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Any],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit =
    if ContextManager.isActive then ContextManager.activeSpan.log(t)
}
