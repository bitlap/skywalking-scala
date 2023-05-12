package org.bitlap.skywalking.apm.plugin.caliban.v2x

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import caliban.{ GraphQLInterpreter, GraphQLRequest, InputValue }
import caliban.execution.QueryExecution

import zio.Trace

import org.bitlap.skywalking.apm.plugin.caliban.v2x.define.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
class CalibanInstrumentationSpec extends AnyFlatSpec with Matchers {

  "testMethodMatch" should "ok" in {
    val matcher = CalibanInstrumentation.getCalibanExecuteRequestMethod
    val method = new MethodDescription.ForLoadedMethod(
      classOf[GraphQLInterpreter[_, _]].getMethod(
        "executeRequest",
        classOf[GraphQLRequest],
        classOf[Boolean],
        classOf[Boolean],
        classOf[QueryExecution],
        classOf[Any]
      )
    )

    matcher.matches(method) shouldEqual true
  }
}
