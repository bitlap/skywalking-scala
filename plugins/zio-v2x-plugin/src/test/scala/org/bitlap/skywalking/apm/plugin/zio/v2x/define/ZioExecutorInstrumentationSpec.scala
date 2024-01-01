package org.bitlap.skywalking.apm.plugin.zio.v2x.define

import net.bytebuddy.description.method.MethodDescription

import zio.*

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ZioExecutorInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch" should "ok" in {
    val matcher = ZioExecutorInstrumentation.methodInterceptors(
      ZioExecutorInstrumentation.SUBMIT_METHOD_INTERCEPTOR
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[zio.Executor].getMethod(
        "submit",
        classOf[Runnable],
        classOf[zio.Unsafe]
      )
    )
    matcher.matches(method) shouldEqual true
  }

}
