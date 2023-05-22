package org.bitlap.skywalking.apm.plugin.ziogrpc

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
    val context = InterceptorSendMessageThreadContext.poll(
      OperationNameFormatUtils.formatOperationName(allArguments(0).asInstanceOf[ServerCall[?, ?]].getMethodDescriptor)
    )
    if context == null then return
    val contextSnapshot = context.contextSnapshot
    val method          = context.methodDescriptor
    val span            = ChannelActions.beforeSendMessage(contextSnapshot, method)
    if span != null then {
      span.prepareForAsync()
      objInst.setSkyWalkingDynamicField(span)
    }
  end beforeMethod

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
  ): Unit = {}

end ZioGrpcServerSendMessageInterceptor
