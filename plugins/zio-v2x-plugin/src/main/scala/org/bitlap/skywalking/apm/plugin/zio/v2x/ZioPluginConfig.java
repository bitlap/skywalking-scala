package org.bitlap.skywalking.apm.plugin.zio.v2x;

import org.apache.skywalking.apm.agent.core.boot.PluginConfig;

/**
 * @author 梦境迷离
 * @version 1.0, 2023/5/23
 */
public class ZioPluginConfig {
    public static class Plugin {
        @PluginConfig(root = ZioPluginConfig.class)
        public static class ZioV2 {
            public static String IGNORE_FIBER_REGEXES = ".*Application\\.run.*,.*ZHttpServer\\.start.*";

        }
    }
}
