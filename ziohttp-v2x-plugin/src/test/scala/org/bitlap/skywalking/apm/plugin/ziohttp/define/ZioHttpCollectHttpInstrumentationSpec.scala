package org.bitlap.skywalking.apm.plugin.ziohttp.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import zio.*

import org.bitlap.skywalking.apm.plugin.ziohttp.define.ZioHttpCollectHttpInstrumentation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import zhttp.http.Http.PartialCollectHttp

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
class ZioHttpCollectHttpInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch" should "ok" in {
    val matcher = ZioHttpCollectHttpInstrumentation.getMethod
    val method = new MethodDescription.ForLoadedMethod(
      classOf[PartialCollectHttp[?]].getMethod(
        ZioHttpCollectHttpInstrumentation.ENHANCE_METHOD,
        classOf[scala.runtime.BoxedUnit],
        classOf[PartialFunction[?, ?]]
      )
    )
    matcher.matches(method) shouldEqual true
  }
}
