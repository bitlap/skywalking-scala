package org.bitlap.skywalking.apm.plugin.common.interceptor

import java.util.concurrent.Callable

import org.apache.skywalking.apm.agent.core.context.*
import org.bitlap.skywalking.apm.plugin.common.*

final class CaptureContextOnSubmitInterceptor extends AbstractThreadingPoolInterceptor:

  override def wrap(param: Any): Any = {
    param match
      case _: RunnableWrapper | _: CallableWrapper[?] =>
        return null
      case callable: Callable[?] =>
        return new CallableWrapper(callable, ContextManager.capture())
      case runnable: Runnable =>
        return new RunnableWrapper(runnable, ContextManager.capture())
      case _ =>
    null
  }
end CaptureContextOnSubmitInterceptor
