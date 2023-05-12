package org.bitlap.skywalking.apm.plugin.caliban.v2x

import java.lang.reflect.Method

import caliban.*
import caliban.execution.QueryExecution

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.apache.skywalking.apm.agent.test.tools.*
import org.hamcrest.CoreMatchers.is
import org.junit.*
import org.junit.Assert.{ assertEquals, assertThat }
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/11
 */
class CalibanInterceptorSpec {

  @Mock var methodInterceptResult: MethodInterceptResult = _
  @Rule def rule: MockitoRule                            = MockitoJUnit.rule
  @Rule def serviceRule                                  = new AgentServiceRule

  val calibanInterceptor: CalibanInterceptor = new CalibanInterceptor

  private val enhancedInstance = new EnhancedInstance() {
    private var obj: Object                = null
    override def getSkyWalkingDynamicField = obj

    override def setSkyWalkingDynamicField(value: Object): Unit =
      obj = value
  }

  val argTypes: Array[Class[_]] = Array(
    classOf[GraphQLRequest],
    classOf[Boolean],
    classOf[Boolean],
    classOf[QueryExecution],
    classOf[Any]
  )

  val method = classOf[GraphQLInterpreter[_, _]].getMethod(
    "executeRequest",
    argTypes: _*
  )

  @Test
  def testHandleRequestWithoutOperationName(): Unit = {
    val args: Array[Object] = Array(
      GraphQLRequest(
        Option("""
          |mutation {
          |  starLakeInsertMetric(
          |    id: "string"
          |    key: "string"
          |    level: 1
          |    canonicalName: "string"
          |    displayName: "string"
          |    granularity: "string"
          |    measureUnit: "string"
          |    director: "1"
          |    counterpart: "1"
          |    expectAt: "1"
          |    parentId: "string"
          |    expr: "string"
          |    businessExplain: "string"
          |    dimensions: ["string"]
          |    sqlExample: "string"
          |  ) {
          |    data {
          |      effectRows
          |    }
          |    statusCode
          |    msg
          |  }
          |}""".stripMargin),
        None,
        None,
        None
      ),
      Boolean.box(false),
      Boolean.box(false),
      QueryExecution.Sequential
    )
    calibanInterceptor.beforeMethod(enhancedInstance, method, args, argTypes, methodInterceptResult)
    calibanInterceptor.afterMethod(enhancedInstance, method, args, argTypes, zio.ZIO.succeed(methodInterceptResult))

    // FIXME
    val operation = ContextManager.activeSpan().getOperationName

    assertEquals("starLakeInsertMetric", operation)
  }

  @Test
  def testHandleRequest(): Unit = {
    val args: Array[Object] = Array(
      GraphQLRequest(Option("query"), Option("operationName"), None, None),
      Boolean.box(false),
      Boolean.box(false),
      QueryExecution.Sequential
    )
    calibanInterceptor.beforeMethod(enhancedInstance, method, args, argTypes, methodInterceptResult)
    calibanInterceptor.afterMethod(enhancedInstance, method, args, argTypes, zio.ZIO.succeed(methodInterceptResult))

    val operation = ContextManager.activeSpan().getOperationName

    assertEquals("Unknown", operation)

  }

}
