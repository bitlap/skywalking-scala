package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

import java.lang.reflect.Method

import io.grpc.ServerBuilder

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
class ZioGrpcServerInterceptor extends InstanceMethodsAroundInterceptor:

  def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    result: MethodInterceptResult
  ): Unit =
    if (objInst.getSkyWalkingDynamicField == null) {
      val builder     = objInst.asInstanceOf[ServerBuilder[?]]
      val interceptor = new NativeGrpcServerInterceptor
      builder.intercept(interceptor)
      objInst.setSkyWalkingDynamicField(interceptor)
    }

  def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    ret: Object
  ): Object = ret

  def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    t: Throwable
  ): Unit = ()

end ZioGrpcServerInterceptor
