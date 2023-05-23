package org.bitlap.skywalking.apm.plugin.ziohttp;

import org.apache.skywalking.apm.agent.core.boot.PluginConfig;

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/23
 */
public class ZioHttpPluginConfig {
    public static class Plugin {
        @PluginConfig(root = ZioHttpPluginConfig.class)
        public static class ZioHttp {
            // split by ,
            public static String IGNORE_HTTP_URL_PREFIXES = "";
        }
    }
}
