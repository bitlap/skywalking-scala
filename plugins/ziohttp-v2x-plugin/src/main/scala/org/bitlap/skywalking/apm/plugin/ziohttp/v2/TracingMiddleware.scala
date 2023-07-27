package org.bitlap.skywalking.apm.plugin.ziohttp.v2

import scala.jdk.CollectionConverters.*
import scala.util.Try

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.agent.core.util.CollectionUtil
import org.apache.skywalking.apm.util.StringUtil
import org.bitlap.skywalking.apm.plugin.common.*

import zhttp.http.*
import zhttp.http.middleware.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/17
 */
object TracingMiddleware:

  final lazy val middleware: HttpMiddleware[Any, Throwable] =
    Middleware.interceptPatch(req => beforeRequest(req)) { case (response, span) =>
      afterRequest(span, response)
      Patch.empty
    }

  private def afterRequest(span: AbstractSpan, response: Response): Unit =
    Try {
      if span != null && response != null then {
        Tags.HTTP_RESPONSE_STATUS_CODE.set(span, response.status.code)

        if response.status.code >= 400 then {
          span.errorOccurred()
        }
        ContextManager.stopSpan()
      }
    }.getOrElse(())
  end afterRequest

  private def beforeRequest(request: Request): AbstractSpan =
    val uri = request.url
    if AgentUtils.ignorePrefix(ZioHttpPluginConfig.Plugin.ZioHttpV2.IGNORE_URL_PREFIXES, uri.path.encode) then {
      return null
    }
    val contextCarrier = new ContextCarrier
    val span: AbstractSpan =
      ContextManager.createEntrySpan(s"${uri.path.toString}", contextCarrier)
    Tags.URL.set(span, request.host.map(String.valueOf).getOrElse("") + request.path.encode)
    Tags.HTTP.METHOD.set(span, request.method.toString())
    if ZioHttpPluginConfig.Plugin.ZioHttpV2.COLLECT_HTTP_PARAMS then {
      val tagValue = collectHttpParam(request)
      Tags.HTTP.PARAMS.set(span, tagValue)
    }
    SpanLayer.asHttp(span)
    span
  end beforeRequest

  private def collectHttpParam(request: Request): String = {
    val tagValue = CollectionUtil.toString(request.url.queryParams.map(kv => kv._1 -> kv._2.toArray).asJava)
    if ZioHttpPluginConfig.Plugin.ZioHttpV2.HTTP_PARAMS_LENGTH_THRESHOLD > 0 then
      StringUtil.cut(tagValue, ZioHttpPluginConfig.Plugin.ZioHttpV2.HTTP_PARAMS_LENGTH_THRESHOLD)
    else tagValue
  }

end TracingMiddleware
