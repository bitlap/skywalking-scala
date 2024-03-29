package org.bitlap.skywalking.apm.plugin.ziogrpc.common.listener

import io.grpc.*
import io.grpc.ServerCall.Listener
import io.grpc.Status.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.common.AgentUtils
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.Constants.*

final class TracingServerCallListener[Req](
  delegate: ServerCall.Listener[Req],
  method: MethodDescriptor[?, ?],
  contextSnapshot: ContextSnapshot,
  asyncSpan: AbstractSpan
) extends ForwardingServerCallListener.SimpleForwardingServerCallListener[Req](delegate):

  private val operationPrefix = OperationNameFormatUtils.formatOperationName(method) + SERVER

  override def onMessage(message: Req): Unit =
    if method.getType.clientSendsOneMessage then {
      delegate.onMessage(message)
      return
    }
    val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_MESSAGE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    AgentUtils.continuedSnapshot(contextSnapshot, asyncSpan)(delegate.onMessage(message))
  end onMessage

  override def onCancel(): Unit =
    val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_CANCEL_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    AgentUtils.continuedSnapshot(contextSnapshot, asyncSpan)(delegate.onCancel())
  end onCancel

  override def onHalfClose(): Unit =
    val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_HALF_CLOSE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    AgentUtils.continuedSnapshot(contextSnapshot, asyncSpan)(delegate.onHalfClose())
  end onHalfClose

  override def onComplete(): Unit =
    delegate.onComplete()

  override def onReady(): Unit = delegate.onReady()

end TracingServerCallListener
