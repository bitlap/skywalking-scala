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
final class ZioExecutorInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioExecutorInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

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

  final val ENHANCE_CLASS: String = "zio.internal.DefaultExecutors$$anon$1"

  final val SUBMIT_METHOD_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.zio.ZioBlockingSubmitInterceptor"

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] = Map(
    SUBMIT_METHOD_INTERCEPTOR -> named("submit").and(takesArguments(2))
  )
