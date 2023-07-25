package org.bitlap.skywalking.apm.plugin.zcommon

import zio.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/7/25
 */
final class TracingSupervisor extends Supervisor[Any] {

  override def value(implicit trace: Trace): UIO[Any] = ZIO.unit

  override def onResume[E, A](fiber: Fiber.Runtime[E, A])(implicit unsafe: Unsafe): Unit = super.onResume(fiber)

  override def onStart[R, E, A](
    environment: ZEnvironment[R],
    effect: ZIO[R, E, A],
    parent: Option[Fiber.Runtime[Any, Any]],
    fiber: Fiber.Runtime[E, A]
  )(implicit unsafe: Unsafe): Unit = ???

  override def onEnd[R, E, A](value: Exit[E, A], fiber: Fiber.Runtime[E, A])(implicit unsafe: Unsafe): Unit = ???

  override def onSuspend[E, A](fiber: Fiber.Runtime[E, A])(implicit unsafe: Unsafe): Unit = super.onSuspend(fiber)

}
