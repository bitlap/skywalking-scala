package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

import java.lang.reflect.Method

import scala.collection.mutable.ListBuffer
import scala.util.Try
import scalapb.zio_grpc.SafeMetadata

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
    val safeMetadata     = allArguments(3).asInstanceOf[SafeMetadata]
    val serviceName      = OperationNameFormatUtils.formatOperationName(methodDescriptor)
    val remotePeer       = "No Peer"
    val contextCarrier   = new ContextCarrier
    val span             = ContextManager.createExitSpan(serviceName + "/client", contextCarrier, remotePeer)
    val items            = ListBuffer[(Metadata.Key[String], String)]()
    var contextItem      = contextCarrier.items
    while (contextItem.hasNext) {
      contextItem = contextItem.next
      val headerKey = Metadata.Key.of(contextItem.getHeadKey, Metadata.ASCII_STRING_MARSHALLER)
      items.append(headerKey -> contextItem.getHeadValue)
    }
    val updateMetadata = ZIO.foreach(items.result())(kv => safeMetadata.put(kv._1, kv._2))
    InterceptorUtils.unsafeRunZIO(updateMetadata)

    span.setComponent(ComponentsDefine.GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)
    span.prepareForAsync()
    objInst.setSkyWalkingDynamicField(span)
  }

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    ret: Object
  ): Object =
    InterceptorUtils.handleMethodExit(objInst, ret) { ret =>
      ret
        .asInstanceOf[ZIO[_, Status, _]]
        .mapError(s => s.asException())
        .catchAllCause(c => InterceptorUtils.dealExceptionF(c.squash) *> ZIO.done(Exit.Failure(c)))
        .mapError(t => Status.fromThrowable(t))
    }

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    t: Throwable
  ): Unit =
    InterceptorUtils.handleMethodException(objInst, allArguments, t)(_ => ())

end ZioGrpcClientInterceptor
