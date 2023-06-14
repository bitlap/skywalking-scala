package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor

import io.grpc.*

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.OperationContext

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioGrpcServerCallConstructorInterceptor extends InstanceConstructorInterceptor:

  override def onConstruct(objInst: EnhancedInstance, allArguments: Array[Object]): Unit =
    objInst.setSkyWalkingDynamicField(OperationContext(selfCall = allArguments(0).asInstanceOf[ServerCall[?, ?]]))
