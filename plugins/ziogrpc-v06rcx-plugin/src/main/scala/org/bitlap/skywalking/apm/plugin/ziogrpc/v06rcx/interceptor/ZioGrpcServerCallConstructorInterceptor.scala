package org.bitlap.skywalking.apm.plugin.ziogrpc.v06rcx.interceptor

import io.grpc.*

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.*
import org.bitlap.skywalking.apm.plugin.ziogrpc.common.Constants.*

final class ZioGrpcServerCallConstructorInterceptor extends InstanceConstructorInterceptor:

  override def onConstruct(objInst: EnhancedInstance, allArguments: Array[Object]): Unit =
    objInst.setSkyWalkingDynamicField(OperationContext(selfCall = allArguments(0).asInstanceOf[ServerCall[?, ?]]))
