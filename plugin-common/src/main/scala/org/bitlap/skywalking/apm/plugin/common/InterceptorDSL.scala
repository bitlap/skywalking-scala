package org.bitlap.skywalking.apm.plugin.common

import scala.util.Try

import zio.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
object InterceptorDSL:

  def unsafeRun[A](z: ZIO[Any, Any, A]): A =
    Try(Unsafe.unsafeCompat { implicit u =>
      Runtime.default.unsafe.run(z).getOrThrowFiberFailure()
    }).getOrElse(null.asInstanceOf[A])

  def handleExceptionF(t: Throwable)(implicit span: AbstractSpan): UIO[Unit] =
    ZIO.attempt(span.errorOccurred.log(t)).ignore

  def handleException(t: Throwable)(implicit span: AbstractSpan): Unit = span.errorOccurred.log(t)

  def handleMethodException(objInst: EnhancedInstance, allArguments: Array[Object], t: Throwable)(
    handler: Array[Object] => Unit
  ): Unit = {
    if (objInst.getSkyWalkingDynamicField == null || !objInst.getSkyWalkingDynamicField.isInstanceOf[AbstractSpan])
      return
    implicit val span: AbstractSpan = objInst.getSkyWalkingDynamicField.asInstanceOf[AbstractSpan]
    handleException(t)
    handler(allArguments)
  }

  def continuedSnapshot(contextSnapshot: ContextSnapshot)(effect: => Unit): Unit =
    try
      ContextManager.continued(contextSnapshot)
      effect
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
    } finally ContextManager.stopSpan()

  def continuedSnapshot(contextSnapshot: ContextSnapshot, asyncSpan: AbstractSpan)(effect: => Unit): Unit =
    try
      ContextManager.continued(contextSnapshot)
      try asyncSpan.asyncFinish
      catch {
        case e: Throwable =>
          if (ContextManager.isActive) ContextManager.activeSpan.log(e)
      }
      effect
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
    } finally ContextManager.stopSpan()
