package org.bitlap.skywalking.apm.plugin.common.interceptor

import java.lang.reflect.Method
import java.util.concurrent.Callable

import scala.collection.AbstractSeq

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.logging.api.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
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
