package org.bitlap.skywalking.apm.plugin.executor.define

import java.util.Collections
import java.util.concurrent.*

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.`match`.logical.LogicalMatchOperation
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine
import org.bitlap.skywalking.apm.plugin.common.interceptor.CaptureContextOnSubmitInterceptor

class ThreadPoolExecutorInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ThreadPoolExecutorInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = null

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    methodInterceptors
      .map(kv =>
        new InstanceMethodsInterceptPoint {
          override def getMethodsMatcher: ElementMatcher[MethodDescription] = kv._2
          override def getMethodsInterceptor: String                        = kv._1.split("_")(0)
          override def isOverrideArgs: Boolean                              = true
        }
      )
      .toArray

  end getInstanceMethodsInterceptPoints

end ThreadPoolExecutorInstrumentation

object ThreadPoolExecutorInstrumentation:

  final val ENHANCE_CLASS = LogicalMatchOperation.or(
    // match ScheduledThreadPoolExecutor
    MultiClassNameMatch.byMultiClassMatch("java.util.concurrent.ScheduledThreadPoolExecutor"),
    // match subclasses of ThreadPoolExecutor
    HierarchyMatch.byHierarchyMatch("java.util.concurrent.ThreadPoolExecutor"),
    // match subclasses of AbstractExecutorService exclude ThreadPoolExecutor
    LogicalMatchOperation.and(
      HierarchyMatch.byHierarchyMatch("java.util.concurrent.AbstractExecutorService"),
      LogicalMatchOperation.not(
        PrefixMatch.nameStartsWith("java.util.concurrent.ThreadPoolExecutor")
      )
    )
  )

  final val EXECUTE_RUNNABLE_INTERCEPTOR: String  = classOf[CaptureContextOnSubmitInterceptor].getTypeName
  final val SUBMIT_RUNNABLE_INTERCEPTOR: String   = classOf[CaptureContextOnSubmitInterceptor].getTypeName
  final val SUBMIT_CALLABLE_INTERCEPTOR: String   = classOf[CaptureContextOnSubmitInterceptor].getTypeName
  final val SCHEDULE_RUNNABLE_INTERCEPTOR: String = classOf[CaptureContextOnSubmitInterceptor].getTypeName
  final val SCHEDULE_CALLABLE_INTERCEPTOR: String = classOf[CaptureContextOnSubmitInterceptor].getTypeName

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] =
    Map(
      SUBMIT_RUNNABLE_INTERCEPTOR + "_0" -> named("submit")
        .and(takesArguments(1).and(takesArgument(0, classOf[Runnable]))),
      SUBMIT_RUNNABLE_INTERCEPTOR + "_1" -> named("submit")
        .and(takesArguments(2).and(takesArgument(0, classOf[Runnable]))),
      SUBMIT_CALLABLE_INTERCEPTOR + "_2" -> named("submit")
        .and(takesArguments(1).and(takesArgument(0, classOf[Callable[?]]))),
      EXECUTE_RUNNABLE_INTERCEPTOR + "_3" -> named("execute").and(takesArguments(1)),
      SCHEDULE_RUNNABLE_INTERCEPTOR + "_4" -> named("schedule").and(
        takesArguments(3).and(takesArgument(0, classOf[Runnable]))
      ),
      SCHEDULE_CALLABLE_INTERCEPTOR + "_5" -> named("schedule").and(
        takesArguments(3).and(takesArgument(0, classOf[Callable[?]]))
      )
    )
