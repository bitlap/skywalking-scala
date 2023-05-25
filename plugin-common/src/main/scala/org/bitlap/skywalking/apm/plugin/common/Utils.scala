package org.bitlap.skywalking.apm.plugin.common

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/25
 */
object Utils:

  def ignorePrefix(ignoreUrlPrefixes: => String, uri: => String): Boolean =
    val prefixes = ignoreUrlPrefixes.split(",").toList.filter(_.nonEmpty)
    prefixes.nonEmpty && prefixes.exists(p => uri.startsWith(p))
