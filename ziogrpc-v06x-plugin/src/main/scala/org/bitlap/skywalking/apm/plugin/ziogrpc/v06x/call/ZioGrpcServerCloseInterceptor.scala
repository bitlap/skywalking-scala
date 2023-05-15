package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.call

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
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.{ InterceptorCloseThreadContext, InterceptorThreadContext }

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/15
 */
final class ZioGrpcServerCloseInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    result: MethodInterceptResult
  ): Unit =
    val context = InterceptorCloseThreadContext.poll
    if (context == null) return
    val contextSnapshot = context.contextSnapshot
    val method          = context.methodDescriptor
    val span            = ChannelActions.beforeClose(contextSnapshot, method)
    objInst.setSkyWalkingDynamicField(context.copy(activeSpan = Option(span)))
  end beforeMethod

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    ret: Object
  ): Object =
    val context = objInst.getSkyWalkingDynamicField
    if (context == null) return ret
    val ctx  = context.asInstanceOf[InterceptorThreadContext]
    val span = ctx.activeSpan.orNull
    if (span == null || !span.isInstanceOf[AbstractSpan]) return ret
    val status = allArguments(1).asInstanceOf[Status]
    ret
      .asInstanceOf[GIO[Unit]]
      .ensuring(ZIO.attempt(ChannelActions.afterClose(status, ctx.asyncSpan, span)(())).ignore)
  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    t: Throwable
  ): Unit = {}

end ZioGrpcServerCloseInterceptor
