package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.named

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch.takesArgumentWithType
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class ZioGrpcClientInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioGrpcClientInstrumentation.*

  override def enhanceClass(): ClassMatch = NameMatch.byName(ENHANCE_CLASS)

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = Array()

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] = Array(
    new InstanceMethodsInterceptPoint:
      override def getMethodsMatcher: ElementMatcher[MethodDescription] = getUnaryMethod

      override def getMethodsInterceptor: String = INTERCEPTOR_CLASS

      override def isOverrideArgs: Boolean = false
  )
end ZioGrpcClientInstrumentation

object ZioGrpcClientInstrumentation:

  private final val INTERCEPTOR_CLASS =
    "org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.ZioGrpcClientInterceptor"
    // scala object type endWiths $
  private final val ENHANCE_CLASS      = "scalapb.zio_grpc.client.ClientCalls$"
  private final val ENHANCE_METHOD     = "unaryCall"
  private final val ARGUMENT_TYPE_NAME = "scalapb.zio_grpc.ZChannel"

  def getUnaryMethod: ElementMatcher[MethodDescription] =
    named(ENHANCE_METHOD)
      .and(takesArgumentWithType(0, ARGUMENT_TYPE_NAME))

end ZioGrpcClientInstrumentation
