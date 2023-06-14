package org.bitlap.skywalking.apm.plugin.ziogrpc

import scalapb.zio_grpc.*
import scalapb.zio_grpc.server.*

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import io.grpc.*

import zio.Trace

import org.bitlap.skywalking.apm.plugin.ziogrpc.v06testx.define.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
class ZioGrpcServerCallInstrumentationSpec extends AnyFlatSpec with Matchers {

  "test sendMessage$extension MethodMatch" should "ok" in {
    val matcher =
      ZioGrpcServerCallInstrumentation.methodInterceptors(
        ZioGrpcServerCallInstrumentation.INTERCEPTOR_SEND_MESSAGE_CLASS
      )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ZServerCall[?]].getMethod(
        "sendMessage$extension",
        classOf[ServerCall[?, ?]],
        classOf[Object]
      )
    )

    matcher.matches(method) shouldEqual true
  }

  "test close$extension MethodMatch" should "ok" in {
    val matcher = ZioGrpcServerCallInstrumentation.methodInterceptors(
      ZioGrpcServerCallInstrumentation.INTERCEPTOR_CLOSE_CLASS
    )
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ZServerCall[?]].getMethod(
        "close$extension",
        classOf[ServerCall[?, ?]],
        classOf[Status],
        classOf[Metadata]
      )
    )

    matcher.matches(method) shouldEqual true
  }
}
