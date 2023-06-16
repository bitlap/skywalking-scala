package org.bitlap.skywalking.apm.plugin.zcommon

/** @author
 *    梦境迷离
 *  @version 1.0,2023/6/9
 */
enum ExecutorType(val classNamePrefix: String):
  case Executor                extends ExecutorType("zio.Executor$$anon$")
  case ZScheduler              extends ExecutorType("zio.internal.ZScheduler")
  case DefaultBlockingExecutor extends ExecutorType("zio.internal.DefaultExecutors$")

end ExecutorType
