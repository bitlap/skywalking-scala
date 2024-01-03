package org.bitlap.skywalking.apm.plugin.zio.v2x.define

import net.bytebuddy.description.method.MethodDescription

import zio.*
import zio.internal.FiberRuntime

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ZioFiberRuntimeInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch1" should "ok" in {
    val matcher = ZioFiberRuntimeInstrumentation.methodInterceptors(
      ZioFiberRuntimeInstrumentation.RUN_METHOD_INTERCEPTOR
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
      ZioFiberRuntimeInstrumentation.RUN_METHOD_INTERCEPTOR
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
