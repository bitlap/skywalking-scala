package org.bitlap.skywalking.apm.plugin.zio.v2x.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.*
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.`match`.logical.LogicalMatchOperation
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.common.interceptor.*

final class ZioExecutorInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioExecutorInstrumentation.*

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

end ZioExecutorInstrumentation

object ZioExecutorInstrumentation:

  final val ENHANCE_CLASS = LogicalMatchOperation.or(
    HierarchyMatch.byHierarchyMatch("zio.Executor"),
    MultiClassNameMatch.byMultiClassMatch("zio.Executor")
  )

  final val SUBMIT_METHOD_INTERCEPTOR: String = classOf[CaptureContextOnSubmitInterceptor].getTypeName

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] =
    Map(
      SUBMIT_METHOD_INTERCEPTOR -> named("submit")
    )
