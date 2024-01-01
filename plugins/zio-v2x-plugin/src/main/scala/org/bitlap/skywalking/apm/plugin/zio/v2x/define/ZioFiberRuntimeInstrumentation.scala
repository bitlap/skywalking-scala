package org.bitlap.skywalking.apm.plugin.zio.v2x.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.*
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.common.interceptor.*
import org.bitlap.skywalking.apm.plugin.zio.v2x.interceptor.FiberRuntimeInterceptor

final class ZioFiberRuntimeInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioFiberRuntimeInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = Array(
    new ConstructorInterceptPoint:
      override def getConstructorMatcher: ElementMatcher[MethodDescription] = takesArguments(3)
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

end ZioFiberRuntimeInstrumentation

object ZioFiberRuntimeInstrumentation:

  final val ENHANCE_CLASS = NameMatch.byName("zio.internal.FiberRuntime")

  final val CLASS_INTERCEPTOR: String = classOf[ConstructorInterceptor].getTypeName

  final val RUN_METHOD_INTERCEPTOR: String = classOf[FiberRuntimeInterceptor].getTypeName

//  final val EXECUTOR_INTERCEPTOR: String = classOf[SetContextOnNewFiber].getTypeName

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] =
    Map(
//      EXECUTOR_INTERCEPTOR   -> named("drainQueueLaterOnExecutor").and(takesArguments(1)),
      RUN_METHOD_INTERCEPTOR -> named("run")
    )
