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
import com.vwo.utils.LogMessageUtil;

import java.util.Objects;


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
                .initPolling();        // Initializes the polling mechanism for fetching settings.

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

        instance = VWO.setInstance(options);
        return instance;
    }
}

