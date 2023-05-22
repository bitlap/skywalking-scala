package org.bitlap.skywalking.apm.plugin.zio

import java.lang.reflect.Method

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/18
 */
object Utils:

  def generateOperationName(objInst: EnhancedInstance, method: Method) =
    s"Zio/${objInst.getClass.getTypeName}/${method.getName}"

  def generateOperationName =
    s"ZioFiber/${Thread.currentThread.getName}"

  def blockingOperationName = "ZioBlockingRunnableWrapper/" + Thread.currentThread.getName

  def operationName = "ZioRunnableWrapper/" + Thread.currentThread.getName
