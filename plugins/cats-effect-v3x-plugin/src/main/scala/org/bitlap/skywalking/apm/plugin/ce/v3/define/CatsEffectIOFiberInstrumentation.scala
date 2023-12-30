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
import org.bitlap.skywalking.apm.plugin.common.interceptor.*

final class CatsEffectIOFiberInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import CatsEffectIOFiberInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = Array(
    new ConstructorInterceptPoint:
      override def getConstructorMatcher: ElementMatcher[MethodDescription] = takesArguments(5)
      override def getConstructorInterceptor: String                        = CLASS_INTERCEPTOR
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

end CatsEffectIOFiberInstrumentation

object CatsEffectIOFiberInstrumentation:

  final val ENHANCE_CLASS = NameMatch.byName("cats.effect.IOFiber")

  final val CLASS_INTERCEPTOR: String = classOf[ConstructorInterceptor].getTypeName

  final val RUN_METHOD_INTERCEPTOR: String = classOf[IOFiberInterceptor].getTypeName

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] = Map(
    RUN_METHOD_INTERCEPTOR -> named("run").and(takesArguments(0))
  )
