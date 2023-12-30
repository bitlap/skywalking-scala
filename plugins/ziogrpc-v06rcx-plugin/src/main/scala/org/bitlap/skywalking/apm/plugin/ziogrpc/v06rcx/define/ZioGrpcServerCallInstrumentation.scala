package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor.*

final class ZioGrpcServerCallInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioGrpcServerCallInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

  override def witnessClasses(): Array[String] = Array("scalapb.zio_grpc.ServerImpl")

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = Array(
    new ConstructorInterceptPoint:
      override def getConstructorMatcher: ElementMatcher[MethodDescription] = takesArguments(2)

      override def getConstructorInterceptor: String = CLASS_INTERCEPTOR
  )

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

  final val CLASS_INTERCEPTOR = classOf[ZioGrpcServerCallConstructorInterceptor].getTypeName

  final val CLOSE_METHOD_INTERCEPTOR = classOf[ZioGrpcServerCloseInterceptor].getTypeName

  final val SEND_MESSAGE_METHOD_INTERCEPTOR = classOf[ZioGrpcServerSendMessageInterceptor].getTypeName

  private final val ENHANCE_CLASS: String = "scalapb.zio_grpc.server.ZServerCall"

  val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] = Map(
    CLOSE_METHOD_INTERCEPTOR        -> named("close"),
    SEND_MESSAGE_METHOD_INTERCEPTOR -> named("sendMessage")
  )

end ZioGrpcServerCallInstrumentation
