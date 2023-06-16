package org.bitlap.skywalking.apm.plugin.common

import scala.util.Try

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.logging.api.LogManager
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/13
 */
object AgentUtils:

  private val LOGGER = LogManager.getLogger(classOf[AgentUtils.type])

  def prepareAsync(span: AbstractSpan) =
    try
      span.prepareForAsync()
    catch {
      case t: Throwable => logError(t)
    }

  def stopAsync(span: AbstractSpan) =
    try
      span.asyncFinish()
    catch {
      case t: Throwable => logError(t)
    }

  def continuedSnapshot_(contextSnapshot: ContextSnapshot): Unit =
    try
      ContextManager.continued(contextSnapshot)
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
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
      AgentUtils.stopAsync(asyncSpan)
      effect
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
    } finally ContextManager.stopSpan()

  def continuedSnapshot(enhanced: Object): Unit =
    enhanced match {
      case enhancedInstance: EnhancedInstance =>
        val storedField = enhancedInstance.getSkyWalkingDynamicField
        if storedField != null then {
          val contextSnapshot = storedField.asInstanceOf[ContextSnapshot]
          AgentUtils.continuedSnapshot_(contextSnapshot)
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

  def ignorePrefix(ignoreUrlPrefixes: => String, uri: => String): Boolean =
    val prefixes = ignoreUrlPrefixes.split(",").toList.filter(_.nonEmpty)
    prefixes.nonEmpty && prefixes.exists(p => uri.startsWith(p))

  def logError[E <: Throwable](e: E): Unit =
    LOGGER.error(s"Span Operation Error!", e)
    if ContextManager.isActive then ContextManager.activeSpan.log(e)
