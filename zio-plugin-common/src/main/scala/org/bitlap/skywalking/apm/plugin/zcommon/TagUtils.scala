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

  private val LOGGER = LogManager.getLogger(classOf[TagUtils.type])

  final lazy val fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  private def formatDate(long: Long) = fmt.format(long)

  def setZioTags(span: AbstractSpan, fiberId: FiberId.Runtime, objInst: EnhancedInstance): Unit =
    Try {
      CustomTag.FiberId.tag.set(span, fiberId.id.toString)
      CustomTag.FiberStartTime.tag.set(span, formatDate(fiberId.startTimeMillis))
      CustomTag.FiberLocation.tag.set(span, fiberId.location.toString)
      CustomTag.FiberClassName.tag.set(span, objInst.getClass.getName)
    } match
      case Failure(ex) => span.errorOccurred.log(ex)
      case Success(_)  =>
  end setZioTags

  def setSpanZioTag(arg: Object, objInst: EnhancedInstance)(implicit span: AbstractSpan) =
    arg match {
      case fiber: FiberRuntime[?, ?] =>
        TagUtils.setZioTags(span, fiber.id, objInst)
      case _ =>
        LOGGER.debug(s"Invalid FiberRuntime: ${objInst.getClass.getName} ${objInst.toString}")
    }

  def getExecutorType(className: String): zcommon.ExecutorType =
    ExecutorType.values.find(c => className.startsWith(c.classNamePrefix)).getOrElse(ExecutorType.Executor)
end TagUtils
