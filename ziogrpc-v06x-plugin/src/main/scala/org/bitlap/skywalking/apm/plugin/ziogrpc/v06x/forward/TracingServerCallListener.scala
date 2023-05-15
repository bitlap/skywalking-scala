package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.forward

import io.grpc.*
import io.grpc.ServerCall.Listener
import io.grpc.Status.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.InterceptorDSL
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.OperationNameFormatUtils

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
final class TracingServerCallListener[REQUEST](
  delegate: ServerCall.Listener[REQUEST],
  method: MethodDescriptor[?, ?],
  contextSnapshot: ContextSnapshot,
  asyncSpan: AbstractSpan
) extends ForwardingServerCallListener.SimpleForwardingServerCallListener[REQUEST](delegate):

  private val operationPrefix = OperationNameFormatUtils.formatOperationName(method) + SERVER

  override def onMessage(message: REQUEST): Unit =
    if (method.getType.clientSendsOneMessage) {
      delegate.onMessage(message)
      return
    }
    val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_MESSAGE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    InterceptorDSL.continuedSnapshot(contextSnapshot, asyncSpan)(delegate.onMessage(message))
  end onMessage

  override def onCancel(): Unit =
    val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_CANCEL_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    InterceptorDSL.continuedSnapshot(contextSnapshot, asyncSpan)(delegate.onCancel())
  end onCancel

  override def onHalfClose(): Unit =
    val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_HALF_CLOSE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    InterceptorDSL.continuedSnapshot(contextSnapshot, asyncSpan)(delegate.onHalfClose())
  end onHalfClose

  override def onComplete(): Unit =
    delegate.onComplete()

  override def onReady(): Unit = delegate.onReady()

end TracingServerCallListener
