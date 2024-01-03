package org.bitlap.skywalking.apm.plugin.ziocache;

import org.apache.skywalking.apm.agent.core.boot.PluginConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ZioCachePluginConfig {
    public static class Plugin {
        @PluginConfig(root = ZioCachePluginConfig.class)
        public static class ZioCache {
            public static Set<String> OPERATION_MAPPING_WRITE = new HashSet<>(Arrays.asList("refresh", "invalidate", "invalidateAll"));
            public static Set<String> OPERATION_MAPPING_READ = new HashSet<>(Arrays.asList("get", "contains", "size"));
        }
    }
}
