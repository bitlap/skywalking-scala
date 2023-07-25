package org.bitlap.skywalking.apm.plugin.zcommon

import org.apache.skywalking.apm.agent.core.context.{ ContextManager, ContextSnapshot }

/** @author
 *    梦境迷离
 *  @version 1.0,2023/7/25
 */
final class FiberContext(_context: ContextSnapshot) {
  var context: ContextSnapshot = _context
  def onSuspend(): Unit =  {
    context = ContextManager.capture()
    // Reset context to avoid leaking it to other fibers
    ContextManager.stopSpan()
  }

  def onResume(): Any = {
  }
}

object FiberContext:
  def apply() = new FiberContext(ContextManager.capture())
