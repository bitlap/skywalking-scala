package org.bitlap.skywalking.apm.plugin.caliban.v2

import scala.jdk.CollectionConverters.*
import scala.util.*

import caliban.*
import caliban.execution.QueryExecution
import caliban.parsing.Parser
import caliban.parsing.adt.{ Document, Selection }
import caliban.wrappers.Wrapper.*

import zio.*

import org.apache.skywalking.apm.agent.core.context
import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.util.CollectionUtil
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.apache.skywalking.apm.util.StringUtil
import org.bitlap.skywalking.apm.plugin.caliban.v2.TracingCaliban.getOperationName
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.zcommon.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/18
 */
object TracingCaliban:

  val traceOverall: OverallWrapper[Any] =
    new OverallWrapper[Any]:

      def wrap[R1](
        process: GraphQLRequest => ZIO[R1, Nothing, GraphQLResponse[CalibanError]]
      ): GraphQLRequest => ZIO[R1, Nothing, GraphQLResponse[CalibanError]] =
        (request: GraphQLRequest) =>
          for {
            span   <- ZIO.succeed(beforeWrapperRequest(request))
            result <- process(request)
            _      <- afterWrapperRequest(span, result)
          } yield result

  def afterRequest[R](
    span: Option[AbstractSpan],
    ret: ZIO[R, Nothing, GraphQLResponse[CalibanError]]
  ): ZIO[R, Nothing, GraphQLResponse[CalibanError]] =
    ret.onExit {
      case Exit.Success(value) =>
        ZIO.succeed {
          span.foreach(a => AgentUtils.stopAsync(a))
          if value.errors.nonEmpty then {
            val ex: Option[CalibanError] = value.errors.headOption
            span.foreach(_.log(ex.getOrElse(CalibanError.ExecutionError("Effect failure"))))
          }
          ContextManager.stopSpan()
        }

      case Exit.Failure(cause) =>
        ZIO.succeed {
          span.foreach(a => AgentUtils.stopAsync(a))
          ZUtils.logError(cause)
          ContextManager.stopSpan()
        }
    }

  def beforeRequest(graphQLRequest: GraphQLRequest): Option[AbstractSpan] =
    checkRequest(graphQLRequest) {
      val opName =
        CalibanPluginConfig.Plugin.CalibanV2.URL_PREFIX + TracingCaliban.getOperationName(graphQLRequest)
      val contextCarrier = new ContextCarrier
      val span           = ContextManager.createEntrySpan(opName, contextCarrier)
      SpanLayer.asHttp(span)
      if CalibanPluginConfig.Plugin.CalibanV2.COLLECT_VARIABLES then {
        val tagValue = collectVariables(graphQLRequest)
        CustomTag.CalibanVariables.tag.set(span, tagValue)
      }
      AgentUtils.prepareAsync(span)
      Tags.LOGIC_ENDPOINT.set(span, Tags.VAL_LOCAL_SPAN_AS_LOGIC_ENDPOINT)
      span.setComponent(ComponentsDefine.GRAPHQL)
      Some(span)
    }

  private def afterWrapperRequest(
    span: Option[AbstractSpan],
    result: GraphQLResponse[CalibanError]
  ): ZIO[Any, Nothing, Unit] =
    ZIO.succeed {
      span.foreach(a => AgentUtils.stopAsync(a))

      if result.errors.nonEmpty then {
        val ex: Option[CalibanError] = result.errors.headOption
        span.foreach(_.log(ex.getOrElse(CalibanError.ExecutionError("Effect failure"))))
      }

      ContextManager.stopSpan()
    }

  private def beforeWrapperRequest(graphQLRequest: GraphQLRequest): Option[AbstractSpan] =
    checkRequest(graphQLRequest) {
      val opName =
        CalibanPluginConfig.Plugin.CalibanV2.URL_PREFIX + TracingCaliban.getOperationName(graphQLRequest) + "/wrapper"
      val span = ContextManager.createLocalSpan(opName)
      AgentUtils.prepareAsync(span)
      SpanLayer.asHttp(span)
      span.setComponent(ComponentsDefine.GRAPHQL)
      Some(span)
    }

  private def checkRequest(graphQLRequest: GraphQLRequest)(effect: => Option[AbstractSpan]): Option[AbstractSpan] =
    if graphQLRequest == null || graphQLRequest.query.isEmpty then None
    else {

      if AgentUtils.ignorePrefix(
          CalibanPluginConfig.Plugin.CalibanV2.IGNORE_URL_PREFIXES,
          getOperationName(graphQLRequest)
        )
      then {
        return None
      }

      effect
    }

  private def collectVariables(graphQLRequest: GraphQLRequest): String = {
    val params   = graphQLRequest.variables.getOrElse(Map.empty).map(kv => kv._1 -> Array(kv._2.toInputString))
    val tagValue = CollectionUtil.toString(params.asJava)
    if CalibanPluginConfig.Plugin.CalibanV2.VARIABLES_LENGTH_THRESHOLD > 0 then {
      StringUtil.cut(tagValue, CalibanPluginConfig.Plugin.CalibanV2.VARIABLES_LENGTH_THRESHOLD)
    } else tagValue
  }

  private def getOperationName(graphQLRequest: GraphQLRequest) =
    val tryOp: Try[String] = Try {
      val docOpName = ZUtils
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
        AgentUtils.logError(e)
        "Unknown"
      case Success(value) => value
