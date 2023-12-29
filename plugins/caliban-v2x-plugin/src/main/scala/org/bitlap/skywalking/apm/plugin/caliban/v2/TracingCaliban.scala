package org.bitlap.skywalking.apm.plugin.caliban.v2

import scala.jdk.CollectionConverters.*
import scala.util.*

import caliban.*
import caliban.execution.{ ExecutionRequest, QueryExecution }
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
import org.bitlap.skywalking.apm.plugin.common.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/18
 */
object TracingCaliban:

  private val DEFAULT_OP = "Unknown"

  val traceOverall: OverallWrapper[Any] =
    new OverallWrapper[Any]:

      def wrap[R1](
        process: GraphQLRequest => ZIO[R1, Nothing, GraphQLResponse[CalibanError]]
      ): GraphQLRequest => ZIO[R1, Nothing, GraphQLResponse[CalibanError]] =
        (request: GraphQLRequest) =>
          val span = beforeOverallGraphQLRequest(request)
          process(request).tap(result => afterGraphQLRequest(span, result).ignore)

  def afterRequest[R](
    span: Option[AbstractSpan],
    ret: ZIO[R, Nothing, GraphQLResponse[CalibanError]]
  ): ZIO[R, Nothing, GraphQLResponse[CalibanError]] =
    ret.onExit {
      case Exit.Success(value) =>
        ZIO.attempt(tagCalibanError(span, value)).ignore

      case Exit.Failure(cause) =>
        ZIO.attempt {
          span.foreach(a => AgentUtils.stopAsync(a))
          if ContextManager.isActive then ContextManager.activeSpan.log(cause.squash)
          ContextManager.stopSpan()
        }.ignore
    }

  def beforeRequest(graphQLRequest: GraphQLRequest): Option[AbstractSpan] =
    checkRequest(graphQLRequest) {
      val opName =
        CalibanPluginConfig.Plugin.CalibanV2.URL_PREFIX + TracingCaliban.getOperationName(graphQLRequest)
      val span = ContextManager.createLocalSpan(opName)
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

  private def afterGraphQLRequest(
    span: Option[AbstractSpan],
    result: GraphQLResponse[CalibanError]
  ): ZIO[Any, Nothing, Unit] =
    ZIO.attempt(tagCalibanError(span, result)).ignore

  private def tagCalibanError(span: Option[AbstractSpan], result: GraphQLResponse[CalibanError]): Unit = {
    span.foreach(a => AgentUtils.stopAsync(a))

    if result.errors.nonEmpty then {
      val ex: Option[CalibanError] = result.errors.headOption
      span.foreach(_.log(ex.getOrElse(CalibanError.ExecutionError("Effect failure"))))
    } else if result.data == null then {
      span.foreach(_.log(CalibanError.ExecutionError("Effect failure")))
    }

    ContextManager.stopSpan()
  }

  def beforeOverallGraphQLRequest(graphQLRequest: GraphQLRequest): Option[AbstractSpan] =
    checkRequest(graphQLRequest) {
      val opName =
        CalibanPluginConfig.Plugin.CalibanV2.URL_PREFIX + "wrap-overall"
      val span = ContextManager.createLocalSpan(opName)
      Tags.LOGIC_ENDPOINT.set(span, Tags.VAL_LOCAL_SPAN_AS_LOGIC_ENDPOINT)
      AgentUtils.prepareAsync(span)
      SpanLayer.asHttp(span)
      span.setComponent(ComponentsDefine.GRAPHQL)
      Some(span)
    }

  def beforeGraphQLRequest(graphQLRequest: GraphQLRequest): Option[AbstractSpan] =
    checkRequest(graphQLRequest) {
      val opName =
        CalibanPluginConfig.Plugin.CalibanV2.URL_PREFIX + "wrap-request"
      val span = ContextManager.createLocalSpan(opName)
      Tags.LOGIC_ENDPOINT.set(span, Tags.VAL_LOCAL_SPAN_AS_LOGIC_ENDPOINT)
      AgentUtils.prepareAsync(span)
      SpanLayer.asHttp(span)
      span.setComponent(ComponentsDefine.GRAPHQL)
      Some(span)
    }

  def beforeParseQuery(query: String): Option[AbstractSpan] =
    val opName =
      CalibanPluginConfig.Plugin.CalibanV2.URL_PREFIX + "wrap-parse"
    val span = ContextManager.createLocalSpan(opName)
    Tags.LOGIC_ENDPOINT.set(span, Tags.VAL_LOCAL_SPAN_AS_LOGIC_ENDPOINT)
    AgentUtils.prepareAsync(span)

    SpanLayer.asHttp(span)
    span.setComponent(ComponentsDefine.GRAPHQL)
    Some(span)

  def beforeValidate(doc: Document): Option[AbstractSpan] =
    val opName =
      CalibanPluginConfig.Plugin.CalibanV2.URL_PREFIX + "wrap-validate"
    val span = ContextManager.createLocalSpan(opName)
    Tags.LOGIC_ENDPOINT.set(span, Tags.VAL_LOCAL_SPAN_AS_LOGIC_ENDPOINT)
    AgentUtils.prepareAsync(span)
    SpanLayer.asHttp(span)
    span.setComponent(ComponentsDefine.GRAPHQL)
    Some(span)

  def beforeExecutorExecuteRequest(executionRequest: ExecutionRequest): Option[AbstractSpan] =
    val opName =
      CalibanPluginConfig.Plugin.CalibanV2.URL_PREFIX + "wrap-executeRequest"
    val span = ContextManager.createLocalSpan(opName)
    Tags.LOGIC_ENDPOINT.set(span, Tags.VAL_LOCAL_SPAN_AS_LOGIC_ENDPOINT)
    AgentUtils.prepareAsync(span)
    SpanLayer.asHttp(span)
    span.setComponent(ComponentsDefine.GRAPHQL)
    Some(span)

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

  def unsafeRun[A](z: ZIO[Any, Any, A]): A =
    Try(Unsafe.unsafe { u ?=>
      Runtime.default.unsafe.run(z).getOrThrowFiberFailure()
    }).getOrElse(null.asInstanceOf[A])

  private def getOperationName(graphQLRequest: GraphQLRequest) =
    val tryOp: Try[String] = Try {
      val docOpName =
        unsafeRun(Parser.parseQuery(graphQLRequest.query.get)).operationDefinitions
          .map(_.selectionSet.collectFirst {
            case Selection.Field(alias, name, arguments, directives, selectionSet, index) => alias.getOrElse(name)
          })
          .headOption
          .flatten
      val name = graphQLRequest.operationName.map(_.split("__", 2).toList).toList.flatten.headOption
      name.orElse(docOpName).getOrElse(DEFAULT_OP)
    }
    tryOp match
      case Failure(e) =>
        AgentUtils.logError(e)
        DEFAULT_OP
      case Success(value) => value
