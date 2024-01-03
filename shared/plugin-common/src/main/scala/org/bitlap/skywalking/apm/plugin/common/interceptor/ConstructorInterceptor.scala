package org.bitlap.skywalking.apm.plugin.common.interceptor

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

final class ConstructorInterceptor extends InstanceConstructorInterceptor:

  override def onConstruct(objInst: EnhancedInstance, allArguments: Array[Object]): Unit =
    if ContextManager.isActive then objInst.setSkyWalkingDynamicField(ContextManager.capture)
