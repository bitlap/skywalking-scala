package org.bitlap.skywalking.apm.plugin.zio.v2x.define

import java.util
import java.util.{ Collections, List as JList }

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.*
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.`match`.logical.{ LogicalAndMatch, LogicalMatchOperation }
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ReturnTypeNameMatch
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.common.interceptor.*
import org.bitlap.skywalking.apm.plugin.zio.v2x.interceptor.ZioFiberRuntimeInterceptor

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioExecutorInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioExecutorInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override protected def witnessMethods: JList[WitnessMethod] =
    Collections.singletonList(
      new WitnessMethod("zio.internal.FiberRunnable", ElementMatchers.named("run").and(takesArguments(1)))
    )

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = null

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    methodInterceptors
      .map(kv =>
        new InstanceMethodsInterceptPoint {
          override def getMethodsMatcher: ElementMatcher[MethodDescription] = kv._2
          override def getMethodsInterceptor: String                        = kv._1.split("_")(0)
          override def isOverrideArgs: Boolean                              = false
        }
      )
      .toArray

  end getInstanceMethodsInterceptPoints

end ZioExecutorInstrumentation

object ZioExecutorInstrumentation:

  final val ENHANCE_CLASS = LogicalMatchOperation.or(
    HierarchyMatch.byHierarchyMatch("zio.Executor"),
    MultiClassNameMatch.byMultiClassMatch("zio.Executor")
  )

  final val SUBMIT_METHOD_INTERCEPTOR: String = classOf[CaptureContextOnSubmitInterceptor].getTypeName

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] =
    Map(
      SUBMIT_METHOD_INTERCEPTOR -> named("submit")
    )
