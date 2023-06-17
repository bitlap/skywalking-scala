package org.bitlap.skywalking.apm.plugin.zcommon

import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.matcher.ElementMatchers.takesArguments

import org.apache.skywalking.apm.agent.core.plugin.WitnessMethod

/** @author
 *    梦境迷离
 *  @version 1.0,2023/6/13
 */
object ZioWitnessConstant:

  final val WITNESS_203X_METHOD =
    new WitnessMethod("zio.internal.FiberRunnable", ElementMatchers.named("run").and(takesArguments(1)))
