package org.bitlap.skywalking.apm.plugin.zcommon

import java.text.SimpleDateFormat

import scala.util.*

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.logging.api.*
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance
import org.bitlap.skywalking.apm.plugin.common.*
import org.bitlap.skywalking.apm.plugin.zcommon

import _root_.zio.*
import _root_.zio.internal.FiberRuntime

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
object TagUtils:

  final lazy val fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  private def formatDate(long: Long) = fmt.format(long)

  def setZioTags(span: AbstractSpan, fiberId: FiberId.Runtime, objInst: EnhancedInstance): Unit =
    Try {
      CustomTag.FiberId.tag.set(span, fiberId.id.toString)
      CustomTag.FiberStartTime.tag.set(span, formatDate(fiberId.startTimeMillis))
      CustomTag.FiberLocation.tag.set(span, fiberId.location.toString)
      CustomTag.FiberClassName.tag.set(span, objInst.getClass.getName)
      CustomTag.CurrentThread.tag.set(span, Thread.currentThread().getName)
    } match
      case Failure(ex) => span.errorOccurred.log(ex)
      case Success(_)  =>
  end setZioTags

end TagUtils
