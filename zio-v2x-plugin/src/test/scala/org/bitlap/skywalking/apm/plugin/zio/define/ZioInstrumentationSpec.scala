package org.bitlap.skywalking.apm.plugin.zio.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import zio.*
import zio.internal.FiberRuntime

import org.bitlap.skywalking.apm.plugin.zio.define.ZioInstrumentation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
class ZioInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch" should "ok" in {
    val matcher = ZioInstrumentation.methodInterceptors(ZioInstrumentation.FIBER_RUNTIME_RUN_METHOD_INTERCEPTOR)
    val method = new MethodDescription.ForLoadedMethod(
      classOf[FiberRuntime[Any, Any]].getMethod(
        "run"
      )
    )
    matcher.matches(method) shouldEqual true
  }
}
