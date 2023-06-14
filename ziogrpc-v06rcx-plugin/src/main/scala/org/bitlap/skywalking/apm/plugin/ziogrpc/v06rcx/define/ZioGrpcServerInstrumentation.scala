package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch.takesArgumentWithType
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.ZioGrpcWitnessConstant

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
final class ZioGrpcServerInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioGrpcServerInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

  override def witnessClasses(): Array[String] = ZioGrpcWitnessConstant.`WITNESS_TEST6-RC5_CLASS`

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = null

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] = Array(
    new InstanceMethodsInterceptPoint:
      override def getMethodsMatcher: ElementMatcher[MethodDescription] = getMethod

      override def getMethodsInterceptor: String = INTERCEPTOR_CLASS

      override def isOverrideArgs: Boolean = false
  )

end ZioGrpcServerInstrumentation

object ZioGrpcServerInstrumentation:

  private final val INTERCEPTOR_CLASS =
    "org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor.ZioGrpcServerInterceptor"
  private final val ENHANCE_CLASS: String = "scalapb.zio_grpc.server.ZServerCallHandler"

  private final val ENHANCE_METHOD: String     = "startCall"
  private final val ARGUMENT_TYPE_NAME: String = "io.grpc.Metadata"

  def getMethod: ElementMatcher[MethodDescription] =
    named(ENHANCE_METHOD).and(takesArgumentWithType(1, ARGUMENT_TYPE_NAME))

end ZioGrpcServerInstrumentation
