package org.bitlap.skywalking.apm.plugin.zio.v2x.interceptor

import java.lang.reflect.Method

import scala.collection.AbstractSeq

import zio.internal.FiberRuntime

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.logging.api.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.zcommon.*
import org.bitlap.skywalking.apm.plugin.zio.v2x.ZioPluginConfig

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioFiberRuntimeInterceptor extends InstanceMethodsAroundInterceptor:

  private val SpanSwitch = "spanSwitch"

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    val fiberRuntime      = objInst.asInstanceOf[FiberRuntime[?, ?]]
    val location          = fiberRuntime.location.toString
    val mainMethodRegexes = ZioPluginConfig.Plugin.ZioV2.IGNORE_FIBER_REGEXES.split(",").toList.filter(_.nonEmpty)
    val matchRegex        = mainMethodRegexes.map(_.r).exists(_.matches(location))

    ContextManager.getRuntimeContext.put(SpanSwitch, !matchRegex)

    if location != null && location != "" && !matchRegex then {
      val currentSpan = ContextManager.createLocalSpan(
        AgentUtils.generateFiberOperationName("ZIO", Some(fiberRuntime.location.toString))
      )
      currentSpan.setComponent(ComponentsDefine.JDK_THREADING)
      TagUtils.setZioTags(currentSpan, fiberRuntime.id, objInst)
      AgentUtils.continuedSnapshot(objInst)
    }

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
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
