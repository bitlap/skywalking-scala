package org.bitlap.skywalking.apm.plugin.zio.v203.define

import java.util.Collections
import java.util.List as JList

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.zio.v203.ZioWitnessConstant

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioExecutorInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioExecutorInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override protected def witnessMethods: JList[WitnessMethod] =
    Collections.singletonList(ZioWitnessConstant.WITNESS_203X_METHOD)

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = null

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    methodInterceptors
      .map(kv =>
        new InstanceMethodsInterceptPoint {
          override def getMethodsMatcher: ElementMatcher[MethodDescription] = kv._2

          override def getMethodsInterceptor: String = kv._1.split("_")(0)

          override def isOverrideArgs: Boolean = false
        }
      )
      .toArray
  end getInstanceMethodsInterceptPoints

end ZioExecutorInstrumentation

object ZioExecutorInstrumentation:

  final val ENHANCE_CLASS = HierarchyMatch.byHierarchyMatch("zio.Executor")

  final val EXECUTOR_INTERCEPTOR: String =
    "org.bitlap.skywalking.apm.plugin.common.interceptor.ZioExecutorInterceptor"

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] =
    (0 until 2)
      .map(i => s"${EXECUTOR_INTERCEPTOR}_$i")
      .zip(
        List(
          named("submit").and(takesArguments(2)),
          named("submitAndYield").and(takesArguments(2))
        )
      )
      .toMap