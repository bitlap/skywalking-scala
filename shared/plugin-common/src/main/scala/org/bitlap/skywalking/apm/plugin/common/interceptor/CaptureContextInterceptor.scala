package org.bitlap.skywalking.apm.plugin.common.interceptor

import java.lang.reflect.Method
import java.util.concurrent.{ Callable, RunnableFuture }

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.logging.api.LogManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.common.*

final class CaptureContextOnScheduleInterceptor extends CaptureContextInterceptor

final class CaptureContextOnSubmitInterceptor extends CaptureContextInterceptor

final class CaptureContextOnExecuteInterceptor extends CaptureContextInterceptor

open class CaptureContextInterceptor extends InstanceMethodsAroundInterceptor:

  private lazy val LOGGER = LogManager.getLogger(classOf[CaptureContextInterceptor])

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
    argument match
      case instance: EnhancedInstance =>
        val ctx = instance.getSkyWalkingDynamicField
        if ctx != null && ctx.isInstanceOf[ContextSnapshot] then return
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
end CaptureContextInterceptor
