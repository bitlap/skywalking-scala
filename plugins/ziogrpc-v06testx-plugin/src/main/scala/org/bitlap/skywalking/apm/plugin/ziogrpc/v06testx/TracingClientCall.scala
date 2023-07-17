package org.bitlap.skywalking.apm.plugin.ziogrpc.v06testx

import java.lang.reflect.*

import scala.collection.mutable.ListBuffer
import scalapb.zio_grpc.SafeMetadata
import scalapb.zio_grpc.client.*

import io.grpc.*
import io.grpc.ClientCall.Listener

import zio.ZIO

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.apache.skywalking.apm.util.StringUtil
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.common.AgentUtils
import org.bitlap.skywalking.apm.plugin.zcommon.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.listener.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/14
 */
final class TracingClientCall[R, Req, Resp](
  peer: Option[String],
  delegate: ZClientCall[R, Req, Resp],
  method: MethodDescriptor[Req, Resp]
) extends ZClientCall[R, Req, Resp]:

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
  ): ZIO[R, Status, Unit] =
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
    val setHeaders = ZIO.collectAll(putInto).ignore

    // ZioUtils.unsafeRun(ZIO.collectAll(putInto))

    snapshot = ContextManager.capture
    span.prepareForAsync()
    setHeaders *> delegate
      .start(new TracingClientCallListener(responseListener, method, snapshot, span), headers)
      .ensuring(ZIO.attempt(ContextManager.stopSpan()).ignore)
  end start

  override def request(numMessages: Int): ZIO[R, Status, Unit] = delegate.request(numMessages)

  override def sendMessage(message: Req): ZIO[R, Status, Unit] =
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
    AgentUtils.continuedSnapshot_(contextSnapshot)
    val result = effect.mapError { a =>
      ContextManager.activeSpan.log(a.asException())
      a.asException()
    }.ensuring(ZIO.attempt {
      AgentUtils.stopAsync(summon[AbstractSpan])
      ContextManager.stopSpan()
    }.ignore)

    result.mapError(e => Status.fromThrowable(e))

end TracingClientCall
