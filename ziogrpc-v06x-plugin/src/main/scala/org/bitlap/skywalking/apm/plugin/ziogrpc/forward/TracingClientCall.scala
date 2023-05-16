package org.bitlap.skywalking.apm.plugin.ziogrpc.forward

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
import org.bitlap.skywalking.apm.plugin.common.InterceptorDSL
import org.bitlap.skywalking.apm.plugin.ziogrpc.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.OperationNameFormatUtils

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/14
 */
final class TracingClientCall[R, REQUEST, RESPONSE](
  peer: Option[String],
  delegate: ZClientCall[R, REQUEST, RESPONSE],
  method: MethodDescriptor[REQUEST, RESPONSE]
) extends ZClientCall[R, REQUEST, RESPONSE]:

  private var snapshot: ContextSnapshot = _
  private val serviceName               = OperationNameFormatUtils.formatOperationName(method)
  private val operationPrefix           = serviceName + CLIENT

  override def start(
    responseListener: Listener[RESPONSE],
    headers: SafeMetadata
  ): ZIO[R, Status, Unit] =
    val contextCarrier = new ContextCarrier
    val span           = ContextManager.createExitSpan(serviceName, peer.getOrElse("No Peer"))
    span.setComponent(ZIO_GRPC)
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

    InterceptorDSL.unsafeRun(ZIO.collectAll(putInto))

    snapshot = ContextManager.capture
    span.prepareForAsync()
    ContextManager.stopSpan(span)
    delegate.start(new TracingClientCallListener(responseListener, method, snapshot, span), headers)
  end start

  override def request(numMessages: Int): ZIO[R, Status, Unit] = delegate.request(numMessages)

  override def sendMessage(message: REQUEST): ZIO[R, Status, Unit] =
    if (method.getType.clientSendsOneMessage) {
      return delegate.sendMessage(message)
    }

    implicit val span: AbstractSpan =
      ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_MESSAGE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
    continuedSnapshotF(snapshot)(delegate.sendMessage(message))
  end sendMessage

  override def halfClose(): ZIO[R, Status, Unit] =
    implicit val span: AbstractSpan =
      ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_COMPLETE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
    continuedSnapshotF(snapshot)(delegate.halfClose())
  end halfClose

  override def cancel(message: String): ZIO[R, Status, Unit] =
    implicit val span: AbstractSpan = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_CANCEL_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
    continuedSnapshotF(snapshot)(delegate.cancel(message))

  end cancel

  private def continuedSnapshotF[A](contextSnapshot: ContextSnapshot)(
    effect: => ZIO[A, Status, Unit]
  )(using AbstractSpan): ZIO[A, Status, Unit] =
    ContextManager.continued(contextSnapshot)
    val result = effect.mapError { a =>
      ContextManager.activeSpan.log(a.asException())
      a.asException()
    }.ensuring(ZIO.attempt {
      summon[AbstractSpan].asyncFinish()
    }.ignore)

    ContextManager.stopSpan()

    result.mapError(e => Status.fromThrowable(e))

end TracingClientCall
