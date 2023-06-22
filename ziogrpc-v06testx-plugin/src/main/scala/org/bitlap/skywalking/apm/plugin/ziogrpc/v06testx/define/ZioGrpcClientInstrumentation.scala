package org.bitlap.skywalking.apm.plugin.ziogrpc.v06testx.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06testx.interceptor.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class ZioGrpcClientInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioGrpcClientInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

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

  final val ENHANCE_CLASS = NameMatch.byName("scalapb.zio_grpc.ZChannel")

  final val INTERCEPTOR_CLASS: String = classOf[ZioGrpcClientInterceptor].getTypeName

  def getMethod: ElementMatcher[MethodDescription] =
    named("newCall")

end ZioGrpcClientInstrumentation
