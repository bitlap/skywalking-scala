package org.bitlap.skywalking.apm.plugin.caliban.v2.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.named

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ReturnTypeNameMatch
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

final class CalibanInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import CalibanInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = null

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    Array[InstanceMethodsInterceptPoint](
      new InstanceMethodsInterceptPoint:
        override def getMethodsMatcher: ElementMatcher[MethodDescription] = getMethod

        override def getMethodsInterceptor: String = INTERCEPTOR_CLASS

        override def isOverrideArgs: Boolean = false
    )
end CalibanInstrumentation

object CalibanInstrumentation:
  private final val INTERCEPTOR_CLASS: String = "org.bitlap.skywalking.apm.plugin.caliban.v2.CalibanInterceptor"

  private final val ENHANCE_CLASS                      = NameMatch.byName("caliban.GraphQL$$anon$2")
  private final val EXECUTE_METHOD_INTERCEPTOR: String = "executeRequest"

  def getMethod: ElementMatcher[MethodDescription] =
    named(EXECUTE_METHOD_INTERCEPTOR).and(ReturnTypeNameMatch.returnsWithType("zio.ZIO"))

end CalibanInstrumentation
