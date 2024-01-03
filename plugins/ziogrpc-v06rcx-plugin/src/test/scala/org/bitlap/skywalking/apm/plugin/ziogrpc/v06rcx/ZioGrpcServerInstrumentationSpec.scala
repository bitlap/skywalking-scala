package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx

import scalapb.zio_grpc.*
import scalapb.zio_grpc.server.*

import net.bytebuddy.description.method.MethodDescription

import io.grpc.*

import org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.define.ZioGrpcServerInstrumentation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ZioGrpcServerInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch" should "ok" in {
    val matcher = ZioGrpcServerInstrumentation.getMethod
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ZServerCallHandler[?, ?]].getMethod(
        "startCall",
        classOf[ServerCall[?, ?]],
        classOf[Metadata]
      )
    )
    matcher.matches(method) shouldEqual true
  }
}
