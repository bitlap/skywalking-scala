package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.call

import io.grpc.*
import io.grpc.ServerCall.Listener
import io.grpc.Status.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.InterceptorDSL
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.OperationNameFormatUtils

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
object ChannelActions:

  def beforeSendMessage(contextSnapshot: ContextSnapshot, method: MethodDescriptor[?, ?]): AbstractSpan =
    if (method.getType.serverSendsOneMessage) {
      return null
    }
    val operationPrefix = OperationNameFormatUtils.formatOperationName(method) + SERVER
    val span            = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_MESSAGE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    InterceptorDSL.continuedSnapshot(contextSnapshot)(())
    span
  end beforeSendMessage

  def beforeClose(contextSnapshot: ContextSnapshot, method: MethodDescriptor[?, ?]): AbstractSpan =
    val operationPrefix = OperationNameFormatUtils.formatOperationName(method) + SERVER
    val span            = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_CLOSE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    InterceptorDSL.continuedSnapshot(contextSnapshot)(())
    span

  def afterClose(status: Status, asyncSpan: AbstractSpan, span: AbstractSpan)(action: => Unit): Unit =
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
      action
      asyncSpan.asyncFinish
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
    } finally ContextManager.stopSpan()

end ChannelActions
