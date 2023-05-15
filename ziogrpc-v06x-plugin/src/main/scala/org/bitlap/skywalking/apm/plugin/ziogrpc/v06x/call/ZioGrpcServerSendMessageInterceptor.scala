package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.call

import java.lang.reflect.Method

import scalapb.zio_grpc.*

import io.grpc.*

import zio.ZIO

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.{ InterceptorSendMessageThreadContext, InterceptorThreadContext }

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/15
 */
final class ZioGrpcServerSendMessageInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    result: MethodInterceptResult
  ): Unit =
    val context = InterceptorSendMessageThreadContext.poll
    if (context == null) return
    val contextSnapshot = context.contextSnapshot
    val method          = context.methodDescriptor
    val span            = ChannelActions.beforeSendMessage(contextSnapshot, method)
    objInst.setSkyWalkingDynamicField(span)
  end beforeMethod

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    ret: Object
  ): Object =
    val span = objInst.getSkyWalkingDynamicField
    if (span == null || !span.isInstanceOf[AbstractSpan]) return ret
    ret
      .asInstanceOf[GIO[Unit]]
      .ensuring(ZIO.attempt(ContextManager.stopSpan(span.asInstanceOf[AbstractSpan])).ignore)
  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    t: Throwable
  ): Unit = {}

end ZioGrpcServerSendMessageInterceptor
