package org.bitlap.skywalking.apm.plugin.ziogrpc

import java.util.concurrent.*

import scala.util.Try

import io.grpc.MethodDescriptor

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan

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

  private final val cache = new LinkedBlockingQueue[InterceptorThreadContext]()

  def offer(value: InterceptorThreadContext): Boolean = Try(cache.offer(value)).getOrElse(false)

  def poll: InterceptorThreadContext = Try(cache.poll()).getOrElse(null.asInstanceOf[InterceptorThreadContext])

end InterceptorSendMessageThreadContext

object InterceptorCloseThreadContext:

  private final val cache = new LinkedBlockingQueue[InterceptorThreadContext]()

  def offer(value: InterceptorThreadContext): Boolean = Try(cache.offer(value)).getOrElse(false)

  def poll: InterceptorThreadContext = Try(cache.poll()).getOrElse(null.asInstanceOf[InterceptorThreadContext])

end InterceptorCloseThreadContext
