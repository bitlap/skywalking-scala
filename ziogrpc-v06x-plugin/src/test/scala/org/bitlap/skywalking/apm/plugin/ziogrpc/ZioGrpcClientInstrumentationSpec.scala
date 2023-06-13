package org.bitlap.skywalking.apm.plugin.ziogrpc

import scalapb.zio_grpc.*
import scalapb.zio_grpc.client.*

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import io.grpc.*

import zio.Trace

import org.bitlap.skywalking.apm.plugin.ziogrpc.define.ZioGrpcClientInstrumentation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
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
