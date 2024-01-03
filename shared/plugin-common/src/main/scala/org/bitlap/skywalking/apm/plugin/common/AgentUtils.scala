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
      if contextSnapshot != null then ContextManager.continued(contextSnapshot)
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
    }

  def continuedSnapshot(contextSnapshot: ContextSnapshot)(effect: => Unit): Unit =
    try
      if contextSnapshot != null then ContextManager.continued(contextSnapshot)
      effect
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
    } finally AgentUtils.stopIfActive()

  def continuedSnapshot(contextSnapshot: ContextSnapshot, asyncSpan: AbstractSpan)(effect: => Unit): Unit =
    try
      if contextSnapshot != null then ContextManager.continued(contextSnapshot)
      AgentUtils.stopAsync(asyncSpan)
      effect
    catch {
      case t: Throwable =>
        ContextManager.activeSpan.log(t)
    } finally AgentUtils.stopIfActive()

  def isValidCurrent(enhanced: EnhancedInstance): Boolean =
    val storedField = enhanced.getSkyWalkingDynamicField
    if storedField != null && storedField.isInstanceOf[ContextSnapshot] then {
      true
    } else {
      false
    }

  def matchPrefix(ignoreUrlPrefixes: => String, uri: => String): Boolean =
    val prefixes = ignoreUrlPrefixes.split(",").toList.filter(_.nonEmpty)
    prefixes.nonEmpty && prefixes.exists(p => uri.startsWith(p))
