package org.bitlap.skywalking.apm.plugin.ziogrpc.common.listener

import io.grpc.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.AgentUtils
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.Constants.*

final class TracingClientCallListener[Resp](
  delegate: ClientCall.Listener[Resp],
  method: MethodDescriptor[?, Resp],
  contextSnapshot: ContextSnapshot,
  asyncSpan: AbstractSpan
) extends ForwardingClientCallListener.SimpleForwardingClientCallListener[Resp](delegate):

  private val operationPrefix = OperationNameFormatUtils.formatOperationName(method) + CLIENT

  override def onMessage(message: Resp): Unit =
    if method.getType.serverSendsOneMessage then {
      delegate.onMessage(message)
      return
    }

    val span = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_MESSAGE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)

    AgentUtils.continuedSnapshot(contextSnapshot)(delegate.onMessage(message))
  end onMessage

  override def onClose(status: Status, trailers: Metadata): Unit =
    val span = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_CLOSE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)

    AgentUtils.continuedSnapshot(contextSnapshot, asyncSpan) {
      if !status.isOk then {
        span.log(status.asRuntimeException)
        Tags.RPC_RESPONSE_STATUS_CODE.set(span, status.getCode.name)
      }
      delegate.onClose(status, trailers)
    }
  end onClose

end TracingClientCallListener
