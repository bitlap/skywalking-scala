package org.bitlap.skywalking.apm.plugin.common.interceptor

import java.lang.reflect.Method
import java.util.concurrent.Callable
import java.util.concurrent.RunnableFuture

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.logging.api.LogManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor
import org.bitlap.skywalking.apm.plugin.common.*

final class CaptureContextOnSubmitInterceptor extends InstanceMethodsAroundInterceptor:

  private lazy val LOGGER = LogManager.getLogger(classOf[CaptureContextOnSubmitInterceptor])

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Any],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit = {
    LOGGER.debug(
      s"ClassName: ${objInst.getClass.getName}, methodName: ${method.getName}, argumentsTypes: ${argumentsTypes.map(_.getName).mkString("[", ",", "]")}"
    )
    if !ContextManager.isActive then return
    if allArguments == null || allArguments.length < 1 then return
    val argument = allArguments(0)
    // Avoid duplicate enhancement, such as the case where it has already been enhanced by RunnableWrapper or CallableWrapper with toolkit.
    val isZioFiber = argument.getClass.getName == "zio.internal.FiberRuntime"
    val isCeFiber  = argument.getClass.getName == "cats.effect.IOFiber"
    argument match
      case instance: EnhancedInstance
          if !isZioFiber && !isCeFiber && instance.getSkyWalkingDynamicField.isInstanceOf[ContextSnapshot] =>
        return
      case _ =>
    val wrappedObject = wrap(argument, objInst.getClass.getName, method.getName)
    if wrappedObject != null then allArguments.update(0, wrappedObject)
  }

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

  private def wrap(param: Any, className: String, methodName: String): Any = {
    param match
      case _: RunnableFuture[?] => return null
      case _: RunnableWrapper | _: CallableWrapper[?] =>
        return null
      case callable: Callable[?] =>
        return new CallableWrapper(callable, ContextManager.capture(), className, methodName)
      case runnable: Runnable =>
        return new RunnableWrapper(runnable, ContextManager.capture(), className, methodName)
      case _ =>
    null
  }
end CaptureContextOnSubmitInterceptor
