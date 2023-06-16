package org.bitlap.skywalking.apm.plugin.ce.v3.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.*
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ReturnTypeNameMatch
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class CatsEffectIOFiberInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import CatsEffectIOFiberInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = Array(
    new ConstructorInterceptPoint:
      override def getConstructorMatcher: ElementMatcher[MethodDescription] = takesArguments(5)
      override def getConstructorInterceptor: String                        = FIBER_CLASS_INTERCEPTOR
  )

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    methodInterceptors
      .map(kv =>
        new InstanceMethodsInterceptPoint {
          override def getMethodsMatcher: ElementMatcher[MethodDescription] = kv._2
          override def getMethodsInterceptor: String                        = kv._1.split("_")(0)
          override def isOverrideArgs: Boolean                              = false
        }
      )
      .toArray

  end getInstanceMethodsInterceptPoints

end CatsEffectIOFiberInstrumentation

object CatsEffectIOFiberInstrumentation:

  final val ENHANCE_CLASS: String = "cats.effect.IOFiber"

  final val FIBER_CLASS_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.common.interceptor.ConstructorInterceptor"

  final val FIBER_RUN_METHOD_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.ce.v3.IOFiberInterceptor"

  final val FIBER_SUSPEND_METHOD_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.common.interceptor.SaveCurrentContextOnExit"

  final val FIBER_RESUME_METHOD_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.ce.v3.IOFiberResumeInterceptor"

  final val FIBER_SCHEDULE_METHOD_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.common.interceptor.SetContextOnNewFiber"

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] = Map(
    FIBER_RUN_METHOD_INTERCEPTOR     -> named("run").and(takesArguments(0)),
    FIBER_SUSPEND_METHOD_INTERCEPTOR -> named("suspend").and(takesArguments(0)),
    FIBER_RESUME_METHOD_INTERCEPTOR  -> named("resume").and(takesArguments(0))
  ) ++ (0 until 3)
    .map(i => s"${FIBER_SCHEDULE_METHOD_INTERCEPTOR}_$i")
    .zip(
      List(
        named("rescheduleFiber").and(takesArguments(2)),
        named("scheduleFiber").and(takesArguments(2)),
        named("scheduleOnForeignEC").and(takesArguments(2))
      )
    )
    .toMap
