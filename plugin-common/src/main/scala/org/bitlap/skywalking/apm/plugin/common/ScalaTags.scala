package org.bitlap.skywalking.apm.plugin.common

import org.apache.skywalking.apm.agent.core.context.tag.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/6/2
 */
enum ScalaTags[T](val tag: StringTag):
  case FiberId          extends ScalaTags(new StringTag(100, "zio.fiber.id"))
  case FiberStartTime   extends ScalaTags(new StringTag(101, "zio.fiber.startTime"))
  case FiberLocation    extends ScalaTags(new StringTag(102, "zio.fiber.location"))
  case CalibanVariables extends ScalaTags(new StringTag(103, "caliban.variables"))
  case ClassName        extends ScalaTags(new StringTag(104, "zio.fiber.className"))
