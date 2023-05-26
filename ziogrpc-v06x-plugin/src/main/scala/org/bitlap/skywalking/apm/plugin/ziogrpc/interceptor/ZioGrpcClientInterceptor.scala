package org.bitlap.skywalking.apm.plugin.ziogrpc.interceptor

import java.lang.reflect.Method

import scalapb.zio_grpc.*
import scalapb.zio_grpc.client.*

import io.grpc.*

import zio.*

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.ZioGrpcClientInterceptor
import org.bitlap.skywalking.apm.plugin.ziogrpc.forward.TracingClientCall

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class ZioGrpcClientInterceptor extends InstanceMethodsAroundInterceptor, InstanceConstructorInterceptor:
  import ZioGrpcClientInterceptor.*

  override def onConstruct(objInst: EnhancedInstance, allArguments: Array[Object]): Unit = {
    val channel = allArguments(0).asInstanceOf[ManagedChannel]
    objInst.setSkyWalkingDynamicField(channel.authority())
  }

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    result: MethodInterceptResult
  ): Unit = {}

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    ret: Object
  ): Object =
    val peer             = objInst.getSkyWalkingDynamicField.asInstanceOf[String]
    val interceptor      = new ZTraceClientInterceptor[Any](Option(peer))
    val methodDescriptor = allArguments(0).asInstanceOf[MethodDescriptor[Any, Any]]
    val options          = allArguments(1).asInstanceOf[CallOptions]
    val result           = ret.asInstanceOf[UIO[ZClientCall[Any, Any, Any]]]
    result.map(r => interceptor.interceptCall(methodDescriptor, options, r))

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[?]],
    t: Throwable
  ): Unit = {}

end ZioGrpcClientInterceptor

object ZioGrpcClientInterceptor:

  private final class ZTraceClientInterceptor[R](peer: Option[String]) extends ZClientInterceptor[R] {

    def interceptCall[REQUEST, RESPONSE](
      methodDescriptor: MethodDescriptor[REQUEST, RESPONSE],
      call: CallOptions,
      clientCall: ZClientCall[R, REQUEST, RESPONSE]
    ): ZClientCall[R, REQUEST, RESPONSE] =
      new TracingClientCall[R, REQUEST, RESPONSE](peer, clientCall, methodDescriptor)

  }
