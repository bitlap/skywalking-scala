package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

import java.util.concurrent.*

import scala.util.Try

import io.grpc.MethodDescriptor

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/15
 */
private[ziogrpc] final case class ZioGrpcContext(
  contextSnapshot: ContextSnapshot,
  asyncSpan: AbstractSpan,
  methodDescriptor: MethodDescriptor[?, ?]
)

object ZioGrpcContext:

  // FIXME
  private final val cache = new LinkedBlockingQueue[ZioGrpcContext]()

  def offer(value: ZioGrpcContext): Boolean = Try(cache.offer(value)).getOrElse(false)

  def poll: ZioGrpcContext = Try(cache.poll()).getOrElse(null.asInstanceOf[ZioGrpcContext])

  def peek: ZioGrpcContext = Try(cache.peek()).getOrElse(null.asInstanceOf[ZioGrpcContext])
