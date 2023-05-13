package org.bitlap.skywalking.apm.plugin.common

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
object InterceptorUtils:

  def unsafeRunZIO[A](z: ZIO[Any, Any, A]): A =
    Unsafe.unsafe { runtime ?=>
      Runtime.default.unsafe.run(z).getOrThrowFiberFailure()
    }

  def dealExceptionF(t: Throwable)(implicit span: AbstractSpan): UIO[Unit] =
    ZIO.attempt(span.errorOccurred.log(t)).ignore

  def dealException(t: Throwable)(implicit span: AbstractSpan): Unit =
    span.errorOccurred.log(t)

  def handleMethodException(objInst: EnhancedInstance, allArguments: Array[Object], t: Throwable)(
    handler: Array[Object] => Unit
  ): Unit = {
    if (objInst.getSkyWalkingDynamicField == null || !objInst.getSkyWalkingDynamicField.isInstanceOf[AbstractSpan])
      return
    implicit val span = objInst.getSkyWalkingDynamicField.asInstanceOf[AbstractSpan]
    dealException(t)
    handler(allArguments)
  }

  def handleMethodExit(objInst: EnhancedInstance, ret: Object)(
    handler: Object => AbstractSpan ?=> ZIO[_, _, _]
  ): Object = {
    if (objInst.getSkyWalkingDynamicField == null || !objInst.getSkyWalkingDynamicField.isInstanceOf[AbstractSpan])
      return ret
    if (!ret.isInstanceOf[ZIO[_, _, _]])
      return ret

    implicit val span = objInst.getSkyWalkingDynamicField.asInstanceOf[AbstractSpan]
    val retObject     = handler(ret)

    if (!retObject.isInstanceOf[ZIO[_, _, _]])
      return retObject

    retObject
      .ensuring(
        ZIO.attempt {
          span.asyncFinish()
          ContextManager.stopSpan(span)
        }
          .catchAllCause(t => InterceptorUtils.dealExceptionF(t.squash))
      )
  }
