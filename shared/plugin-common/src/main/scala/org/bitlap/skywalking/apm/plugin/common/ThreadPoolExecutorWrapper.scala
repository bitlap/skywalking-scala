package org.bitlap.skywalking.apm.plugin.common

import java.util
import java.util.concurrent.*

import org.apache.skywalking.apm.agent.core.context.{ ContextManager, ContextSnapshot }
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance
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

  override def execute(command: Runnable): Unit =
    command match
      case e: EnhancedInstance
          if e.getSkyWalkingDynamicField.isInstanceOf[ContextSnapshot] || !ContextManager.isActive =>
        threadPoolExecutor.execute(command)
      case _ =>
        threadPoolExecutor.execute(
          new RunnableWrapper(command, ContextManager.capture(), threadPoolExecutor.getClass.getName, "execute")
        )
}
