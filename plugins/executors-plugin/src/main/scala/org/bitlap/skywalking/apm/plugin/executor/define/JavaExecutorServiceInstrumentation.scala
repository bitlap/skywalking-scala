package org.bitlap.skywalking.apm.plugin.executor.define

import java.util.Collections
import java.util.concurrent.{ Executors, ScheduledExecutorService }

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.{ HierarchyMatch, * }
import org.apache.skywalking.apm.agent.core.plugin.`match`.logical.LogicalMatchOperation
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine
import org.bitlap.skywalking.apm.plugin.common.interceptor.CaptureContextOnSubmitInterceptor

/** @author
 *    梦境迷离
 *  @version 1.0,2023/12/28
 */
class JavaExecutorServiceInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import JavaExecutorServiceInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = null

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    methodInterceptors
      .map(kv =>
        new InstanceMethodsInterceptPoint {
          override def getMethodsMatcher: ElementMatcher[MethodDescription] = kv._2
          override def getMethodsInterceptor: String                        = kv._1
          override def isOverrideArgs: Boolean                              = true
        }
      )
      .toArray

  end getInstanceMethodsInterceptPoints

end JavaExecutorServiceInstrumentation

object JavaExecutorServiceInstrumentation:

  final val ENHANCE_CLASS = LogicalMatchOperation.and(
    LogicalMatchOperation.or(
      HierarchyMatch.byHierarchyMatch("java.util.concurrent.AbstractExecutorService"),
      MultiClassNameMatch.byMultiClassMatch("java.util.concurrent.AbstractExecutorService")
    ),
    LogicalMatchOperation.or(
      HierarchyMatch.byHierarchyMatch("java.util.concurrent.ScheduledExecutorService")
    )
  )

  final val EXECUTE_INTERCEPTOR: String = classOf[CaptureContextOnSubmitInterceptor].getTypeName

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] =
    Map(EXECUTE_INTERCEPTOR -> named("execute"))
