package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx

import java.lang.reflect.*

import scala.collection.mutable.ListBuffer
import scalapb.zio_grpc.SafeMetadata
import scalapb.zio_grpc.client.*

import io.grpc.*
import io.grpc.ClientCall.Listener

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.util.StringUtil
import org.bitlap.skywalking.apm.plugin.common.AgentUtils
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.listener.*

final class TracingClientCall[Req, Resp](
  peer: Option[String],
  delegate: ZClientCall[Req, Resp],
  method: MethodDescriptor[Req, Resp]
) extends ZClientCall[Req, Resp]:

  private var snapshot: ContextSnapshot = _
  private val serviceName               = OperationNameFormatUtils.formatOperationName(method)
  private val operationPrefix           = serviceName + CLIENT
  private val DEFAULT_PEER              = "No Peer"

  private def getRemotePeerIp: String = {
    var call: Field               = null
    var attMethod: Option[Method] = None

    try {
      call = delegate.getClass.getDeclaredField("call")
      call.setAccessible(true)
      val callImpl = call.get(delegate).asInstanceOf[ClientCall[?, ?]]
      attMethod = callImpl.getClass.getDeclaredMethods.find(_.getName == "getAttributes")
      attMethod.foreach(_.setAccessible(true))
      val attributes = attMethod.map(_.invoke(callImpl).asInstanceOf[Attributes])

      val realRemoteIp = attributes.map(_.get[String](Attributes.Key.create("remote-addr"))).orNull
      if StringUtil.isNotBlank(realRemoteIp) then {
        if realRemoteIp.startsWith("/") then realRemoteIp.substring(1) else realRemoteIp
      } else {
        peer.getOrElse(DEFAULT_PEER)
      }

    } catch {
      case ignore: Throwable =>
        DEFAULT_PEER
    } finally {
      call.setAccessible(false)
      attMethod.foreach(_.setAccessible(false))
    }
  }

  override def start(
    responseListener: Listener[Resp],
    headers: SafeMetadata
  ): IO[StatusException, Unit] =
    val contextCarrier = new ContextCarrier
    val remotePeer     = getRemotePeerIp
    val span           = ContextManager.createExitSpan(serviceName, remotePeer)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    ContextManager.inject(contextCarrier)
    var contextItem = contextCarrier.items
    val unsafePut   = ListBuffer[(Metadata.Key[String], String)]()
    while contextItem.hasNext do {
      contextItem = contextItem.next
      val headerKey = Metadata.Key.of(contextItem.getHeadKey, Metadata.ASCII_STRING_MARSHALLER)
      unsafePut.append(headerKey -> contextItem.getHeadValue)
    }
    val putInto    = unsafePut.result().map(kv => headers.put(kv._1, kv._2))
    val setHeaders = ZIO.collectAll(putInto).ignoreLogged

    // ZioUtils.unsafeRun(ZIO.collectAll(putInto))

    snapshot = ContextManager.capture
    span.prepareForAsync()
    setHeaders *> delegate
      .start(new TracingClientCallListener(responseListener, method, snapshot, span), headers)
      .ensuring(ZIO.attempt(ContextManager.stopSpan()).ignoreLogged)
  end start

  override def request(numMessages: Int): IO[StatusException, Unit] = delegate.request(numMessages)

  override def sendMessage(message: Req): IO[StatusException, Unit] =
    if method.getType.clientSendsOneMessage then {
      return delegate.sendMessage(message)
    }

    implicit val span: AbstractSpan =
      ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_MESSAGE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
    continuedSnapshotF(snapshot)(delegate.sendMessage(message))
  end sendMessage

  override def halfClose(): IO[StatusException, Unit] =
    implicit val span: AbstractSpan =
      ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_COMPLETE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
    continuedSnapshotF(snapshot)(delegate.halfClose())
  end halfClose

  override def cancel(message: String): IO[StatusException, Unit] =
    implicit val span: AbstractSpan = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_CANCEL_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
    continuedSnapshotF(snapshot)(delegate.cancel(message))

  end cancel

  private def continuedSnapshotF(contextSnapshot: ContextSnapshot)(
    effect: => IO[StatusException, Unit]
  )(using AbstractSpan): IO[StatusException, Unit] =
    AgentUtils.continuedSnapshot_(contextSnapshot)
    val result = effect.mapError { a =>
      ContextManager.activeSpan.log(a)
      a
    }.ensuring(ZIO.attempt {
      AgentUtils.stopAsync(summon[AbstractSpan])
      ContextManager.stopSpan()
    }.ignoreLogged)

    result.mapError(e => new StatusException(Status.fromThrowable(e)))

end TracingClientCall
