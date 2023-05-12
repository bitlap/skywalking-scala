package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

import java.lang.reflect.Method

import scala.util.Try

import io.grpc.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.{ AbstractSpan, SpanLayer }
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class ZioGrpcClientInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    result: MethodInterceptResult
  ): Unit = {
    val methodDescriptor = allArguments(1).asInstanceOf[MethodDescriptor[_, _]]
    val serviceName      = OperationNameFormatUtil.formatOperationName(methodDescriptor)
    val remotePeer       = "No Peer"
    val span             = ContextManager.createExitSpan(serviceName + "/client", remotePeer)
    span.setComponent(ComponentsDefine.GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
  }

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    ret: Object
  ): Object = {
    val result = ret.asInstanceOf[ZIO[_, Status, _]]
    result
      .mapError(s => s.asException())
      .catchAllCause(c => ZIO.attempt(dealException(c.squash)) *> ZIO.done(Exit.Failure(c)))
      .mapError(t => Status.fromThrowable(t))
      .ensuring(
        ZIO.attempt {
          ContextManager.activeSpan().asyncFinish()
          ContextManager.stopSpan()
        }
          .catchAllCause(t => ZIO.attempt(dealException(t.squash)).ignore)
      )
  }

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    t: Throwable
  ): Unit =
    dealException(t)

  private def dealException(t: Throwable): Unit =
    ContextManager.activeSpan.errorOccurred.log(t)
