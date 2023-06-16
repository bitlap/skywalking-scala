package org.bitlap.skywalking.apm.plugin.common

import org.apache.skywalking.apm.agent.core.context.tag.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/6/2
 */
enum CustomTag[T](val tag: StringTag):
  case FiberId          extends CustomTag(new StringTag(100, "zio.fiber.id"))
  case FiberStartTime   extends CustomTag(new StringTag(101, "zio.fiber.startTime"))
  case FiberLocation    extends CustomTag(new StringTag(102, "zio.fiber.location"))
  case CalibanVariables extends CustomTag(new StringTag(103, "caliban.variables"))
  case FiberClassName   extends CustomTag(new StringTag(104, "zio.fiber.className"))
  case FiberIOType   extends CustomTag(new StringTag(105, "io.fiber.type"))
