package org.bitlap.skywalking.apm.plugin.ziogrpc

import java.util.concurrent.*

import scala.util.Try

import io.grpc.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan

/** TODO remove
 *  @author
 *    梦境迷离
 *  @version 1.0,2023/5/15
 */
private[ziogrpc] final case class OperationContext(
  selfCall: ServerCall[?, ?] = null,
  methodDescriptor: MethodDescriptor[?, ?] = null,
  asyncSpan: AbstractSpan = null,
  activeSpan: Option[AbstractSpan] = None,
  contextSnapshot: ContextSnapshot = null
)

object GrpcOperationQueue:

  private final val cacheClose = new ConcurrentHashMap[ServerCall[?, ?], OperationContext]()

  def put(call: ServerCall[?, ?], ctx: OperationContext): Boolean =
    Try {
      cacheClose.put(call, ctx)
      true
    }.getOrElse(false)

  def get(call: ServerCall[?, ?]): OperationContext =
    Try {
      cacheClose.get(call)
    }.getOrElse(null.asInstanceOf[OperationContext])

  def remove(call: ServerCall[?, ?]): OperationContext =
    Try {
      cacheClose.remove(call)
    }.getOrElse(null.asInstanceOf[OperationContext])

end GrpcOperationQueue
