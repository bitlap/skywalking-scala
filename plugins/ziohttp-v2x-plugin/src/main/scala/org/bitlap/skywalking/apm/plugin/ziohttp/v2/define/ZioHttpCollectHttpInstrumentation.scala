package org.bitlap.skywalking.apm.plugin.ziohttp.v2.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ziohttp.v2.*

final class ZioHttpCollectHttpInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioHttpCollectHttpInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = null

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] = Array(
    new InstanceMethodsInterceptPoint {
      override def getMethodsMatcher: ElementMatcher[MethodDescription] = getMethod

      override def getMethodsInterceptor: String = INTERCEPTOR_CLASS

      override def isOverrideArgs: Boolean = false
    }
  )

object ZioHttpCollectHttpInstrumentation:

  // PartialCollectHttp is a AnyVal!!!
  final val ENHANCE_CLASS = NameMatch.byName("zhttp.http.Http$PartialCollectHttp$")

  final val INTERCEPTOR_CLASS: String =
    "org.bitlap.skywalking.apm.plugin.ziohttp.v2.interceptor.ZioHttpCollectHttpInterceptor"

  def getMethod: ElementMatcher[MethodDescription] =
    named("apply$extension").and(takesArguments(2))
