package org.bitlap.skywalking.apm.plugin.ziocache.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.*
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.`match`.logical.LogicalMatchOperation
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ziocache.interceptor.ZioCacheInterceptor

final class ZioCacheInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioCacheInstrumentation.*

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

end ZioCacheInstrumentation

object ZioCacheInstrumentation:

  final val ENHANCE_CLASS = HierarchyMatch.byHierarchyMatch("zio.cache.Cache")

  final val CACHE_METHOD_INTERCEPTOR: String = classOf[ZioCacheInterceptor].getTypeName

  // ERROR 2023-12-31 18:08:51.022 ZScheduler-Worker-8 SkyWalkingAgent : Enhance class zio.cache.Cache$$anon$1 error.
  // java.lang.IllegalArgumentException: Cannot resolve In from class zio.cache.Cache$$anon$1
  // see https://github.com/raphw/byte-buddy/issues/1577
  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] =
    Map(
      CACHE_METHOD_INTERCEPTOR ->
        named("invalidateAll")
          .or(named("size"))
          .or(named("contains"))
          .or(named("refresh"))
          .or(named("invalidate"))
          .or(named("get"))
    )
