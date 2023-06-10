package org.bitlap.skywalking.apm.plugin.zio

import java.text.SimpleDateFormat

import scala.util.*

import zio.*
import zio.internal.FiberRuntime

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.tag.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.logging.api.{ ILog, LogManager }
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance
import org.bitlap.skywalking.apm.plugin.common.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/16
 */
object Utils:

  private val LOGGER = LogManager.getLogger(classOf[Utils.type])

  final lazy val fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  private def formatDate(long: Long) = fmt.format(long)

  def setZioTags(span: AbstractSpan, fiberId: FiberId.Runtime, objInst: EnhancedInstance): Unit =
    Try {
      ScalaTags.FiberId.tag.set(span, fiberId.id.toString)
      ScalaTags.FiberStartTime.tag.set(span, formatDate(fiberId.startTimeMillis))
      ScalaTags.FiberLocation.tag.set(span, fiberId.location.toString)
      ScalaTags.ClassName.tag.set(span, objInst.getClass.getName)
    } match
      case Failure(ex) => span.errorOccurred.log(ex)
      case Success(_)  =>
  end setZioTags

  def setSpanZioTag(arg: Object, objInst: EnhancedInstance)(implicit span: AbstractSpan) =
    arg match {
      case fiber: FiberRuntime[?, ?] =>
        Utils.setZioTags(span, fiber.id, objInst)
      case _ =>
        LOGGER.debug(s"Invalid FiberRuntime: ${objInst.getClass.getName} ${objInst.toString}")
    }

  def continuedSnapshot(enhanced: Object): Unit =
    enhanced match {
      case enhancedInstance: EnhancedInstance =>
        val storedField = enhancedInstance.getSkyWalkingDynamicField
        if storedField != null then {
          val contextSnapshot = storedField.asInstanceOf[ContextSnapshot]
          InterceptorDSL.continuedSnapshot_(contextSnapshot)
        } else {
          LOGGER.debug(
            s"EnhancedInstance/getSkyWalkingDynamicField is null: ${enhanced.toString}"
          )
        }
      case _ =>
        LOGGER.debug(
          s"Invalid EnhancedInstance: ${enhanced.getClass.getName}"
        )
    }

  def getExecutorType(className: String): ExecutorType =
    ExecutorType.values.find(c => className.startsWith(c.classNamePrefix)).getOrElse(ExecutorType.Executor)
end Utils
