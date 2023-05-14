package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

import java.lang.reflect.Method

import scalapb.zio_grpc.*
import scalapb.zio_grpc.client.*

import io.grpc.*

import zio.{ UIO, ZIO }

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.forward.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class ZioGrpcClientInterceptor extends InstanceMethodsAroundInterceptor:
  import ZioGrpcClientInterceptor.*

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
    val interceptor      = new ZTraceClientInterceptor[Any]
    val methodDescriptor = allArguments(0).asInstanceOf[MethodDescriptor[Any, Any]]
    val options          = allArguments(1).asInstanceOf[CallOptions]
    val result           = ret.asInstanceOf[UIO[ZClientCall[Any, Any, Any]]]
    result.map(r => interceptor.interceptCall(methodDescriptor, options, r))

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
