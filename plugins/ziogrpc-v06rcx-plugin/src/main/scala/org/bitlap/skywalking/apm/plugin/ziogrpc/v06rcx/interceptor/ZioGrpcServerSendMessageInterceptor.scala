package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor

import java.lang.reflect.Method

import scalapb.zio_grpc.*

import io.grpc.*

import zio.ZIO

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.Constants.*

final class ZioGrpcServerSendMessageInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    val ctx = objInst.getSkyWalkingDynamicField
    if ctx == null || !ctx.isInstanceOf[OperationContext] then return

    val cx      = ctx.asInstanceOf[OperationContext]
    val context = OperationContext.get(cx.selfCall)
    if context == null then return
    val span = beforeSendMessage(context.contextSnapshot, context.methodDescriptor)
    span.foreach(s => objInst.setSkyWalkingDynamicField(context.copy(activeSpan = s)))
  end beforeMethod

  private def beforeSendMessage(
    contextSnapshot: ContextSnapshot,
    method: MethodDescriptor[?, ?]
  ): Option[AbstractSpan] =
    if method.getType.serverSendsOneMessage then {
      return None
    }
    val operationPrefix = OperationNameFormatUtils.formatOperationName(method) + SERVER
    val span            = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_MESSAGE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
    AgentUtils.continuedSnapshot_(contextSnapshot)
    Some(span)
  end beforeSendMessage

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    val context = objInst.getSkyWalkingDynamicField
    if context == null || !context.isInstanceOf[OperationContext] then return ret

    val ctx = context.asInstanceOf[OperationContext]

    if ctx.activeSpan == null then return ret

    ret.asInstanceOf[GIO[Unit]].ensuring(ZIO.attempt { ctx.activeSpan.asyncFinish(); ContextManager.stopSpan() }.ignore)
  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = if ContextManager.isActive then ContextManager.activeSpan.log(t)

end ZioGrpcServerSendMessageInterceptor
