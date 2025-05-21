/**
 * Copyright 2024-2025 Wingify Software Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vwo.utils;

import java.util.HashMap;
import java.util.Map;

import com.vwo.models.user.VWOInitOptions;
import com.vwo.packages.logger.enums.LogLevelNumberEnum;

/**
 * Manages usage statistics for the SDK.
 * Tracks various features and configurations being used by the client.
 * Implements Singleton pattern to ensure a single instance.
 */
public class UsageStatsUtil {
    /** Singleton instance */
    private static UsageStatsUtil instance;

    /** Internal storage for usage statistics data */
    private Map<String, Object> usageStatsData;

    /** Private constructor to prevent direct instantiation */
    private UsageStatsUtil() {
        this.usageStatsData = new HashMap<>();
    }

    /**
     * Provides access to the singleton instance of UsageUtil.
     *
     * @return The single instance of UsageUtil
     */
    public static UsageStatsUtil getInstance() {
        if (instance == null) {
            instance = new UsageStatsUtil();
        }
        return instance;
    }

    /**
     * Sets usage statistics based on provided options.
     * Maps various SDK features and configurations to boolean flags.
     *
     * @param options Configuration options for the SDK
     */
    public void setUsageStats(VWOInitOptions options) {
        Map<String, Object> data = new HashMap<>();

        // Map configuration options to usage stats flags
        if (options.getIntegrations() != null) data.put("ig", 1);

        // check if the logger has transports in it
        if (options.getLogger() != null && options.getLogger() instanceof Map) {
            Map<?, ?> loggerMap = (Map<?, ?>) options.getLogger();
            if (loggerMap.containsKey("transport") || loggerMap.containsKey("transports")) {
                data.put("cl", 1);
            }
        }

        // check the logger level
        // if the level is not valid, push -1
        // if the level is valid, push the enum value
        if (options.getLogger() != null && options.getLogger() instanceof Map) {
            Map<?, ?> loggerMap = (Map<?, ?>) options.getLogger();
            if (loggerMap.containsKey("level")) {
                String level = loggerMap.get("level").toString();
                try {
                    LogLevelNumberEnum logLevelEnum = LogLevelNumberEnum.valueOf(level.toUpperCase());
                    data.put("ll", logLevelEnum.getLevel());
                } catch (IllegalArgumentException e) {
                    data.put("ll", -1);
                }
            }
        }

        if (options.getStorage() != null) data.put("ss", 1);

        // check if the gatewayService is not null and is not an empty map
        if (options.getGatewayService() != null && !options.getGatewayService().isEmpty()) {
            data.put("gs", 1);     
        }

        if (options.getPollInterval() != null) data.put("pi", 1);

        // Handle _vwo_meta
        Object vwoMeta = options.getVwoMetaData();
        if (vwoMeta != null && vwoMeta instanceof Map) {
            Map<?, ?> vwoMetaMap = (Map<?, ?>) vwoMeta;
            if (vwoMetaMap.containsKey("ea")) {
                data.put("_ea", 1);
            }
        }

        // Get Java version
        data.put("lv", System.getProperty("java.version"));

        this.usageStatsData = data;
    }

    /**
     * Retrieves the current usage statistics.
     *
     * @return Map containing flags for various SDK features in use
     */
    public Map<String, Object> getUsageStats() {
        return new HashMap<>(usageStatsData);
    }

    /**
     * Clears the usage statistics data.
     */
    public void clearUsageStats() {
        usageStatsData.clear();
    }
}
