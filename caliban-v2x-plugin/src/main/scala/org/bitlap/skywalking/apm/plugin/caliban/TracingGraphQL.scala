package org.bitlap.skywalking.apm.plugin.caliban

import scala.jdk.CollectionConverters.*
import scala.util.*

import caliban.*
import caliban.execution.QueryExecution
import caliban.parsing.Parser
import caliban.parsing.adt.{ Document, Selection }
import caliban.wrappers.Wrapper.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.util.CollectionUtil
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.apache.skywalking.apm.util.StringUtil
import org.bitlap.skywalking.apm.plugin.common.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/18
 */
object TracingGraphQL:

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

      val opName         = CalibanPluginConfig.Plugin.Caliban.URL_PREFIX + getOperationName(graphQLRequest)
      val contextCarrier = new ContextCarrier
      val span           = ContextManager.createEntrySpan(opName, contextCarrier)
      span.prepareForAsync()
      SpanLayer.asHttp(span)
      if CalibanPluginConfig.Plugin.Caliban.COLLECT_VARIABLES then {
        val tagValue = collectVariables(graphQLRequest)
        CustomTag.CalibanVariables.tag.set(span, tagValue)
      }
      Tags.LOGIC_ENDPOINT.set(span, Tags.VAL_LOCAL_SPAN_AS_LOGIC_ENDPOINT)
      span.setComponent(ComponentsDefine.GRAPHQL)
      Some(span)
    }

  private def collectVariables(graphQLRequest: GraphQLRequest): String = {
    val params   = graphQLRequest.variables.getOrElse(Map.empty).map(kv => kv._1 -> Array(kv._2.toInputString))
    val tagValue = CollectionUtil.toString(params.asJava)
    if CalibanPluginConfig.Plugin.Caliban.VARIABLES_LENGTH_THRESHOLD > 0 then {
      StringUtil.cut(tagValue, CalibanPluginConfig.Plugin.Caliban.VARIABLES_LENGTH_THRESHOLD)
    } else tagValue
  }

  private def getOperationName(graphQLRequest: GraphQLRequest) =
    val tryOp: Try[String] = Try {
      val docOpName = Utils
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
        Utils.logError(e)
        "Unknown"
      case Success(value) => value
