package org.bitlap.skywalking.apm.plugin.executor

import java.util
import java.util.concurrent.*

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.bitlap.skywalking.apm.plugin.common.*

final class ThreadPoolExecutorWrapper(threadPoolExecutor: ThreadPoolExecutor)
    extends ThreadPoolExecutor(
      threadPoolExecutor.getCorePoolSize,
      threadPoolExecutor.getMaximumPoolSize,
      threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS),
      TimeUnit.MILLISECONDS,
      threadPoolExecutor.getQueue,
      threadPoolExecutor.getThreadFactory,
      threadPoolExecutor.getRejectedExecutionHandler
    ) {

  override def shutdown(): Unit = threadPoolExecutor.shutdown()

  override def shutdownNow(): util.List[Runnable] = threadPoolExecutor.shutdownNow()

  override def isShutdown: Boolean = threadPoolExecutor.isShutdown

  override def isTerminated: Boolean = threadPoolExecutor.isTerminated

  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean =
    threadPoolExecutor.awaitTermination(timeout, unit)

  override def submit[T](task: Callable[T]): Future[T] =
    threadPoolExecutor.submit(
      new CallableWrapper[T](task, ContextManager.capture(), threadPoolExecutor.getClass.getName, "submit")
    )

  override def submit[T](task: Runnable, result: T): Future[T] =
    threadPoolExecutor.submit(
      new RunnableWrapper(task, ContextManager.capture(), threadPoolExecutor.getClass.getName, "submit"),
      result
    )

  override def submit(task: Runnable): Future[?] =
    threadPoolExecutor.submit(
      new RunnableWrapper(task, ContextManager.capture(), threadPoolExecutor.getClass.getName, "submit")
    )

  override def invokeAll[T](tasks: util.Collection[? <: Callable[T]]): util.List[Future[T]] =
    threadPoolExecutor.invokeAll(tasks)

  override def invokeAll[T](
    tasks: util.Collection[? <: Callable[T]],
    timeout: Long,
    unit: TimeUnit
  ): util.List[Future[T]] =
    threadPoolExecutor.invokeAll(tasks, timeout, unit)

  override def invokeAny[T](tasks: util.Collection[? <: Callable[T]]): T =
    threadPoolExecutor.invokeAny(tasks)

  override def invokeAny[T](tasks: util.Collection[? <: Callable[T]], timeout: Long, unit: TimeUnit): T =
    threadPoolExecutor.invokeAny(tasks, timeout, unit)

  override def execute(command: Runnable): Unit =
    threadPoolExecutor.execute(
      new RunnableWrapper(command, ContextManager.capture(), threadPoolExecutor.getClass.getName, "execute")
    )
}
