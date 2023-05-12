package org.bitlap.skywalking.apm.plugin.caliban.v2x

import java.lang.reflect.Method
import java.util
import java.util.{ HashMap, Map }
import java.util.function.BiConsumer

import scala.util.{ Failure, Success, Try }

import caliban.*
import caliban.execution.QueryExecution
import caliban.parsing.Parser
import caliban.parsing.adt.{ Document, Selection }

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.{ AbstractSpan, SpanLayer }
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine

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
  ): Object = {
    val span = objInst.getSkyWalkingDynamicField.asInstanceOf[AbstractSpan]
    if (span == null) return ret

    val graphQLRequest = allArguments(0).asInstanceOf[GraphQLRequest]
    if (graphQLRequest == null || graphQLRequest.query.isEmpty) {
      ContextManager.stopSpan(span)
      return ret
    }
    val result = ret.asInstanceOf[URIO[_, GraphQLResponse[CalibanError]]]
    result
      .catchAllCause(c => ZIO.attempt(dealException(c.squash)) *> ZIO.done(Exit.Failure(c)))
      .ensuring(
        ZIO.attempt {
          ContextManager.activeSpan().asyncFinish()
          ContextManager.stopSpan()
        }
          .catchAllCause(t => ZIO.attempt(dealException(t.squash)).ignore)
      )
  }

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    t: Throwable
  ): Unit = {
    val graphQLRequest = allArguments(0).asInstanceOf[GraphQLRequest]
    if (graphQLRequest == null || graphQLRequest.query.isEmpty || graphQLRequest.operationName.isEmpty) return
    dealException(t)
  }

  private def getOperationName(graphQLRequest: GraphQLRequest) =
    val tryOp: Try[String] = Try(
      Unsafe.unsafe { runtime ?=>
        val doc: Document =
          zio.Runtime.default.unsafe.run(Parser.parseQuery(graphQLRequest.query.get)).getOrThrowFiberFailure()
        val docOpName = doc.operationDefinitions
          .map(_.selectionSet.collectFirst {
            case Selection.Field(alias, name, arguments, directives, selectionSet, index) => alias.getOrElse(name)
          })
          .headOption
          .flatten
        graphQLRequest.operationName.orElse(docOpName).getOrElse("Unknown")
      }
    )
    tryOp match
      case Failure(e) =>
        dealException(e)
        "Unknown"
      case Success(value) => value

  private def dealException(t: Throwable): Unit =
    ContextManager.activeSpan.errorOccurred.log(t)
