package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class ZioGrpcClientInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioGrpcClientInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    Array(new InstanceMethodsInterceptPoint() {

      override def getMethodsMatcher: ElementMatcher[MethodDescription] =
        getUnaryMethod

      override def getMethodsInterceptor: String =
        INTERCEPTOR_CLASS

      override def isOverrideArgs: Boolean =
        false
    })

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = Array(
    new ConstructorInterceptPoint() {
      override def getConstructorMatcher: ElementMatcher[MethodDescription] = takesArguments(2)
      override def getConstructorInterceptor: String                        = INTERCEPTOR_CLASS
    }
  )

end ZioGrpcClientInstrumentation

object ZioGrpcClientInstrumentation:

  private final val INTERCEPTOR_CLASS =
    "org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.ZioGrpcClientInterceptor"
  private final val ENHANCE_CLASS  = "scalapb.zio_grpc.ZChannel"
  private final val ENHANCE_METHOD = "newCall"

  def getUnaryMethod: ElementMatcher[MethodDescription] =
    named(ENHANCE_METHOD)

end ZioGrpcClientInstrumentation
