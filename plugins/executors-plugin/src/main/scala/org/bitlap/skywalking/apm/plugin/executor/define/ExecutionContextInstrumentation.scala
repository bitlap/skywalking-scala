package org.bitlap.skywalking.apm.plugin.executor.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.`match`.logical.LogicalMatchOperation
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine
import org.bitlap.skywalking.apm.plugin.common.interceptor.CaptureContextOnSubmitInterceptor

final class ExecutionContextInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ExecutionContextInstrumentation.*

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

end ExecutionContextInstrumentation

object ExecutionContextInstrumentation:

  final val ENHANCE_CLASS = HierarchyMatch.byHierarchyMatch("scala.concurrent.ExecutionContext")

  final val CAPTURE_ON_SUBMIT_INTERCEPTOR: String = classOf[CaptureContextOnSubmitInterceptor].getTypeName

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] =
    Map(
      CAPTURE_ON_SUBMIT_INTERCEPTOR ->
        named("execute")
    )
