package org.bitlap.skywalking.apm.plugin.ziogrpc.v06x

import scalapb.zio_grpc.*
import scalapb.zio_grpc.client.ClientCalls

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import io.grpc.*

import zio.{ Trace, ZLayer }

import org.bitlap.skywalking.apm.plugin.ziogrpc.v06x.define.ZioGrpcServerInstrumentation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
class ZioGrpcServerInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch" should "ok" in {
    val matcher = ZioGrpcServerInstrumentation.getUnaryMethod
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ServerLayer.type].getMethod(
        "fromServiceList",
        classOf[() => ServerBuilder[_]],
        classOf[ServiceList[_]]
      )
    )
    matcher.matches(method) shouldEqual true
  }
}
