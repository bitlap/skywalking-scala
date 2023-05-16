package org.bitlap.skywalking.apm.plugin.zio

import org.apache.skywalking.apm.agent.core.context.ContextManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
final class ZioFiberRuntimeInitialInterceptor extends InstanceConstructorInterceptor:

  override def onConstruct(objInst: EnhancedInstance, allArguments: Array[Object]): Unit =
    if (ContextManager.isActive) objInst.setSkyWalkingDynamicField(ContextManager.capture)
