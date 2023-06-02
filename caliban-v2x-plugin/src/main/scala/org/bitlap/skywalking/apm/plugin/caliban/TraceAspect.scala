package org.bitlap.skywalking.apm.plugin.caliban

import scala.util.*

import caliban.*
import caliban.execution.QueryExecution
import caliban.parsing.Parser
import caliban.parsing.adt.{ Document, Selection }
import caliban.wrappers.Wrapper.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/18
 */
object TraceAspect:

  def beforeRequest(graphQLRequest: GraphQLRequest): Option[AbstractSpan] =
    if graphQLRequest == null || graphQLRequest.query.isEmpty then None
    else {

      if Utils.ignorePrefix(
          CalibanPluginConfig.Plugin.Caliban.IGNORE_URL_PREFIXES,
          getOperationName(graphQLRequest)
        )
      then {
        return None
      }

      val opName = CalibanPluginConfig.Plugin.Caliban.URL_PREFIX + getOperationName(graphQLRequest)
      val span   = ContextManager.createLocalSpan(opName)
      span.prepareForAsync()
      SpanLayer.asHttp(span)
      if CalibanPluginConfig.Plugin.Caliban.COLLECT_VARIABLES then {
        ScalaTags.CalibanVariables.tag
          .set(span, graphQLRequest.variables.getOrElse(Map.empty).map(kv => kv._1 -> kv._2.toInputString).toString())
      }
      Tags.LOGIC_ENDPOINT.set(span, Tags.VAL_LOCAL_SPAN_AS_LOGIC_ENDPOINT)
      span.setComponent(ComponentsDefine.GRAPHQL)
      Some(span)
    }

  def getOperationName(graphQLRequest: GraphQLRequest) =
    val tryOp: Try[String] = Try {
      val docOpName = InterceptorDSL
        .unsafeRun(Parser.parseQuery(graphQLRequest.query.get))
        .operationDefinitions
        .map(_.selectionSet.collectFirst {
          case Selection.Field(alias, name, arguments, directives, selectionSet, index) => alias.getOrElse(name)
        })
        .headOption
        .flatten
      val name = graphQLRequest.operationName.map(_.split("__", 2).toList).toList.flatten.headOption
      name.orElse(docOpName).getOrElse("Unknown")
    }
    tryOp match
      case Failure(e) =>
        if ContextManager.isActive then ContextManager.activeSpan().log(e)
        "Unknown"
      case Success(value) => value
