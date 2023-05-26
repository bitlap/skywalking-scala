package org.bitlap.skywalking.apm.plugin.caliban;

import org.apache.skywalking.apm.agent.core.boot.PluginConfig;

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/23
 */
public class CalibanPluginConfig {
    public static class Plugin {
        @PluginConfig(root = CalibanPluginConfig.class)
        public static class Caliban {
            // split by ,
            public static String IGNORE_URL_PREFIXES = "";
            public static String URL_PREFIX = "GraphQL/";
        }
    }
}