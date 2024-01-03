package org.bitlap.skywalking.apm.plugin.ziohttp.v2.define

import net.bytebuddy.description.method.MethodDescription

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import zhttp.http.Http.PartialCollectHttp

class ZioHttpCollectHttpInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch" should "ok" in {
    val matcher = ZioHttpCollectHttpInstrumentation.getMethod
    val method = new MethodDescription.ForLoadedMethod(
      classOf[PartialCollectHttp[?]].getMethod(
        "apply$extension",
        classOf[scala.runtime.BoxedUnit],
        classOf[PartialFunction[?, ?]]
      )
    )
    matcher.matches(method) shouldEqual true
  }
}
