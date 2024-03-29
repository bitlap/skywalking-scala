package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx

import scalapb.zio_grpc.*
import scalapb.zio_grpc.server.*

import net.bytebuddy.description.method.MethodDescription

import io.grpc.*

import org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.define.ZioGrpcServerCallInstrumentation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ZioGrpcServerCallInstrumentationSpec extends AnyFlatSpec with Matchers {

  "test sendMessage$extension MethodMatch" should "ok" in {
    val matcher =
      ZioGrpcServerCallInstrumentation.methodInterceptors(
        ZioGrpcServerCallInstrumentation.SEND_MESSAGE_METHOD_INTERCEPTOR
      )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ZServerCall[?]].getMethod(
        "sendMessage",
        classOf[Object]
      )
    )

    matcher.matches(method) shouldEqual true
  }

  "test close$extension MethodMatch" should "ok" in {
    val matcher = ZioGrpcServerCallInstrumentation.methodInterceptors(
      ZioGrpcServerCallInstrumentation.CLOSE_METHOD_INTERCEPTOR
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ZServerCall[?]].getMethod(
        "close",
        classOf[Status],
        classOf[Metadata]
      )
    )

    matcher.matches(method) shouldEqual true
  }
}
