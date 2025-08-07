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
package com.vwo;

import com.vwo.models.user.VWOInitOptions;
import com.vwo.enums.EventEnum;
import com.vwo.utils.LogMessageUtil;
import com.vwo.utils.EventUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;


public class VWO extends VWOClient {
    private static VWOBuilder vwoBuilder;
    private static VWO instance;

    /**
     * Constructor for the VWO class.
     * Initializes a new instance of VWO with the provided options.
     * @param options - Configuration options for the VWO instance.
     */
    public VWO(String settings, VWOInitOptions options) {
        super(settings, options);
    }

    /**
     * Sets the singleton instance of VWO.
     * Configures and builds the VWO instance using the provided options.
     * @param options - Configuration options for setting up VWO.
     * @return A CompletableFuture resolving to the configured VWO instance.
     */
    private static VWO setInstance(VWOInitOptions options) {
        if (options.getVwoBuilder() != null) {
            vwoBuilder = options.getVwoBuilder();
        } else {
            vwoBuilder = new VWOBuilder(options);
        }
        vwoBuilder
                .setLogger()           // Sets up logging for debugging and monitoring.
                .setSettingsManager()  // Sets the settings manager for configuration management.
                .setStorage()          // Configures storage for data persistence.
                .setNetworkManager()   // Configures network management for API communication.
                .setSegmentation()     // Sets up segmentation for targeted functionality.
                .initPolling()        // Initializes the polling mechanism for fetching settings.
                .initUsageStats();

        String settings =  vwoBuilder.getSettings(false);
        VWO vwoInstance = new VWO(settings, options);

        // Set VWOClient instance in VWOBuilder
        vwoBuilder.setVWOClient(vwoInstance);
        vwoBuilder.initBatching();
        return vwoInstance;
    }

    /**
     * Gets the singleton instance of VWO.
     * @return The singleton instance of VWO.
     */
    public static VWO getInstance() {
        return instance;
    }

    public static VWO init(VWOInitOptions options) {
        if (options == null || options.getSdkKey() == null || options.getSdkKey().isEmpty()) {
            String message = LogMessageUtil.buildMessage("SDK key is required to initialize VWO. Please provide the sdkKey in the options.", null);
            System.err.println(message);
        }

        if (options == null || options.getAccountId() == null || options.getAccountId().toString().isEmpty()) {
            String message = LogMessageUtil.buildMessage("Account ID is required to initialize VWO. Please provide the accountId in the options.", null);
            System.err.println(message);
        }
        //start timer
        long initStartTime = System.currentTimeMillis();
        instance = VWO.setInstance(options);
        long initTime = System.currentTimeMillis() - initStartTime;

        // if wasInitializedEarlier in sdkMetaInfo in settings is false or is absent and settings is valid on init, then send sdk init event
        String settings = vwoBuilder.getOriginalSettings();
        if (settings != null && !settings.isEmpty()) {
            try {
                JsonNode settingsJsonNode = VWOClient.objectMapper.readTree(settings);

                boolean wasInitializedEarlier = false; // default value
                JsonNode sdkMetaInfoNode = settingsJsonNode.get("sdkMetaInfo");
                if (sdkMetaInfoNode != null) {
                    JsonNode wasInitializedEarlierNode = sdkMetaInfoNode.get("wasInitializedEarlier");
                    if (wasInitializedEarlierNode != null) {
                        wasInitializedEarlier = wasInitializedEarlierNode.asBoolean();
                    }
                }
                
                boolean isSettingsValidOnInit = vwoBuilder.getSettingsService().isSettingsValidOnInit;
                
                if (!wasInitializedEarlier && isSettingsValidOnInit) {
                    EventUtil.sendSdkInitEvent(vwoBuilder.getSettingsService().getSettingsFetchTime(), initTime, EventEnum.VWO_SDK_INIT_EVENT.getValue());
                }
            } catch (JsonProcessingException e) {
                String message = LogMessageUtil.buildMessage("Error parsing settings JSON: " + e.getMessage(), null);
                System.err.println(message);
            }
        }
        return instance;
    }
}

