package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

import scalapb.zio_grpc.*
import scalapb.zio_grpc.server.*

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import io.grpc.*

import zio.*

import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.define.ZioGrpcServerInstrumentation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
class ZioGrpcServerInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch" should "ok" in {
    val matcher = ZioGrpcServerInstrumentation.getMethod
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ZServerCallHandler[_, _, _]].getMethod(
        "startCall",
        classOf[ServerCall[_, _]],
        classOf[Metadata]
      )
    )
    matcher.matches(method) shouldEqual true
  }
}
