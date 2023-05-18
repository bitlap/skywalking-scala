package org.bitlap.skywalking.apm.plugin.zio

import java.text.SimpleDateFormat

import scala.util.{ Failure, Success, Try }

import zio.*

import org.apache.skywalking.apm.agent.core.context.tag.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
object ZioTag:

  final lazy val fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  private def formatDate(long: Long) = fmt.format(long)

  private lazy val id: IntegerTag = new IntegerTag(100, "zio.fiber.id")
  private lazy val startTime      = new StringTag(101, "zio.fiber.startTime")
  private lazy val location       = new StringTag(102, "zio.fiber.location")

  def setZioTags(span: AbstractSpan, fiberId: FiberId.Runtime): Unit =
    Try {
      id.set(span, fiberId.id)
      startTime.set(span, formatDate(fiberId.startTimeMillis))
      location.set(span, fiberId.location.toString)
    } match
      case Failure(ex) => span.errorOccurred.log(ex)
      case Success(_)  =>
  end setZioTags

end ZioTag
