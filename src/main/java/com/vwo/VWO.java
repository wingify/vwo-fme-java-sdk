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

import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.models.user.VWOInitOptions;
import com.vwo.utils.LogMessageUtil;


public class VWO extends VWOClient {

    /**
     * Constructor for the VWO class.
     * Initializes a new instance of VWO with the provided options.
     * @param settings - Settings string for the VWO instance.
     * @param vwoBuilder - VWO builder instance containing configuration options.
     */
    public VWO(String settings, VWOBuilder vwoBuilder) {
        super(settings, vwoBuilder);
    }

    /**
     * Sets the singleton instance of VWO.
     * Configures and builds the VWO instance using the provided options.
     * @param options - Configuration options for setting up VWO.
     * @return A CompletableFuture resolving to the configured VWO instance.
     */
    private static VWO setInstance(VWOInitOptions options) {
        VWOBuilder vwoBuilder;
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
                .initPolling()        // Initializes the polling mechanism for fetching settings.
                .initUsageStats();

        String settings =  vwoBuilder.getSettings(false);
        vwoBuilder.initBatching();
        VWO vwoInstance = new VWO(settings, vwoBuilder);
        // Set VWOClient instance in VWOBuilder
        vwoBuilder.setVWOClient(vwoInstance);

        vwoBuilder.getLoggerService().log(LogLevelEnum.INFO, "CLIENT_INITIALIZED", null);
        return vwoInstance;
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
        VWO instance = VWO.setInstance(options);
        long initTime = System.currentTimeMillis() - initStartTime;
        // send sdk init event
        instance.sendSdkInitEvent(initTime);
        return instance;
    }
}

