package org.bitlap.skywalking.apm.plugin.ziogrpc

import java.util.concurrent.*

import scala.util.Try

import io.grpc.MethodDescriptor

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.bitlap.skywalking.apm.plugin.ziogrpc.InterceptorSendMessageThreadContext.cache

/** TODO remove
 *  @author
 *    梦境迷离
 *  @version 1.0,2023/5/15
 */
private[ziogrpc] final case class InterceptorThreadContext(
  contextSnapshot: ContextSnapshot,
  asyncSpan: AbstractSpan,
  methodDescriptor: MethodDescriptor[?, ?],
  activeSpan: Option[AbstractSpan] = None
)

object InterceptorSendMessageThreadContext:

  private final val cache = new ConcurrentHashMap[String, LinkedBlockingQueue[InterceptorThreadContext]]()

  def offer(string: String, value: InterceptorThreadContext): Boolean = this.synchronized {
    Try {
      cache.putIfAbsent(string, new LinkedBlockingQueue[InterceptorThreadContext]())
      cache.get(string).offer(value)
    }.getOrElse(false)
  }

  def poll(string: String): InterceptorThreadContext = Try {
    val q   = cache.get(string)
    val ret = q.poll()
    if q.isEmpty then cache.remove(string)
    ret
  }.getOrElse(null.asInstanceOf[InterceptorThreadContext])

end InterceptorSendMessageThreadContext

object InterceptorCloseThreadContext:

  private final val cache = new ConcurrentHashMap[String, LinkedBlockingQueue[InterceptorThreadContext]]()

  def offer(string: String, value: InterceptorThreadContext): Boolean = this.synchronized {
    Try {
      cache.putIfAbsent(string, new LinkedBlockingQueue[InterceptorThreadContext]())
      cache.get(string).offer(value)
    }.getOrElse(false)
  }

  def poll(string: String): InterceptorThreadContext = Try {
    val q   = cache.get(string)
    val ret = q.poll()
    if q.isEmpty then cache.remove(string)
    ret
  }.getOrElse(null.asInstanceOf[InterceptorThreadContext])

end InterceptorCloseThreadContext
