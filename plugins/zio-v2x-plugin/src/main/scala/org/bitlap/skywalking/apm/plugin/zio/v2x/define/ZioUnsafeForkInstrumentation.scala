package org.bitlap.skywalking.apm.plugin.zio.v2x.define

import java.util.{ Collections, List as JList }
import java.util.Collections

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.*
import net.bytebuddy.matcher.ElementMatchers.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod
import org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ReturnTypeNameMatch
import org.apache.skywalking.apm.agent.core.plugin.interceptor.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.zcommon.ZioWitnessConstant
import org.bitlap.skywalking.apm.plugin.zio.v2x.interceptor.ZioUnsafeForkInterceptor

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioUnsafeForkInstrumentation extends ClassInstanceMethodsEnhancePluginDefine:

  import ZioUnsafeForkInstrumentation.*

  override def enhanceClass(): ClassMatch = ENHANCE_CLASS

  override protected def witnessMethods: JList[WitnessMethod] =
    Collections.singletonList(ZioWitnessConstant.WITNESS_203X_METHOD)

  override def getConstructorsInterceptPoints: Array[ConstructorInterceptPoint] = null

  override def getInstanceMethodsInterceptPoints: Array[InstanceMethodsInterceptPoint] =
    methodInterceptors
      .map(kv =>
        new InstanceMethodsInterceptPoint {
          override def getMethodsMatcher: ElementMatcher[MethodDescription] = kv._2
          override def getMethodsInterceptor: String                        = kv._1
          override def isOverrideArgs: Boolean                              = false
        }
      )
      .toArray

  end getInstanceMethodsInterceptPoints

end ZioUnsafeForkInstrumentation

object ZioUnsafeForkInstrumentation:

  final val ENHANCE_CLASS = NameMatch.byName("zio.ZIO$unsafe$")

  final val FORK_INTERCEPTOR: String = classOf[ZioUnsafeForkInterceptor].getTypeName

  final val methodInterceptors: Map[String, ElementMatcher[MethodDescription]] =
    Map(
      FORK_INTERCEPTOR -> named("fork").and(takesArguments(6))
    )
