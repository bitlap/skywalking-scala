package org.bitlap.skywalking.apm.plugin.executor.define

import java.util.concurrent.*

import net.bytebuddy.description.`type`.TypeDescription
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import org.apache.skywalking.apm.dependencies.io.netty.channel.SingleThreadEventLoop
import org.apache.skywalking.apm.dependencies.io.netty.util.concurrent.DefaultEventExecutor
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ThreadPoolExecutorInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testClassMatch ThreadPoolExecutor" should "ok" in {
    val matcher            = ThreadPoolExecutorInstrumentation.ENHANCE_CLASS
    val threadPoolExecutor = matcher.isMatch(new TypeDescription.ForLoadedType(classOf[ThreadPoolExecutor]))
    assert(!threadPoolExecutor)

    val scheduledThreadPoolExecutor =
      matcher.isMatch(new TypeDescription.ForLoadedType(classOf[ScheduledThreadPoolExecutor]))
    assert(scheduledThreadPoolExecutor)

    val defaultEventExecutor =
      matcher.isMatch(new TypeDescription.ForLoadedType(classOf[DefaultEventExecutor]))
    assert(defaultEventExecutor)

    val singleThreadEventLoop =
      matcher.isMatch(new TypeDescription.ForLoadedType(classOf[SingleThreadEventLoop]))
    assert(singleThreadEventLoop)
  }

  "testMethodMatch submit1" should "ok" in {
    val matcher = ThreadPoolExecutorInstrumentation.methodInterceptors(
      ThreadPoolExecutorInstrumentation.SUBMIT_RUNNABLE_INTERCEPTOR + "_0"
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[AbstractExecutorService].getMethod(
        "submit",
        classOf[Runnable]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch submit2" should "ok" in {
    val matcher = ThreadPoolExecutorInstrumentation.methodInterceptors(
      ThreadPoolExecutorInstrumentation.SUBMIT_RUNNABLE_INTERCEPTOR + "_1"
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[AbstractExecutorService].getMethod(
        "submit",
        classOf[Runnable],
        classOf[Object]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch submit3" should "ok" in {
    val matcher = ThreadPoolExecutorInstrumentation.methodInterceptors(
      ThreadPoolExecutorInstrumentation.SUBMIT_CALLABLE_INTERCEPTOR + "_2"
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[AbstractExecutorService].getMethod(
        "submit",
        classOf[Callable[?]]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch execute" should "ok" in {
    val matcher = ThreadPoolExecutorInstrumentation.methodInterceptors(
      ThreadPoolExecutorInstrumentation.EXECUTE_RUNNABLE_INTERCEPTOR + "_3"
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[AbstractExecutorService].getMethod(
        "execute",
        classOf[Runnable]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch schedule1" should "ok" in {
    val matcher = ThreadPoolExecutorInstrumentation.methodInterceptors(
      ThreadPoolExecutorInstrumentation.SCHEDULE_RUNNABLE_INTERCEPTOR + "_4"
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ScheduledThreadPoolExecutor].getMethod(
        "schedule",
        classOf[Runnable],
        classOf[Long],
        classOf[TimeUnit]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch schedule2" should "ok" in {
    val matcher = ThreadPoolExecutorInstrumentation.methodInterceptors(
      ThreadPoolExecutorInstrumentation.SCHEDULE_RUNNABLE_INTERCEPTOR + "_5"
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ScheduledThreadPoolExecutor].getMethod(
        "schedule",
        classOf[Callable[?]],
        classOf[Long],
        classOf[TimeUnit]
      )
    )
    matcher.matches(method) shouldEqual true
  }

}
