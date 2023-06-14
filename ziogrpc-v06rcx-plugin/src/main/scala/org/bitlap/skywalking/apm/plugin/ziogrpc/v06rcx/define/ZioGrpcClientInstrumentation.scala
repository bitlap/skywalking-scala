package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.ZioGrpcWitnessConstant

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class ZioGrpcClientInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioGrpcClientInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

  override def witnessClasses(): Array[String] = ZioGrpcWitnessConstant.`WITNESS_TEST6-RC5_CLASS`

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] = Array(
    new InstanceMethodsInterceptPoint:
      override def getMethodsMatcher: ElementMatcher[MethodDescription] = getMethod
      override def getMethodsInterceptor: String                        = INTERCEPTOR_CLASS
      override def isOverrideArgs: Boolean                              = false
  )

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = Array(
    new ConstructorInterceptPoint:
      override def getConstructorMatcher: ElementMatcher[MethodDescription] = takesArguments(2)
      override def getConstructorInterceptor: String                        = INTERCEPTOR_CLASS
  )

end ZioGrpcClientInstrumentation

object ZioGrpcClientInstrumentation:

  private final val INTERCEPTOR_CLASS: String =
    "org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor.ZioGrpcClientInterceptor"
  private final val ENHANCE_CLASS: String  = "scalapb.zio_grpc.ZChannel"
  private final val ENHANCE_METHOD: String = "newCall"

  def getMethod: ElementMatcher[MethodDescription] =
    named(ENHANCE_METHOD)

end ZioGrpcClientInstrumentation
