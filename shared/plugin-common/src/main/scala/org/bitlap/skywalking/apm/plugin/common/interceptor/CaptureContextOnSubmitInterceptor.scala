package org.bitlap.skywalking.apm.plugin.common.interceptor

import java.util.concurrent.Callable

import org.apache.skywalking.apm.agent.core.context.*
import org.bitlap.skywalking.apm.plugin.common.*

final class CaptureContextOnSubmitInterceptor extends AbstractThreadingPoolInterceptor:

  override def wrap(param: Any): Any = {
    if param.isInstanceOf[RunnableWrapper] || param.isInstanceOf[CallableWrapper[?]] then {
      return null
    }
    param match
      case callable: Callable[?] =>
        return new CallableWrapper(callable, ContextManager.capture())
      case runnable: Runnable =>
        return new RunnableWrapper(runnable, ContextManager.capture())
      case _ =>
    null
  }
end CaptureContextOnSubmitInterceptor
