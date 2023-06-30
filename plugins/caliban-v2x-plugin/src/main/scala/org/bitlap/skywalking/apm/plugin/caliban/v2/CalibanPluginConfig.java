package org.bitlap.skywalking.apm.plugin.caliban.v2;

import org.apache.skywalking.apm.agent.core.boot.PluginConfig;

/** @author
 *    梦境迷离
 *  @version 1.0,2023/5/23
 */
public class CalibanPluginConfig {
    public static class Plugin {
        @PluginConfig(root = CalibanPluginConfig.class)
        public static class CalibanV2 {
            // split by ,
            public static String IGNORE_URL_PREFIXES = "";
            public static String URL_PREFIX = "Caliban/GraphQL/";
            
            public static boolean COLLECT_VARIABLES = false;

            public static int VARIABLES_LENGTH_THRESHOLD = 1024;
        }
    }
}
