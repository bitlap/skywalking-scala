package org.bitlap.skywalking.apm.plugin.zio.v2x.interceptor

import java.lang.reflect.Method
import java.text.SimpleDateFormat

import scala.collection.AbstractSeq
import scala.util.*

import zio.internal.FiberRuntime

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.logging.api.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.zio.v2x.ZioPluginConfig

import _root_.zio.*
import _root_.zio.internal.FiberRuntime

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioFiberRuntimeInterceptor extends InstanceMethodsAroundInterceptor:

  private val SpanSwitch = "spanSwitch"

  final lazy val fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  private def formatDate(long: Long) = fmt.format(long)

  def setZioTags(span: AbstractSpan, fiberId: FiberId.Runtime, objInst: EnhancedInstance): Unit =
    Try {
      CustomTag.FiberId.tag.set(span, fiberId.id.toString)
      CustomTag.FiberStartTime.tag.set(span, formatDate(fiberId.startTimeMillis))
      CustomTag.FiberLocation.tag.set(span, fiberId.location.toString)
      CustomTag.FiberClassName.tag.set(span, objInst.getClass.getName)
      CustomTag.CurrentThread.tag.set(span, Thread.currentThread().getName)
    } match
      case Failure(ex) => span.errorOccurred.log(ex)
      case Success(_)  =>
  end setZioTags

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    if objInst.getSkyWalkingDynamicField == null then return
    val fiberRuntime      = objInst.asInstanceOf[FiberRuntime[?, ?]]
    val location          = fiberRuntime.location.toString
    val mainMethodRegexes = ZioPluginConfig.Plugin.ZioV2.IGNORE_FIBER_REGEXES.split(",").toList.filter(_.nonEmpty)
    val matchRegex        = mainMethodRegexes.map(_.r).exists(_.matches(location))

    ContextManager.getRuntimeContext.put(SpanSwitch, !matchRegex)

    if location != null && location != "" && !matchRegex then {
      val currentSpan = ContextManager.createLocalSpan(
        "ZIORunnableWrapper/" + Thread.currentThread().getName
      )
      currentSpan.setComponent(ComponentsDefine.JDK_THREADING)
      setZioTags(currentSpan, fiberRuntime.id, objInst)
      AgentUtils.continuedSnapshot(objInst)
    }

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    if objInst.getSkyWalkingDynamicField == null then return ret
    val switch = ContextManager.getRuntimeContext.get(SpanSwitch, classOf[Boolean])
    if switch then {
      AgentUtils.stopIfActive()
    }

    ret

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = AgentUtils.logError(t)

  end handleMethodException

end ZioFiberRuntimeInterceptor
