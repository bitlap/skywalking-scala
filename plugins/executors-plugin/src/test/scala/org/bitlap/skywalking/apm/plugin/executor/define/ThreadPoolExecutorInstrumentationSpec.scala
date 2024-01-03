package org.bitlap.skywalking.apm.plugin.executor.define

import java.util.concurrent.*

import net.bytebuddy.description.`type`.TypeDescription
import net.bytebuddy.description.method.MethodDescription

import org.apache.skywalking.apm.dependencies.io.netty.channel.SingleThreadEventLoop
import org.apache.skywalking.apm.dependencies.io.netty.util.concurrent.DefaultEventExecutor
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ThreadPoolExecutorInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testClassMatch ThreadPoolExecutor" should "ok" in {
    val matcher            = ThreadPoolExecutorInstrumentation.ENHANCE_CLASS
    val threadPoolExecutor = matcher.isMatch(new TypeDescription.ForLoadedType(classOf[ThreadPoolExecutor]))
    assert(threadPoolExecutor)

    val forkJoinPool = matcher.isMatch(new TypeDescription.ForLoadedType(classOf[ForkJoinPool]))
    assert(!forkJoinPool)

    val scheduledThreadPoolExecutor =
      matcher.isMatch(new TypeDescription.ForLoadedType(classOf[ScheduledThreadPoolExecutor]))
    assert(scheduledThreadPoolExecutor)
  }

  "testMethodMatch submit1" should "ok" in {
    val matcher = ThreadPoolExecutorInstrumentation.methodInterceptors(
      ThreadPoolExecutorInstrumentation.CAPTURE_ON_SUBMIT_INTERCEPTOR
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ThreadPoolExecutor].getMethod(
        "submit",
        classOf[Runnable]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch submit2" should "ok" in {
    val matcher = ThreadPoolExecutorInstrumentation.methodInterceptors(
      ThreadPoolExecutorInstrumentation.CAPTURE_ON_SUBMIT_INTERCEPTOR
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ThreadPoolExecutor].getMethod(
        "submit",
        classOf[Runnable],
        classOf[Object]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch submit3" should "ok" in {
    val matcher = ThreadPoolExecutorInstrumentation.methodInterceptors(
      ThreadPoolExecutorInstrumentation.CAPTURE_ON_SUBMIT_INTERCEPTOR
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[AbstractExecutorService].getMethod(
        "submit",
        classOf[Callable[?]]
      )
    )
    matcher.matches(method) shouldEqual true
  }

  "testMethodMatch schedule1" should "ok" in {
    val matcher = ThreadPoolExecutorInstrumentation.methodInterceptors(
      ThreadPoolExecutorInstrumentation.CAPTURE_ON_SCHEDULE_INTERCEPTOR
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
      ThreadPoolExecutorInstrumentation.CAPTURE_ON_SCHEDULE_INTERCEPTOR
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
