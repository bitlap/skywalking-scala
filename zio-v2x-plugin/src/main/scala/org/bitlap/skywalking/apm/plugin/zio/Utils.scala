package org.bitlap.skywalking.apm.plugin.zio

import java.lang.reflect.Method

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/18
 */
object Utils:

  def generateFiberOperationName =
    s"ZioFiberWrapper/${Thread.currentThread.getName}"

  def blockingOperationName = "ZioBlockingSubmitWrapper/" + Thread.currentThread.getName

end Utils
