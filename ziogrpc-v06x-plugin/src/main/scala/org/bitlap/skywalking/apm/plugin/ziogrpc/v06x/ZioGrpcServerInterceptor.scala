package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

import java.lang.reflect.Method

import scalapb.zio_grpc.{ RequestContext, SafeMetadata }
import scalapb.zio_grpc.server.{ CallDriver, ZServerCall }

import io.grpc.*
import io.grpc.ServerCall.Listener
import io.grpc.Status.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.Tags
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.network.language.agent.v3.MeterData
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine
import org.apache.skywalking.apm.util.StringUtil
import org.bitlap.skywalking.apm.plugin.common.InterceptorUtils
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.Constants.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.forward.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
class ZioGrpcServerInterceptor extends InstanceMethodsAroundInterceptor, InstanceConstructorInterceptor:

  override def onConstruct(objInst: EnhancedInstance, allArguments: Array[Object]): Unit =
    objInst.setSkyWalkingDynamicField(Map(RUNTIME -> allArguments(0), MK_DRIVER -> allArguments(1)))

  override def beforeMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    result: MethodInterceptResult
  ): Unit =
    val call           = allArguments(0).asInstanceOf[ServerCall[?, ?]]
    val headers        = allArguments(1).asInstanceOf[Metadata]
    val contextCarrier = new ContextCarrier
    var next           = contextCarrier.items
    while (next.hasNext) {
      next = next.next
      val contextValue = headers.get(Metadata.Key.of(next.getHeadKey, Metadata.ASCII_STRING_MARSHALLER))
      if (!StringUtil.isEmpty(contextValue)) next.setHeadValue(contextValue)
    }
    val span = ContextManager.createEntrySpan(
      OperationNameFormatUtils.formatOperationName(call.getMethodDescriptor),
      contextCarrier
    )
    span.setComponent(ComponentsDefine.GRPC)
    span.setLayer(SpanLayer.RPC_FRAMEWORK)

    val contextSnapshot = ContextManager.capture
    span.prepareForAsync()
    ContextManager.stopSpan(span)

    val originField = objInst.getSkyWalkingDynamicField.asInstanceOf[Map[String, Any]]

    objInst.setSkyWalkingDynamicField(
      originField ++ Seq(CONTEXT_SNAPSHOT -> contextSnapshot, ACTIVE_SPAN -> span).toMap
    )

  end beforeMethod

  override def afterMethod(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    ret: Object
  ): Object =
    if (objInst.getSkyWalkingDynamicField == null || !objInst.getSkyWalkingDynamicField.isInstanceOf[Map[?, ?]])
      return ret
    val map             = objInst.getSkyWalkingDynamicField.asInstanceOf[Map[String, Any]]
    val call            = allArguments(0).asInstanceOf[ServerCall[Any, Any]]
    val headers         = allArguments(1).asInstanceOf[Metadata]
    val contextSnapshot = map.get(CONTEXT_SNAPSHOT).orNull.asInstanceOf[ContextSnapshot]
    val asyncSpan       = map.get(ACTIVE_SPAN).orNull.asInstanceOf[AbstractSpan]
    // TODO copy from zio-grpc
    // zio-grpc not support grpc interceptor
    val mkDriver =
      map.get(MK_DRIVER).orNull.asInstanceOf[(ZServerCall[Any], RequestContext) => URIO[Any, CallDriver[Any, Any]]]
    val runtime = map.get(RUNTIME).orNull.asInstanceOf[Runtime[Any]]
    val zioCall = new ZServerCall(new TracingServerCall(call, contextSnapshot, asyncSpan))
    val runner = for {
      responseMetadata <- SafeMetadata.make
      driver <- SafeMetadata.fromMetadata(headers).flatMap { md =>
        mkDriver(zioCall, RequestContext.fromServerCall(md, responseMetadata, call))
      }
      _ <- driver.run.forkDaemon
    } yield new TracingServerCallListener(
      driver.listener,
      call.getMethodDescriptor,
      contextSnapshot,
      asyncSpan
    )
    Unsafe.unsafeCompat { implicit u =>
      runtime.unsafe.run(runner).getOrThrowFiberFailure()
    }

  end afterMethod

  override def handleMethodException(
    objInst: EnhancedInstance,
    method: Method,
    allArguments: Array[Object],
    argumentsTypes: Array[Class[_]],
    t: Throwable
  ): Unit = InterceptorUtils.handleMethodException(objInst, allArguments, t)(_ => ())

end ZioGrpcServerInterceptor
