package org.bitlap.skywalking.apm.plugin.executor.define

import java.util.Collections
import java.util.concurrent.*

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.`match`.logical.LogicalMatchOperation
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine

class ThreadPoolExecutorInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ThreadPoolExecutorInstrumentation.*

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

  override def isBootstrapInstrumentation: Boolean = true

end ThreadPoolExecutorInstrumentation

object ThreadPoolExecutorInstrumentation:

  final val ENHANCE_CLASS =
    LogicalMatchOperation.or(
      HierarchyMatch.byHierarchyMatch("java.util.concurrent.ThreadPoolExecutor"),
      MultiClassNameMatch.byMultiClassMatch("java.util.concurrent.ThreadPoolExecutor")
    )

  final val CAPTURE_ON_SUBMIT_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.common.interceptor.CaptureContextOnSubmitInterceptor"

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] =
    Map(
      CAPTURE_ON_SUBMIT_INTERCEPTOR ->
        named("submit")
          .and(takesArguments(1).and(takesArgument(0, classOf[Runnable])))
          .or(
            named("submit").and(takesArguments(2).and(takesArgument(0, classOf[Runnable])))
          )
          .or(
            named("submit").and(takesArguments(1).and(takesArgument(0, classOf[Callable[?]])))
          )
          .or(
            named("execute").and(takesArguments(1).and(takesArgument(0, classOf[Runnable])))
          )
          .or(
            named("schedule").and(takesArguments(3).and(takesArgument(0, classOf[Runnable])))
          )
          .or(
            named("schedule").and(takesArguments(3).and(takesArgument(0, classOf[Callable[?]])))
          )
    )
