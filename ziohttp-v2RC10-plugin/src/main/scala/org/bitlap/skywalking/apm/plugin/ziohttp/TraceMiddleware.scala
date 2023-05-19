package org.bitlap.skywalking.apm.plugin.ziohttp

import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

import scala.util.Try

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.InterceptorDSL

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
      }
      ContextManager.stopSpan()
    }.getOrElse(())
  end afterRequest

  private def beforeRequest(request: Request): AbstractSpan =
    val uri            = request.url
    val host           = request.host
    val contextCarrier = new ContextCarrier
    val span: AbstractSpan =
      ContextManager.createExitSpan(uri.path.toString, contextCarrier, String.valueOf(host.orNull))
    Tags.URL.set(span, String.valueOf(request.origin.orNull))
    Tags.HTTP.METHOD.set(span, request.method.toString())
    SpanLayer.asHttp(span)
    span
  end beforeRequest

end TraceMiddleware
