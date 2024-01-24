package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor

import java.lang.reflect.Method

import io.grpc.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
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
    val call    = objInst.asInstanceOf[ServerCall[?, ?]]
    val context = OperationContext.get(call)
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
    AgentUtils.stopAsync(ctx.activeSpan)
    ContextManager.stopSpan()
    ret

  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = if ContextManager.isActive then ContextManager.activeSpan.log(t)

end ZioGrpcServerSendMessageInterceptor
