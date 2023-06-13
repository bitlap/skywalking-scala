package org.bitlap.skywalking.apm.plugin.caliban.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.named

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ReturnTypeNameMatch
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class CalibanInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import CalibanInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] =
    new Array[ConstructorInterceptPoint](0)

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    Array[InstanceMethodsInterceptPoint](
      new InstanceMethodsInterceptPoint:
        override def getMethodsMatcher: ElementMatcher[MethodDescription] = getCalibanExecuteRequestMethod

        override def getMethodsInterceptor: String = INTERCEPTOR_CLASS

        override def isOverrideArgs: Boolean = false
    )
end CalibanInstrumentation

object CalibanInstrumentation:
  private final val INTERCEPTOR_CLASS: String = "org.bitlap.skywalking.apm.plugin.caliban.CalibanInterceptor"

  private final val ENHANCE_CLASS          = MultiClassNameMatch.byMultiClassMatch("caliban.GraphQL$$anon$2")
  private final val ENHANCE_METHOD: String = "executeRequest"

  def getCalibanExecuteRequestMethod: ElementMatcher[MethodDescription] =
    named(ENHANCE_METHOD).and(ReturnTypeNameMatch.returnsWithType("zio.ZIO"))

end CalibanInstrumentation
