package org.bitlap.skywalking.apm.plugin.ziocache.define

import net.bytebuddy.description.method.MethodDescription

import zio.*

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ZioCacheInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch get" should "ok" in {
    val matcher = ZioCacheInstrumentation.methodInterceptors(
      ZioCacheInstrumentation.CACHE_METHOD_INTERCEPTOR
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[zio.cache.Cache[?, ?, ?]].getMethod(
        "get",
        classOf[Object],
        classOf[Object]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch contains" should "ok" in {
    val matcher = ZioCacheInstrumentation.methodInterceptors(
      ZioCacheInstrumentation.CACHE_METHOD_INTERCEPTOR
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[zio.cache.Cache[?, ?, ?]].getMethod(
        "contains",
        classOf[Object],
        classOf[Object]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch refresh" should "ok" in {
    val matcher = ZioCacheInstrumentation.methodInterceptors(
      ZioCacheInstrumentation.CACHE_METHOD_INTERCEPTOR
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[zio.cache.Cache[?, ?, ?]].getMethod(
        "refresh",
        classOf[Object]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch invalidate" should "ok" in {
    val matcher = ZioCacheInstrumentation.methodInterceptors(
      ZioCacheInstrumentation.CACHE_METHOD_INTERCEPTOR
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[zio.cache.Cache[?, ?, ?]].getMethod(
        "invalidate",
        classOf[Object],
        classOf[Object]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch invalidateAll" should "ok" in {
    val matcher = ZioCacheInstrumentation.methodInterceptors(
      ZioCacheInstrumentation.CACHE_METHOD_INTERCEPTOR
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[zio.cache.Cache[?, ?, ?]].getMethod(
        "invalidateAll"
      )
    )
    matcher.matches(method) shouldEqual true
  }

}
