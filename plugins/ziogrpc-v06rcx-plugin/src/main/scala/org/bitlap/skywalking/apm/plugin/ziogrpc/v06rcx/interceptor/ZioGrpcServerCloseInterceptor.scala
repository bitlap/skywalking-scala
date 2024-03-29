package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor

import java.lang.reflect.Method

import io.grpc.*
import io.grpc.Status.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.*

final class ZioGrpcServerCloseInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    val call    = objInst.asInstanceOf[ServerCall[?, ?]]
    val context = OperationContext.remove(call)
    if context == null then return

    val span = beforeClose(context.contextSnapshot, context.methodDescriptor)
    objInst.setSkyWalkingDynamicField(context.copy(activeSpan = span))
  end beforeMethod

  private def beforeClose(contextSnapshot: ContextSnapshot, method: MethodDescriptor[?, ?]): AbstractSpan =
    val operationPrefix = OperationNameFormatUtils.formatOperationName(method) + SERVER
    val span            = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_CLOSE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
    AgentUtils.continuedSnapshot_(contextSnapshot)
    span

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    val context = objInst.getSkyWalkingDynamicField
    if context == null then return ret

    val ctx  = context.asInstanceOf[OperationContext]
    val span = ctx.activeSpan
    if span == null || !span.isInstanceOf[AbstractSpan] then return ret

    val status = allArguments(0).asInstanceOf[Status]
    afterClose(status, ctx.asyncSpan, span)
    ret
  end afterMethod

  private def afterClose(status: Status, asyncSpan: AbstractSpan, span: AbstractSpan): Unit =
    status match {
      case UNKNOWN | INTERNAL =>
        if status.getCause == null then span.log(status.asRuntimeException)
        else span.log(status.getCause)
      case _ =>
        if status.getCause != null then span.log(status.getCause)
    }
    Tags.RPC_RESPONSE_STATUS_CODE.set(span, status.getCode.name)
    AgentUtils.stopAsync(span)
    AgentUtils.stopAsync(asyncSpan)
    ContextManager.stopSpan()

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = if ContextManager.isActive then ContextManager.activeSpan.log(t)

end ZioGrpcServerCloseInterceptor
