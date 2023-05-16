package org.bitlap.skywalking.apm.plugin.ziogrpc.forward

import io.grpc.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.InterceptorDSL
import org.bitlap.skywalking.apm.plugin.ziogrpc.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.OperationNameFormatUtils

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/14
 */
final class TracingClientCallListener[RESPONSE](
  delegate: ClientCall.Listener[RESPONSE],
  method: MethodDescriptor[?, RESPONSE],
  contextSnapshot: ContextSnapshot,
  asyncSpan: AbstractSpan
) extends ForwardingClientCallListener.SimpleForwardingClientCallListener[RESPONSE](delegate):

  private val operationPrefix = OperationNameFormatUtils.formatOperationName(method) + CLIENT

  override def onMessage(message: RESPONSE): Unit =
    if (method.getType.serverSendsOneMessage) {
      delegate.onMessage(message)
      return
    }

    val span = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_MESSAGE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)

    InterceptorDSL.continuedSnapshot(contextSnapshot)(delegate.onMessage(message))
  end onMessage

  override def onClose(status: Status, trailers: Metadata): Unit =
    val span = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_CLOSE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)

    InterceptorDSL.continuedSnapshot(contextSnapshot, asyncSpan) {
      if (!status.isOk) {
        span.log(status.asRuntimeException)
        Tags.RPC_RESPONSE_STATUS_CODE.set(span, status.getCode.name)
      }
      delegate.onClose(status, trailers)
    }
  end onClose

end TracingClientCallListener
