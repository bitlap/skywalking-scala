package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

import java.lang.reflect.Method

import scalapb.zio_grpc.*
import scalapb.zio_grpc.client.ClientCalls

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import io.grpc.{ CallOptions, MethodDescriptor, Status }
import io.grpc.Metadata
import io.grpc.binarylog.v1.Address
import io.grpc.protobuf.lite.ProtoLiteUtils

import zio.Trace
import zio.ZIO

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.agent.test.tools.*
import org.bitlap.skywalking.apm.plugin.common.InterceptorUtils
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.ZioGrpcClientInterceptor
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.define.ZioGrpcClientInstrumentation
import org.hamcrest.CoreMatchers.is
import org.junit.*
import org.junit.Assert.{ assertEquals, assertThat }
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import com.google.protobuf.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
class ZIoGrpcClientInterceptorSpec {

  @Mock var methodInterceptResult: MethodInterceptResult = _
  @Rule def rule: MockitoRule                            = MockitoJUnit.rule
  @Rule def serviceRule                                  = new AgentServiceRule

  val zioGrpcClientUnaryCallInterceptor: ZioGrpcClientInterceptor = new ZioGrpcClientInterceptor

  private val enhancedInstance = new EnhancedInstance() {
    private var obj: Object = null

    override def getSkyWalkingDynamicField = obj

    override def setSkyWalkingDynamicField(value: Object): Unit =
      obj = value
  }

  val argTypes: Array[Class[_]] = Array(
    classOf[ZChannel[_]],
    classOf[MethodDescriptor[_, _]],
    classOf[CallOptions],
    classOf[SafeMetadata],
    classOf[scala.Any]
  )

  val method = classOf[ClientCalls.type].getMethod(
    "unaryCall",
    argTypes: _*
  )

  @Test
  def testUnaryCall(): Unit = {
    val args: Array[Object] = Array(
      null.asInstanceOf[ZChannel[_]],
      MethodDescriptor
        .newBuilder()
        .setFullMethodName("session.SessionService/GetUserInfoByToken")
        .setType(MethodDescriptor.MethodType.UNARY)
        .setRequestMarshaller(
          ProtoLiteUtils
            .marshaller(
              DynamicMessage.newBuilder(Address.newBuilder().build()).build()
            )
        )
        .setResponseMarshaller(
          ProtoLiteUtils
            .marshaller(
              DynamicMessage.newBuilder(Address.newBuilder().build()).build()
            )
        )
        .build(),
      CallOptions.DEFAULT,
      InterceptorUtils.unsafeRunZIO(SafeMetadata.fromMetadata(new Metadata)),
      null
    )
    zioGrpcClientUnaryCallInterceptor.beforeMethod(
      enhancedInstance,
      method,
      args,
      argTypes,
      methodInterceptResult
    )
    zioGrpcClientUnaryCallInterceptor.afterMethod(
      enhancedInstance,
      method,
      args,
      argTypes,
      ZIO.succeed(null.asInstanceOf[ZIO[_, Status, _]])
    )

    val operation = ContextManager.activeSpan().getOperationName
    assertEquals("session.SessionService.getUserInfoByToken/client", operation)

  }

}
