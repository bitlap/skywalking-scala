package org.bitlap.skywalking.apm.plugin.zio.v203.define

import java.util.{ Collections, List as JList }

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.*
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.zio.v203.ZioWitnessConstant

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioWorkStealingInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioWorkStealingInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

  override protected def witnessMethods: JList[WitnessMethod] =
    Collections.singletonList(ZioWitnessConstant.WITNESS_203X_METHOD)

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = null

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    methodInterceptors
      .map(kv =>
        new InstanceMethodsInterceptPoint {
          override def getMethodsMatcher: ElementMatcher[MethodDescription] = kv._2
          override def getMethodsInterceptor: String                        = kv._1
          override def isOverrideArgs: Boolean                              = false
        }
      )
      .toArray

  end getInstanceMethodsInterceptPoints

end ZioWorkStealingInstrumentation

object ZioWorkStealingInstrumentation:

  final val ENHANCE_CLASS: String = "zio.internal.FiberRuntime"

  final val FIBER_STEAL_METHOD_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.common.interceptor.SetContextOnNewFiber"

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] = Map(
    FIBER_STEAL_METHOD_INTERCEPTOR -> named("stealWork").and(takesArguments(3))
  )
