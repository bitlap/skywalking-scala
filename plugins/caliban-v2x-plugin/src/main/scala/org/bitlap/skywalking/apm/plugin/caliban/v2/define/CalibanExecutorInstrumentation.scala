package org.bitlap.skywalking.apm.plugin.caliban.v2.define

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.caliban.v2.*

final class CalibanExecutorInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import CalibanExecutorInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = null

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    Array[InstanceMethodsInterceptPoint](
      new InstanceMethodsInterceptPoint:
        override def getMethodsMatcher: ElementMatcher[MethodDescription] = getMethod

        override def getMethodsInterceptor: String = INTERCEPTOR_CLASS

        override def isOverrideArgs: Boolean = false
    )
end CalibanExecutorInstrumentation

object CalibanExecutorInstrumentation:

  final val INTERCEPTOR_CLASS: String = "org.bitlap.skywalking.apm.plugin.caliban.v2.CalibanExecutorInterceptor"
  final val ENHANCE_CLASS             = NameMatch.byName("caliban.execution.Executor$")

  def getMethod: ElementMatcher[MethodDescription] = named("executeRequest")

end CalibanExecutorInstrumentation
