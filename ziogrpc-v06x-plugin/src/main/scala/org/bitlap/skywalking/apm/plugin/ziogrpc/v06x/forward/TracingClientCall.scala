package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.forward

import scala.collection.mutable.ListBuffer
import scalapb.zio_grpc.SafeMetadata
import scalapb.zio_grpc.client.ZClientCall

import io.grpc.*
import io.grpc.ClientCall.Listener

import zio.ZIO

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.InterceptorUtils
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.OperationNameFormatUtils

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/14
 */
final class TracingClientCall[R, REQUEST, RESPONSE](
  delegate: ZClientCall[R, REQUEST, RESPONSE],
  method: MethodDescriptor[REQUEST, RESPONSE]
) extends ZClientCall[R, REQUEST, RESPONSE]:

  private var snapshot: ContextSnapshot = _
  private val serviceName               = OperationNameFormatUtils.formatOperationName(method)
  private val operationPrefix           = OperationNameFormatUtils.formatOperationName(method) + CLIENT

  override def start(
    responseListener: Listener[RESPONSE],
    headers: SafeMetadata
  ): ZIO[R, Status, Unit] = {
    val contextCarrier = new ContextCarrier
    val span           = ContextManager.createExitSpan(serviceName, "No Peer")
    span.setComponent(ComponentsDefine.GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    ContextManager.inject(contextCarrier)
    var contextItem = contextCarrier.items
    val unsafePut   = ListBuffer[(Metadata.Key[String], String)]()
    while (contextItem.hasNext) {
      contextItem = contextItem.next
      val headerKey = Metadata.Key.of(contextItem.getHeadKey, Metadata.ASCII_STRING_MARSHALLER)
      unsafePut.append(headerKey -> contextItem.getHeadValue)
    }
    val putInto = unsafePut.result().map(kv => headers.put(kv._1, kv._2))

    InterceptorUtils.unsafeRunZIO(ZIO.collectAll(putInto))

    snapshot = ContextManager.capture
    span.prepareForAsync()
    ContextManager.stopSpan(span)
    delegate.start(new TracingClientCallListener(responseListener, method, snapshot, span), headers)
  }

  override def request(numMessages: Int): ZIO[R, Status, Unit] = delegate.request(numMessages)

  override def sendMessage(message: REQUEST): ZIO[R, Status, Unit] = {
    if (method.getType.clientSendsOneMessage) {
      return delegate.sendMessage(message)
    }

    val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_MESSAGE_OPERATION_NAME)
    span.setComponent(ComponentsDefine.GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    try
      ContextManager.continued(snapshot)
      delegate.sendMessage(message)
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
        throw t
    } finally ContextManager.stopSpan()
  }

  override def halfClose(): ZIO[R, Status, Unit] = {
    val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_COMPLETE_OPERATION_NAME)
    span.setComponent(ComponentsDefine.GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    try
      ContextManager.continued(snapshot)
      delegate.halfClose()
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
        throw t
    } finally ContextManager.stopSpan()
  }

  override def cancel(message: String): ZIO[R, Status, Unit] = {
    val span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_CANCEL_OPERATION_NAME)
    span.setComponent(ComponentsDefine.GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    try
      ContextManager.continued(snapshot)
      delegate.cancel(message)
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
        throw t
    } finally ContextManager.stopSpan()
  }
