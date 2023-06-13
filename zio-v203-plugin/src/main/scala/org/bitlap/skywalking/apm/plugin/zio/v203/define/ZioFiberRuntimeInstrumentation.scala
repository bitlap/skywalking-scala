package org.bitlap.skywalking.apm.plugin.zio.v203.define

import java.util
import java.util.{ Collections, List as JList }

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.{ ElementMatcher, ElementMatchers }
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ReturnTypeNameMatch
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.zio.v203.ZioWitnessConstant

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioFiberRuntimeInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioFiberRuntimeInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

  override protected def witnessMethods: JList[WitnessMethod] =
    Collections.singletonList(ZioWitnessConstant.WITNESS_203X_METHOD)

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = Array(
    new ConstructorInterceptPoint:
      override def getConstructorMatcher: ElementMatcher[MethodDescription] = takesArguments(3)
      override def getConstructorInterceptor: String                        = FIBER_RUNTIME_CLASS_INTERCEPTOR
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

end ZioFiberRuntimeInstrumentation

object ZioFiberRuntimeInstrumentation:

  final val ENHANCE_CLASS: String = "zio.internal.FiberRuntime"

  final val FIBER_RUNTIME_CLASS_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.common.interceptor.ConstructorInterceptor"

  final val FIBER_RUNTIME_RUN_METHOD_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.common.interceptor.ZioFiberRuntimeInterceptor"

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] =
    (0 until 2)
      .map(i => s"${FIBER_RUNTIME_RUN_METHOD_INTERCEPTOR}_$i")
      .zip(
        List(
          named("run").and(takesArguments(0)),
          named("run").and(takesArguments(1))
        )
      )
      .toMap
