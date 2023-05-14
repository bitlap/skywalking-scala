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
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.OperationNameFormatUtils

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
final class TracingServerCall[REQUEST, RESPONSE](
  delegate: ServerCall[REQUEST, RESPONSE],
  contextSnapshot: ContextSnapshot,
  asyncSpan: AbstractSpan
) extends ForwardingServerCall.SimpleForwardingServerCall[REQUEST, RESPONSE](delegate):

  private val operationPrefix = OperationNameFormatUtils.formatOperationName(delegate.getMethodDescriptor) + SERVER

  override def sendMessage(message: RESPONSE): Unit =
    if (getMethodDescriptor.getType.serverSendsOneMessage) {
      delegate.sendMessage(message)
      return
    }
    val span = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_MESSAGE_OPERATION_NAME)
    span.setComponent(ComponentsDefine.GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    try
      ContextManager.continued(contextSnapshot)
      delegate.sendMessage(message)
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
        throw t
    } finally ContextManager.stopSpan()

  override def close(status: Status, trailers: Metadata): Unit =
    val span = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_CLOSE_OPERATION_NAME)
    span.setComponent(ComponentsDefine.GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    try
      ContextManager.continued(contextSnapshot)
    catch {
      case e: Throwable =>
        ContextManager.activeSpan.log(e)
    }

    status match {
      case OK      =>
      case UNKNOWN =>
      case INTERNAL =>
        if (status.getCause == null) span.log(status.asRuntimeException)
        else span.log(status.getCause)
      case _ =>
        if (status.getCause != null) span.log(status.getCause)
    }
    Tags.RPC_RESPONSE_STATUS_CODE.set(span, status.getCode.name)
    try
      asyncSpan.asyncFinish
      delegate.close(status, trailers)
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
        throw t
    } finally ContextManager.stopSpan()

end TracingServerCall
