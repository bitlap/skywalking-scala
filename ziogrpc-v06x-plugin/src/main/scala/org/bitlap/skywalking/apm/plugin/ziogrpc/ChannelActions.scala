package org.bitlap.skywalking.apm.plugin.ziogrpc

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

import Constants.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
object ChannelActions:

  def beforeSendMessage(contextSnapshot: ContextSnapshot, method: MethodDescriptor[?, ?]): Option[AbstractSpan] =
    if method.getType.serverSendsOneMessage then {
      return None
    }
    val operationPrefix = OperationNameFormatUtils.formatOperationName(method) + SERVER
    val span            = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_MESSAGE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    InterceptorDSL.continuedSnapshot_(contextSnapshot)
    Some(span)
  end beforeSendMessage

  def beforeClose(contextSnapshot: ContextSnapshot, method: MethodDescriptor[?, ?]): AbstractSpan =
    val operationPrefix = OperationNameFormatUtils.formatOperationName(method) + SERVER
    val span            = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_CLOSE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
    ContextManager.stopSpan(span)
    InterceptorDSL.continuedSnapshot_(contextSnapshot)
    span

  def afterClose(status: Status, asyncSpan: AbstractSpan, span: AbstractSpan): Unit =
    status match {
      case OK      =>
      case UNKNOWN =>
      case INTERNAL =>
        if status.getCause == null then span.log(status.asRuntimeException)
        else span.log(status.getCause)
      case _ =>
        if status.getCause != null then span.log(status.getCause)
    }
    Tags.RPC_RESPONSE_STATUS_CODE.set(span, status.getCode.name)
    try
      span.asyncFinish
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
    } finally
      try
        asyncSpan.asyncFinish()
      catch {
        case ignore: Throwable =>
      }

end ChannelActions
