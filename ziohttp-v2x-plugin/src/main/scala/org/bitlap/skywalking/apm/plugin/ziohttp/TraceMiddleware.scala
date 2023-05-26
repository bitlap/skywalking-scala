package org.bitlap.skywalking.apm.plugin.ziohttp

import scala.util.Try

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.common.Utils

import zhttp.http.*
import zhttp.http.middleware.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/17
 */
object TraceMiddleware:

  final lazy val middleware: HttpMiddleware[Any, Throwable] =
    Middleware
      .interceptPatch(req => beforeRequest(req)) { case (response, span) =>
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
    if Utils.ignorePrefix(ZioHttpPluginConfig.Plugin.ZioHttp.IGNORE_URL_PREFIXES, uri.path.encode) then {
      return null
    }
    val contextCarrier = new ContextCarrier
    val span: AbstractSpan =
      ContextManager.createEntrySpan(s"${request.method.toString()}:${uri.path.toString}", contextCarrier)
    Tags.URL.set(span, request.host.map(String.valueOf).getOrElse("") + request.path.encode)
    Tags.HTTP.METHOD.set(span, request.method.toString())
    SpanLayer.asHttp(span)
    span
  end beforeRequest

end TraceMiddleware
