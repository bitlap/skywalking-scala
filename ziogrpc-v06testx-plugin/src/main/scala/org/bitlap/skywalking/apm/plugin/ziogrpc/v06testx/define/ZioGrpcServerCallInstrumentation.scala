package org.bitlap.skywalking.apm.plugin.ziogrpc.v06testx.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
final class ZioGrpcServerCallInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioGrpcServerCallInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

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

  final val ENHANCE_CLASS = NameMatch.byName("scalapb.zio_grpc.server.ZServerCall$")

  final val CLOSE_METHOD_INTERCEPTOR =
    "org.bitlap.skywalking.apm.plugin.ziogrpc.v06testx.interceptor.ZioGrpcServerCloseInterceptor"

  final val SEND_MESSAGE_METHOD_INTERCEPTOR =
    "org.bitlap.skywalking.apm.plugin.ziogrpc.v06testx.interceptor.ZioGrpcServerSendMessageInterceptor"

  // NOTE: ZServerCall is a AnyVal!!!
  val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] = Map(
    CLOSE_METHOD_INTERCEPTOR        -> named("close$extension"),
    SEND_MESSAGE_METHOD_INTERCEPTOR -> named("sendMessage$extension")
  )

end ZioGrpcServerCallInstrumentation
