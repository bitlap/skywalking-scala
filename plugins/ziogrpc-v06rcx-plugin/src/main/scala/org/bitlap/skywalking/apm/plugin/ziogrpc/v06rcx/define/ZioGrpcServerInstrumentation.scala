package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor.*

final class ZioGrpcServerInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioGrpcServerInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

  override def witnessClasses(): Array[String] = Array("scalapb.zio_grpc.ServerImpl")

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

  def getMethod: ElementMatcher[MethodDescription] =
    named("startCall").and(ArgumentTypeNameMatch.takesArgumentWithType(1, "io.grpc.Metadata"))

end ZioGrpcServerInstrumentation
