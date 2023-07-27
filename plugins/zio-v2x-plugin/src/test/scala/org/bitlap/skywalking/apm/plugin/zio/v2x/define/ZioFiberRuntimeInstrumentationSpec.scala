package org.bitlap.skywalking.apm.plugin.zio.v2x.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import zio.*
import zio.internal.FiberRuntime

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
class ZioFiberRuntimeInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch1" should "ok" in {
    val matcher = ZioFiberRuntimeInstrumentation.methodInterceptors(
      ZioFiberRuntimeInstrumentation.RUN_METHOD_INTERCEPTOR + "_0"
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[FiberRuntime[Any, Any]].getMethod(
        "run"
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch2" should "ok" in {
    val matcher = ZioFiberRuntimeInstrumentation.methodInterceptors(
      ZioFiberRuntimeInstrumentation.RUN_METHOD_INTERCEPTOR + "_1"
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[FiberRuntime[Any, Any]].getMethod(
        "run",
        classOf[Int]
      )
    )
    matcher.matches(method) shouldEqual true
  }
}
