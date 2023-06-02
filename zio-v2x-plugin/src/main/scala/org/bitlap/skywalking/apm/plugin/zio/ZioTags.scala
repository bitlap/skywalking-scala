package org.bitlap.skywalking.apm.plugin.zio

import java.text.SimpleDateFormat

import scala.util.*

import zio.*

import org.apache.skywalking.apm.agent.core.context.tag.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.bitlap.skywalking.apm.plugin.common.ScalaTags

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
object ZioTags:

  final lazy val fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  private def formatDate(long: Long) = fmt.format(long)

  def setZioTags(span: AbstractSpan, fiberId: FiberId.Runtime): Unit =
    Try {
      ScalaTags.FiberId.tag.set(span, fiberId.id.toString)
      ScalaTags.FiberStartTime.tag.set(span, formatDate(fiberId.startTimeMillis))
      ScalaTags.FiberLocation.tag.set(span, fiberId.location.toString)
    } match
      case Failure(ex) => span.errorOccurred.log(ex)
      case Success(_)  =>
  end setZioTags

end ZioTags
