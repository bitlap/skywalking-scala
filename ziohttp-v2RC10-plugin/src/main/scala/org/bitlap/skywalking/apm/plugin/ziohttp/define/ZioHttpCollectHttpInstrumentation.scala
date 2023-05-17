package org.bitlap.skywalking.apm.plugin.ziohttp.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch.takesArgumentWithType
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/17
 */
final class ZioHttpCollectHttpInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioHttpCollectHttpInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

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
  final val INTERCEPTOR_CLASS: String = "org.bitlap.skywalking.apm.plugin.ziohttp.ZioHttpCollectHttpInterceptor"
  final val ENHANCE_CLASS: String     = "zhttp.http.Http$PartialCollectHttp$"
  final val ENHANCE_METHOD: String    = "apply$extension"

  def getMethod: ElementMatcher[MethodDescription] =
    named(ENHANCE_METHOD).and(takesArguments(2))
