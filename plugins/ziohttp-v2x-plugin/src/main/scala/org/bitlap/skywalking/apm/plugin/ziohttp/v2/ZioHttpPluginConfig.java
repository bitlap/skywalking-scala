package org.bitlap.skywalking.apm.plugin.ziohttp.v2;

import org.apache.skywalking.apm.agent.core.boot.PluginConfig;

public class ZioHttpPluginConfig {
    public static class Plugin {
        @PluginConfig(root = ZioHttpPluginConfig.class)
        public static class ZioHttpV2 {
            // split by ,
            public static String IGNORE_URL_PREFIXES = "";

            public static boolean COLLECT_HTTP_PARAMS = false;

            public static int HTTP_PARAMS_LENGTH_THRESHOLD = 1024;
        }
    }
}
