package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
final class ZioGrpcServerCallInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioGrpcServerCallInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

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

  private final val INTERCEPTOR_CLOSE_CLASS =
    "org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.call.ZioGrpcServerCloseInterceptor"

  private final val INTERCEPTOR_SEND_MESSAGE_CLASS =
    "org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.call.ZioGrpcServerSendMessageInterceptor"

  private final val ENHANCE_CLASS = "scalapb.zio_grpc.server.ZServerCall$"

  // NOTE: ZServerCall is a AnyVal!!!
  private final val ENHANCE_CLOSE_METHOD        = "close$extension"
  private final val ENHANCE_SEND_MESSAGE_METHOD = "sendMessage$extension"

  val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] = Map(
    INTERCEPTOR_CLOSE_CLASS        -> named(ENHANCE_CLOSE_METHOD),
    INTERCEPTOR_SEND_MESSAGE_CLASS -> named(ENHANCE_SEND_MESSAGE_METHOD)
  )

end ZioGrpcServerCallInstrumentation
