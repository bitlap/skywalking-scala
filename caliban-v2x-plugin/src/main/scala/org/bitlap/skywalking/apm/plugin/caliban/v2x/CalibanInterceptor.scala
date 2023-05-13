package org.bitlap.skywalking.apm.plugin.caliban.v2x

import java.lang.reflect.Method

import scala.util.*

import caliban.*
import caliban.execution.QueryExecution
import caliban.parsing.Parser
import caliban.parsing.adt.{ Document, Selection }

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
final class CalibanInterceptor extends InstanceMethodsAroundInterceptor:

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    result: MethodInterceptResult
  ): Unit = {
    val graphQLRequest = allArguments(0).asInstanceOf[GraphQLRequest]
    if (graphQLRequest == null || graphQLRequest.query.isEmpty) return
    val opName = getOperationName(graphQLRequest)
    val span   = ContextManager.createLocalSpan(opName)
    span.prepareForAsync()
    Tags.LOGIC_ENDPOINT.set(span, Tags.VAL_LOCAL_SPAN_AS_LOGIC_ENDPOINT)
    SpanLayer.asHttp(span)
    span.setComponent(ComponentsDefine.GRAPHQL)
    objInst.setSkyWalkingDynamicField(span)
  }

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    ret: Object
  ): Object =
    InterceptorUtils.handleMethodExit(objInst, ret) { ret =>
      ret.asInstanceOf[URIO[_, GraphQLResponse[CalibanError]]]
    }

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    t: Throwable
  ): Unit =
    InterceptorUtils.handleMethodException(objInst, allArguments, t)(_ => ())

  private def getOperationName(graphQLRequest: GraphQLRequest) =
    val tryOp: Try[String] = Try {
      val docOpName = InterceptorUtils
        .unsafeRunZIO(Parser.parseQuery(graphQLRequest.query.get))
        .operationDefinitions
        .map(_.selectionSet.collectFirst {
          case Selection.Field(alias, name, arguments, directives, selectionSet, index) => alias.getOrElse(name)
        })
        .headOption
        .flatten
      graphQLRequest.operationName.orElse(docOpName).getOrElse("Unknown")
    }
    tryOp match
      case Failure(e) =>
        ContextManager.activeSpan().log(e)
        "Unknown"
      case Success(value) => value

end CalibanInterceptor
