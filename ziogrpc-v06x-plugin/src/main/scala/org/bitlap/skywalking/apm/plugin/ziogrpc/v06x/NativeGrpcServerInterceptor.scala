package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

import io.grpc.Context
import io.grpc.Contexts
import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.Status
import io.grpc.Status.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.apache.skywalking.apm.util.StringUtil
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.OperationNameFormatUtils

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
class NativeGrpcServerInterceptor extends io.grpc.ServerInterceptor:

  import NativeGrpcServerInterceptor.*

  override def interceptCall[REQUEST, RESPONSE](
    call: ServerCall[REQUEST, RESPONSE],
    headers: Metadata,
    handler: ServerCallHandler[REQUEST, RESPONSE]
  ): ServerCall.Listener[REQUEST] =
    val contextCarrier = new ContextCarrier
    var next           = contextCarrier.items
    while (next.hasNext) {
      next = next.next
      val contextValue = headers.get(Metadata.Key.of(next.getHeadKey, Metadata.ASCII_STRING_MARSHALLER))
      if (!StringUtil.isEmpty(contextValue)) next.setHeadValue(contextValue)
    }
    val span = ContextManager.createEntrySpan(
      OperationNameFormatUtils.formatOperationName(call.getMethodDescriptor),
      contextCarrier
    )
    span.setComponent(ComponentsDefine.GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    val contextSnapshot = ContextManager.capture
    val asyncSpan       = span.prepareForAsync
    val context         = Context.current.withValues(CONTEXT_SNAPSHOT_KEY, contextSnapshot, ACTIVE_SPAN_KEY, asyncSpan)
    val listener = Contexts.interceptCall(
      context,
      new TracingServerCall(call),
      headers,
      (serverCall, metadata) =>
        new TracingServerCallListener(handler.startCall(serverCall, metadata), serverCall.getMethodDescriptor)
    )
    ContextManager.stopSpan(asyncSpan)
    listener
  end interceptCall

end NativeGrpcServerInterceptor

object NativeGrpcServerInterceptor:
  final val REQUEST_ON_CANCEL_OPERATION_NAME: String     = "/Request/onCancel"
  final val REQUEST_ON_MESSAGE_OPERATION_NAME: String    = "/Request/onMessage"
  final val REQUEST_ON_HALF_CLOSE_OPERATION_NAME: String = "/Request/onHalfClose"
  final val RESPONSE_ON_CLOSE_OPERATION_NAME: String     = "/Response/onClose"
  final val RESPONSE_ON_MESSAGE_OPERATION_NAME: String   = "/Response/onMessage"
  final val SERVER: String                               = "/server"

  final val CONTEXT_SNAPSHOT_KEY: Context.Key[ContextSnapshot] = Context.key("skywalking-grpc-context-snapshot")
  final val ACTIVE_SPAN_KEY: Context.Key[AbstractSpan]         = Context.key("skywalking-grpc-active-span")

  final class TracingServerCall[REQUEST, RESPONSE](delegate: ServerCall[REQUEST, RESPONSE])
      extends ForwardingServerCall.SimpleForwardingServerCall[REQUEST, RESPONSE](delegate):

    private val operationPrefix = OperationNameFormatUtils.formatOperationName(delegate.getMethodDescriptor) + SERVER

    override def sendMessage(message: RESPONSE): Unit =
      if (!getMethodDescriptor.getType.serverSendsOneMessage) {
        val span = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_MESSAGE_OPERATION_NAME)
        span.setComponent(ComponentsDefine.GRPC)
        span.setLayer(SpanLayer.RPC_FRAMEWORK)
        ContextManager.continued(CONTEXT_SNAPSHOT_KEY.get)
        try super.sendMessage(message)
        catch {
          case t: Throwable =>
            ContextManager.activeSpan.log(t)
            throw t
        } finally ContextManager.stopSpan()
      } else super.sendMessage(message)

    override def close(status: Status, trailers: Metadata): Unit =
      val span = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_CLOSE_OPERATION_NAME)
      span.setComponent(ComponentsDefine.GRPC)
      span.setLayer(SpanLayer.RPC_FRAMEWORK)
      ContextManager.continued(CONTEXT_SNAPSHOT_KEY.get)
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
      ACTIVE_SPAN_KEY.get.asyncFinish
      try super.close(status, trailers)
      catch {
        case t: Throwable =>
          ContextManager.activeSpan.log(t)
          throw t
      } finally ContextManager.stopSpan()

  end TracingServerCall

  final class TracingServerCallListener[REQUEST](
    delegate: ServerCall.Listener[REQUEST],
    descriptor: MethodDescriptor[REQUEST, ?]
  ) extends ForwardingServerCallListener.SimpleForwardingServerCallListener[REQUEST](delegate):
    private val methodType      = descriptor.getType
    private val operationPrefix = OperationNameFormatUtils.formatOperationName(descriptor) + SERVER

    import NativeGrpcServerInterceptor.*

    override def onMessage(message: REQUEST): Unit =
      if (!methodType.clientSendsOneMessage) {
        val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_MESSAGE_OPERATION_NAME)
        span.setComponent(ComponentsDefine.GRPC)
        span.setLayer(SpanLayer.RPC_FRAMEWORK)
        ContextManager.continued(CONTEXT_SNAPSHOT_KEY.get)
        try super.onMessage(message)
        catch {
          case t: Throwable =>
            ContextManager.activeSpan.log(t)
            throw t
        } finally ContextManager.stopSpan()
      } else super.onMessage(message)

    override def onCancel(): Unit =
      val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_CANCEL_OPERATION_NAME)
      span.setComponent(ComponentsDefine.GRPC)
      span.setLayer(SpanLayer.RPC_FRAMEWORK)
      ContextManager.continued(CONTEXT_SNAPSHOT_KEY.get)
      try super.onCancel()
      catch {
        case t: Throwable =>
          ContextManager.activeSpan.log(t)
          throw t
      } finally {
        ContextManager.stopSpan()
        ACTIVE_SPAN_KEY.get.asyncFinish
      }

    override def onHalfClose(): Unit =
      val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_HALF_CLOSE_OPERATION_NAME)
      span.setComponent(ComponentsDefine.GRPC)
      span.setLayer(SpanLayer.RPC_FRAMEWORK)
      ContextManager.continued(CONTEXT_SNAPSHOT_KEY.get)
      try super.onHalfClose()
      catch {
        case t: Throwable =>
          ContextManager.activeSpan.log(t)
          throw t
      } finally ContextManager.stopSpan()

    override def onComplete(): Unit = super.onComplete()

    override def onReady(): Unit = super.onReady()

  end TracingServerCallListener

end NativeGrpcServerInterceptor
