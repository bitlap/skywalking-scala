package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor

import java.lang.reflect.Method

import scala.util.*
import scalapb.zio_grpc.*

import io.grpc.*
import io.grpc.Status.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.Constants.*

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
    val ctx = objInst.getSkyWalkingDynamicField
    if ctx == null || !ctx.isInstanceOf[OperationContext] then return

    val cx      = ctx.asInstanceOf[OperationContext]
    val context = GrpcOperationQueue.remove(cx.selfCall)
    if context == null then return

    val method = context.methodDescriptor
    val span   = beforeClose(ContextManager.capture(), method)
    objInst.setSkyWalkingDynamicField(context.copy(activeSpan = Option(span)))
  end beforeMethod

  private def beforeClose(contextSnapshot: ContextSnapshot, method: MethodDescriptor[?, ?]): AbstractSpan =
    val operationPrefix = OperationNameFormatUtils.formatOperationName(method) + SERVER
    val span            = ContextManager.createLocalSpan(operationPrefix + RESPONSE_ON_CLOSE_OPERATION_NAME)
    span.setComponent(ZIO_GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
    ContextManager.stopSpan(span)
    Utils.continuedSnapshot_(contextSnapshot)
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
    val span = ctx.activeSpan.orNull
    if span == null || !span.isInstanceOf[AbstractSpan] then return ret

    val status = allArguments(1).asInstanceOf[Status]
    ret
      .asInstanceOf[GIO[Unit]]
      .ensuring(ZIO.attempt(afterClose(status, ctx.asyncSpan, span)).ignore)
  end afterMethod

  private def afterClose(status: Status, asyncSpan: AbstractSpan, span: AbstractSpan): Unit =
    status match {
      case OK      =>
      case UNKNOWN =>
      case INTERNAL =>
        if status.getCause == null then span.log(status.asRuntimeException)
        else span.log(status.getCause)
      case _ =>
        if status.getCause != null then span.log(status.getCause)
    }
    Tags.RPC_RESPONSE_STATUS_CODE.set(span, status.getCode.name)
    try
      span.asyncFinish
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
    } finally
      try
        asyncSpan.asyncFinish()
      catch {
        case ignore: Throwable =>
      }

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = Utils.logError(t)

end ZioGrpcServerCloseInterceptor
