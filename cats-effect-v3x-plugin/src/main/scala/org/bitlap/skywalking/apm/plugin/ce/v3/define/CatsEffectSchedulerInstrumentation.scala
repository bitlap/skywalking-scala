package org.bitlap.skywalking.apm.plugin.ce.v3.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.*
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ReturnTypeNameMatch
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ce.v3.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class CatsEffectSchedulerInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import CatsEffectSchedulerInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

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

end CatsEffectSchedulerInstrumentation

object CatsEffectSchedulerInstrumentation:

  final val ENHANCE_CLASS = HierarchyMatch.byHierarchyMatch("cats.effect.unsafe.Scheduler")

  final val SLEEP_METHOD_INTERCEPTOR: String = classOf[IOFiberSchedulerInterceptor].getTypeName

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] = Map(
    SLEEP_METHOD_INTERCEPTOR -> named("sleep").and(takesArguments(2))
  )
