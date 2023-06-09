package org.bitlap.skywalking.apm.plugin.zio

import java.lang.reflect.Method

import zio.internal.FiberRuntime

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.InterceptorDSL
import org.bitlap.skywalking.apm.plugin.zio.ExecutorType.{ DefaultBlockingExecutor, Executor, ZScheduler }
import org.bitlap.skywalking.apm.plugin.zio.Utils

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/17
 */
final class ZioExecutorInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit = {
    implicit val span = ContextManager.createLocalSpan(generateWorkerOperationName(method))
    span.setComponent(ComponentsDefine.JDK_THREADING)
    Utils.getExecutorType(objInst.getClass.getName) match
      case Executor =>
        Utils.continuedSnapshotFromFiber(allArguments(0), objInst)
      case ZScheduler =>
        span.setOperationName(generateZSchedulerOperationName(method))
        Utils.continuedSnapshotFromFiber(allArguments(0), objInst)
      case DefaultBlockingExecutor =>
        span.setOperationName(generateBlockingOperationName(method))
        Utils.continuedSnapshotFromEnhance(allArguments(0), objInst)
  }

  private def generateBlockingOperationName(method: Method): String =
    s"ZioBlockingWrapper/${method.getName}/${Thread.currentThread().getName}"

  private def generateZSchedulerOperationName(method: Method): String =
    s"ZSchedulerWrapper/${method.getName}/${Thread.currentThread().getName}"

  private def generateWorkerOperationName(method: Method): String =
    s"ZioExecutorWrapper/${method.getName}/${Thread.currentThread().getName}"

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
