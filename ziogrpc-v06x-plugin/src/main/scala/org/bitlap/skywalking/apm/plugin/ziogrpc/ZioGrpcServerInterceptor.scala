package org.bitlap.skywalking.apm.plugin.ziogrpc

import java.lang.reflect.Method

import scalapb.zio_grpc.*
import scalapb.zio_grpc.server.*

import io.grpc.*
import io.grpc.ServerCall.Listener
import io.grpc.Status.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.language.agent.v3.MeterData
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.apache.skywalking.apm.util.StringUtil
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.forward.TracingServerCallListener

import Constants.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
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
    val context = OperationContext(contextSnapshot, span, call.getMethodDescriptor)
    GrpcOperationQueue.offer(OperationNameFormatUtils.formatOperationName(call.getMethodDescriptor), context)
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
    if context == null then return ret
    val call            = allArguments(0).asInstanceOf[ServerCall[Any, Any]]
    val ctx             = context.asInstanceOf[OperationContext]
    val contextSnapshot = ctx.contextSnapshot
    val asyncSpan       = ctx.asyncSpan
    val listener        = ret.asInstanceOf[Listener[Any]]

    val result = new TracingServerCallListener(
      listener,
      call.getMethodDescriptor,
      contextSnapshot,
      asyncSpan
    )
    ContextManager.stopSpan()
    result
  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = {}

end ZioGrpcServerInterceptor
