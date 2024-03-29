package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor

import java.lang.reflect.Method

import scalapb.zio_grpc.*

import io.grpc.*
import io.grpc.ServerCall.Listener
import io.grpc.Status.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.util.StringUtil
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.listener.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.*

final class ZioGrpcServerInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    val call           = allArguments(0).asInstanceOf[ServerCall[?, ?]]
    val headers        = allArguments(1).asInstanceOf[Metadata]
    val contextCarrier = new ContextCarrier
    var next           = contextCarrier.items
    while next.hasNext do {
      next = next.next
      val contextValue = headers.get(Metadata.Key.of(next.getHeadKey, Metadata.ASCII_STRING_MARSHALLER))
      if !StringUtil.isEmpty(contextValue) then next.setHeadValue(contextValue)
    }
    val span = ContextManager.createEntrySpan(
      OperationNameFormatUtils.formatOperationName(call.getMethodDescriptor),
      contextCarrier
    )
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)

    val contextSnapshot = ContextManager.capture
    span.prepareForAsync()
    val context = OperationContext(
      selfCall = call,
      asyncSpan = span,
      contextSnapshot = contextSnapshot,
      methodDescriptor = call.getMethodDescriptor
    )
    OperationContext.put(call, context)
    objInst.setSkyWalkingDynamicField(context)

  end beforeMethod

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    val context = objInst.getSkyWalkingDynamicField
    if context == null || !context.isInstanceOf[OperationContext] then return ret
    val call     = allArguments(0).asInstanceOf[ServerCall[Any, Any]]
    val ctx      = context.asInstanceOf[OperationContext]
    val listener = ret.asInstanceOf[Listener[Any]]
    val result = new TracingServerCallListener(
      listener,
      call.getMethodDescriptor,
      ctx.contextSnapshot,
      ctx.asyncSpan
    )
    AgentUtils.stopIfActive()
    result
  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = if ContextManager.isActive then ContextManager.activeSpan.log(t)

end ZioGrpcServerInterceptor
