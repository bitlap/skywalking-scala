package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx

import scalapb.zio_grpc.*

import net.bytebuddy.description.method.MethodDescription

import io.grpc.*

import org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.define.ZioGrpcClientInstrumentation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ZioGrpcClientInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch" should "ok" in {
    val matcher = ZioGrpcClientInstrumentation.getMethod
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ZChannel].getMethod(
        "newCall",
        classOf[MethodDescriptor[?, ?]],
        classOf[CallOptions]
      )
    )

    matcher.matches(method) shouldEqual true
  }
}
