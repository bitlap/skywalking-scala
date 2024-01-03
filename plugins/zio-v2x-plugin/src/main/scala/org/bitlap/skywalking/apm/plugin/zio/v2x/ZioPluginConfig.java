package org.bitlap.skywalking.apm.plugin.zio.v2x;

import org.apache.skywalking.apm.agent.core.boot.PluginConfig;

public class ZioPluginConfig {
    public static class Plugin {
        @PluginConfig(root = ZioPluginConfig.class)
        public static class ZioV2 {
            public static String IGNORE_FIBER_REGEXES = "";

        }
    }
}
