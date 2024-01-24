package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.`match`.logical.LogicalMatchOperation
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

final class ZioGrpcServerCallInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioGrpcServerCallInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override def witnessClasses(): Array[String] = Array("scalapb.zio_grpc.ServerImpl")

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = null

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    methodInterceptors
      .map(kv =>
        new InstanceMethodsInterceptPoint {
          override def getMethodsMatcher: ElementMatcher[MethodDescription] = kv._2
          override def getMethodsInterceptor: String                        = kv._1
          override def isOverrideArgs: Boolean                              = false
        }
      )
      .toArray

end ZioGrpcServerCallInstrumentation

object ZioGrpcServerCallInstrumentation:

  final val CLOSE_METHOD_INTERCEPTOR =
    "org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor.ZioGrpcServerCloseInterceptor"

  final val SEND_MESSAGE_METHOD_INTERCEPTOR =
    "org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor.ZioGrpcServerSendMessageInterceptor"

  // see issue: https://github.com/scalapb/zio-grpc/issues/501, we cannot use Server Interceptor
  // Because the server stream call calls `ServerCall.sendMessage` and `ServerCall.close`, and we intercept grpc directly.
  private final val ENHANCE_CLASS = LogicalMatchOperation.or(
    HierarchyMatch.byHierarchyMatch("io.grpc.ServerCall"),
    MultiClassNameMatch.byMultiClassMatch("io.grpc.ServerCall")
  )

  val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] = Map(
    CLOSE_METHOD_INTERCEPTOR        -> named("close"),
    SEND_MESSAGE_METHOD_INTERCEPTOR -> named("sendMessage")
  )

end ZioGrpcServerCallInstrumentation
