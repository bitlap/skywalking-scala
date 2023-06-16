package org.bitlap.skywalking.apm.plugin.zcommon

import java.lang.reflect.Method

import scala.util.Try

import org.apache.skywalking.apm.agent.core.context.*

import _root_.zio.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
object ZUtils:

  def unsafeRun[A](z: ZIO[Any, Any, A]): A =
    Try(Unsafe.unsafe { u ?=>
      Runtime.default.unsafe.run(z).getOrThrowFiberFailure()
    }).getOrElse(null.asInstanceOf[A])

  def logError[E <: Throwable](cause: Cause[E]): Unit =
    if ContextManager.isActive then ContextManager.activeSpan.log(cause.squash)
