package org.bitlap.skywalking.apm.plugin.ziogrpc

import java.lang.reflect.Method

import scala.util.*
import scalapb.zio_grpc.*

import io.grpc.*

import zio.*

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
final class ZioGrpcServerCloseInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit =
    val context = GrpcOperationQueue.poll(
      OperationNameFormatUtils.formatOperationName(allArguments(0).asInstanceOf[ServerCall[?, ?]].getMethodDescriptor)
    )
    if context == null then return
    val contextSnapshot = context.contextSnapshot
    val method          = context.methodDescriptor
    val span            = ChannelActions.beforeClose(contextSnapshot, method)
    objInst.setSkyWalkingDynamicField(context.copy(activeSpan = Option(span)))
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
    val ctx  = context.asInstanceOf[OperationContext]
    val span = ctx.activeSpan.orNull
    if span == null || !span.isInstanceOf[AbstractSpan] then return ret
    val status = allArguments(1).asInstanceOf[Status]
    ret
      .asInstanceOf[GIO[Unit]]
      .ensuring(ZIO.attempt(ChannelActions.afterClose(status, ctx.asyncSpan, span)).ignore)
  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = {}

end ZioGrpcServerCloseInterceptor
