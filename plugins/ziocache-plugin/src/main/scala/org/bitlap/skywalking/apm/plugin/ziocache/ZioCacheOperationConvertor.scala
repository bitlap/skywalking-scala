package org.bitlap.skywalking.apm.plugin.ziocache

object ZioCacheOperationConvertor {

  def parseOperation(cmd: String): Option[String] =
    if ZioCachePluginConfig.Plugin.ZioCache.OPERATION_MAPPING_READ.contains(cmd) then Some("read")
    else if ZioCachePluginConfig.Plugin.ZioCache.OPERATION_MAPPING_WRITE.contains(cmd) then Some("write")
    else None

}
