package org.bitlap.skywalking.apm.plugin.executor.define

import java.util.concurrent.*

import net.bytebuddy.description.method.MethodDescription

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ExecutionContextInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch execute" should "ok" in {
    val matcher = ExecutionContextInstrumentation.methodInterceptors(
      ExecutionContextInstrumentation.CAPTURE_ON_SUBMIT_INTERCEPTOR
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[AbstractExecutorService].getMethod(
        "execute",
        classOf[Runnable]
      )
    )
    matcher.matches(method) shouldEqual true
  }

}
