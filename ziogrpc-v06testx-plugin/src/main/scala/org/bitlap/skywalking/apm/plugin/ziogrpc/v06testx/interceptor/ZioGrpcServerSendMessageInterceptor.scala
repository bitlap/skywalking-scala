package org.bitlap.skywalking.apm.plugin.ziogrpc.v06testx.interceptor

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
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06testx.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06testx.Constants.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/15
 */
final class ZioGrpcServerSendMessageInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    val call    = allArguments(0).asInstanceOf[ServerCall[?, ?]]
    val context = GrpcOperationQueue.get(call)
    if context == null then return
    val method = context.methodDescriptor
    val span   = beforeSendMessage(ContextManager.capture(), method)
    span.foreach(a => objInst.setSkyWalkingDynamicField(a))
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
    Utils.continuedSnapshot_(contextSnapshot)
    Some(span)
  end beforeSendMessage

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    val span = objInst.getSkyWalkingDynamicField
    if span == null || !span.isInstanceOf[AbstractSpan] then return ret
    ret
      .asInstanceOf[GIO[Unit]]
      .ensuring(ZIO.attempt(ContextManager.stopSpan(span.asInstanceOf[AbstractSpan])).ignore)
  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = Utils.logError(t)

end ZioGrpcServerSendMessageInterceptor