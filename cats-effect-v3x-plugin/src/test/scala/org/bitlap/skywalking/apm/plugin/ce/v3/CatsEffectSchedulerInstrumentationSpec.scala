package org.bitlap.skywalking.apm.plugin.ce.v3

import scala.concurrent.duration.FiniteDuration

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.bitlap.skywalking.apm.plugin.ce.v3.define.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2023/6/19
 */
class CatsEffectSchedulerInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch" should "ok" in {
    val matcherSleep =
      CatsEffectSchedulerInstrumentation.methodInterceptors(CatsEffectSchedulerInstrumentation.SLEEP_METHOD_INTERCEPTOR)
    val method = new MethodDescription.ForLoadedMethod(
      classOf[cats.effect.unsafe.Scheduler].getMethod(
        "sleep",
        classOf[FiniteDuration],
        classOf[Runnable]
      )
    )

    matcherSleep.matches(method) shouldEqual true
  }
}
