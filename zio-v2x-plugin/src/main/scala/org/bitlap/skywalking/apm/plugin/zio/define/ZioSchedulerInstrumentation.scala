package org.bitlap.skywalking.apm.plugin.zio.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioSchedulerInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioSchedulerInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = Array(
    new ConstructorInterceptPoint:
      override def getConstructorMatcher: ElementMatcher[MethodDescription] = any()

      override def getConstructorInterceptor: String = CONSTRUCTOR_INTERCEPTOR
  )

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

end ZioSchedulerInstrumentation

object ZioSchedulerInstrumentation:

  final val CONSTRUCTOR_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.common.ConstructorInterceptor"

  final val ENHANCE_CLASS: IndirectMatch = HierarchyMatch.byHierarchyMatch("zio.Scheduler")

  final val SCHEDULE_METHOD_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.zio.ZioSchedulerScheduleInterceptor"

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] = Map(
    SCHEDULE_METHOD_INTERCEPTOR -> named("schedule").and(takesArguments(3))
  )
