package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

import java.lang.reflect.Method

import scalapb.zio_grpc.*
import scalapb.zio_grpc.client.*

import io.grpc.*

import zio.ZIO

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.forward.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class ZioGrpcClientInterceptor extends InstanceConstructorInterceptor, InstanceMethodsAroundInterceptor:
  import ZioGrpcClientInterceptor.*

  override def onConstruct(objInst: EnhancedInstance, allArguments: Array[Object]): Unit = {
    val interceptors = allArguments(1).asInstanceOf[Seq[ZClientInterceptor[?]]]
    allArguments(1) = interceptors ++ Seq(new ZTraceClientInterceptor[Any])
    objInst.setSkyWalkingDynamicField(allArguments)
  }

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    result: MethodInterceptResult
  ): Unit = {}

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    ret: Object
  ): Object =
    if (objInst.getSkyWalkingDynamicField == null || !objInst.getSkyWalkingDynamicField.isInstanceOf[Array[?]])
      return ret
    val constructorParams = objInst.getSkyWalkingDynamicField.asInstanceOf[Array[Object]]
    val channel           = constructorParams(0).asInstanceOf[ManagedChannel]
    val interceptors      = constructorParams(1).asInstanceOf[Seq[ZClientInterceptor[Any]]]
    val methodDescriptor  = allArguments(0).asInstanceOf[MethodDescriptor[Any, Any]]
    val options           = allArguments(1).asInstanceOf[CallOptions]
    ZIO.succeed(
      interceptors.foldLeft[ZClientCall[Any, Any, Any]](
        ZClientCall(channel.newCall(methodDescriptor, options))
      )((call: ZClientCall[Any, Any, Any], interceptor) =>
        interceptor.interceptCall[Any, Any](methodDescriptor, options, call)
      )
    )

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    t: Throwable
  ): Unit = {}

end ZioGrpcClientInterceptor

object ZioGrpcClientInterceptor:

  final class ZTraceClientInterceptor[R] extends ZClientInterceptor[R] {

    def interceptCall[REQUEST, RESPONSE](
      methodDescriptor: MethodDescriptor[REQUEST, RESPONSE],
      call: CallOptions,
      clientCall: ZClientCall[R, REQUEST, RESPONSE]
    ): ZClientCall[R, REQUEST, RESPONSE] =
      new TracingClientCall[R, REQUEST, RESPONSE](clientCall, methodDescriptor)

  }
