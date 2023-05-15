package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.forward

import io.grpc.*
import io.grpc.ServerCall.Listener
import io.grpc.Status.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.bitlap.skywalking.apm.plugin.common.InterceptorDSL
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.OperationNameFormatUtils
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.call.ChannelActions

/** TODO
 *
 *  This class is used to implement a native interceptor for grpc, but currently zio grpc does not support it.
 *
 *  It is implemented by intercepting each zio grpc method.
 *
 *  @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
final class TracingServerCall[REQUEST, RESPONSE](
  delegate: ServerCall[REQUEST, RESPONSE],
  contextSnapshot: ContextSnapshot,
  asyncSpan: AbstractSpan
) extends ForwardingServerCall.SimpleForwardingServerCall[REQUEST, RESPONSE](delegate):

  override def sendMessage(message: RESPONSE): Unit =
    val span = ChannelActions.beforeSendMessage(contextSnapshot, getMethodDescriptor)
    if (span == null) {
      delegate.sendMessage(message)
      return
    }

    InterceptorDSL.continuedSnapshot(contextSnapshot) {
      delegate.sendMessage(message)
    }
  end sendMessage

  override def close(status: Status, trailers: Metadata): Unit =
    val span = ChannelActions.beforeClose(contextSnapshot, getMethodDescriptor)
    ChannelActions.afterClose(status, asyncSpan, span)(delegate.close(status, trailers))
  end close

end TracingServerCall
