package org.bitlap.skywalking.apm.plugin.ce.v3.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.*
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class CatsEffectWorkStealingInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import CatsEffectWorkStealingInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

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

end CatsEffectWorkStealingInstrumentation

object CatsEffectWorkStealingInstrumentation:

  final val ENHANCE_CLASS: String = "cats.effect.unsafe.WorkStealingThreadPool"

  final val FIBER_SCHEDULE_METHOD_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.common.interceptor.SetContextOnNewFiber"

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] = Map(
    FIBER_SCHEDULE_METHOD_INTERCEPTOR -> named("scheduleExternal").and(takesArguments(0))
  )
