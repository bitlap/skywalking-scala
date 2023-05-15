package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

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
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.forward.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
final class ZioGrpcServerInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    result: MethodInterceptResult
  ): Unit =
    val call           = allArguments(0).asInstanceOf[ServerCall[?, ?]]
    val headers        = allArguments(1).asInstanceOf[Metadata]
    val contextCarrier = new ContextCarrier
    var next           = contextCarrier.items
    while (next.hasNext) {
      next = next.next
      val contextValue = headers.get(Metadata.Key.of(next.getHeadKey, Metadata.ASCII_STRING_MARSHALLER))
      if (!StringUtil.isEmpty(contextValue)) next.setHeadValue(contextValue)
    }
    val span = ContextManager.createEntrySpan(
      OperationNameFormatUtils.formatOperationName(call.getMethodDescriptor),
      contextCarrier
    )
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)

    val contextSnapshot = ContextManager.capture
    span.prepareForAsync()
    ZioGrpcContext.offer(ZioGrpcContext(contextSnapshot, span, call.getMethodDescriptor))
    ContextManager.stopSpan(span)

  end beforeMethod

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    ret: Object
  ): Object =
    if (ZioGrpcContext.peek == null)
      return ret
    val call            = allArguments(0).asInstanceOf[ServerCall[Any, Any]]
    val context         = ZioGrpcContext.peek
    val contextSnapshot = context.contextSnapshot
    val asyncSpan       = context.asyncSpan
    val listener        = ret.asInstanceOf[Listener[Any]]
    new TracingServerCallListener(
      listener,
      call.getMethodDescriptor,
      contextSnapshot,
      asyncSpan
    )
  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    t: Throwable
  ): Unit = InterceptorDSL.handleMethodException(objInst, allArguments, t)(_ => ())

end ZioGrpcServerInterceptor
