package org.bitlap.skywalking.apm.plugin.caliban.v2

import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher

import caliban.*
import caliban.execution.QueryExecution
import caliban.wrappers.ApolloTracing

import zio.*

import org.apache.skywalking.apm.agent.core.plugin.`match`.*
import org.bitlap.skywalking.apm.plugin.caliban.v2.define.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
class CalibanInstrumentationSpec extends AnyFlatSpec with Matchers {

  "test wrapper" should "ok" in {
    val matcher = CalibanWrapperInstrumentation.getMethod
    val method = new MethodDescription.ForLoadedMethod(
      classOf[ApolloTracing.type].getMethod(
        "apolloTracing"
      )
    )

    matcher.matches(method) shouldEqual true
  }

  "test exec" should "ok" in {
    val matcher = CalibanInstrumentation.getMethod
    val method = new MethodDescription.ForLoadedMethod(
      classOf[GraphQLInterpreter[?, ?]].getMethod(
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

  "test class match" should "ok" in {
    val clazz = new caliban.GraphQLInterpreter[Any, Any] {
      override def check(query: String)(implicit trace: Trace): IO[CalibanError, Unit] = ???
      override def executeRequest(
        request: GraphQLRequest,
        skipValidation: Boolean,
        enableIntrospection: Boolean,
        queryExecution: QueryExecution
      )(implicit trace: Trace): URIO[Any, GraphQLResponse[Any]] = ???

    }
    clazz.getClass.getInterfaces.apply(0).getName shouldEqual "caliban.GraphQLInterpreter"

  }
}
