package org.bitlap.skywalking.apm.plugin.common

import org.apache.skywalking.apm.agent.core.context.*
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance

object AgentUtils:

  def stopIfActive(): Unit =
    if ContextManager.isActive then ContextManager.stopSpan()

  def prepareAsync(span: AbstractSpan): Unit =
    try
      span.prepareForAsync()
    catch {
      case _: Throwable =>
    }

  def stopAsync(span: AbstractSpan): Unit =
    try
      span.asyncFinish()
    catch {
      case _: Throwable =>
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
    } finally AgentUtils.stopIfActive()

  def continuedSnapshot(contextSnapshot: ContextSnapshot, asyncSpan: AbstractSpan)(effect: => Unit): Unit =
    try
      ContextManager.continued(contextSnapshot)
      AgentUtils.stopAsync(asyncSpan)
      effect
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
    } finally AgentUtils.stopIfActive()

  def continuedSnapshot(enhanced: Object): Unit =
    enhanced match {
      case enhancedInstance: EnhancedInstance =>
        val storedField = enhancedInstance.getSkyWalkingDynamicField
        if storedField != null then {
          val contextSnapshot = storedField.asInstanceOf[ContextSnapshot]
          AgentUtils.continuedSnapshot_(contextSnapshot)
        }
      case _ =>
    }

  def matchPrefix(ignoreUrlPrefixes: => String, uri: => String): Boolean =
    val prefixes = ignoreUrlPrefixes.split(",").toList.filter(_.nonEmpty)
    prefixes.nonEmpty && prefixes.exists(p => uri.startsWith(p))
