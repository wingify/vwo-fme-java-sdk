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

import com.fasterxml.jackson.databind.JsonNode;
import com.vwo.constants.Constants;
import com.vwo.models.Settings;
import com.vwo.models.user.VWOInitOptions;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.network_layer.manager.NetworkManager;

import com.vwo.packages.segmentation_evaluator.core.SegmentationManager;
import com.vwo.packages.storage.Storage;
import com.vwo.services.BatchEventQueue;
import com.vwo.services.LoggerService;
import com.vwo.services.SettingsManager;
import com.vwo.utils.DataTypeUtil;
import com.vwo.utils.UsageStatsUtil;

import java.util.HashMap;

import static com.vwo.utils.LogMessageUtil.*;


public class VWOBuilder {
    private VWOClient vwoClient;
    private final VWOInitOptions options;
    private SettingsManager settingFileManager;
    private String settings;
    private String originalSettings;
    private boolean isSettingsFetchInProgress;
    private boolean isValidPollIntervalPassedFromInit = false;

    public VWOBuilder(VWOInitOptions options) {
        this.options = options;
    }

    // Set VWOClient instance
    public void setVWOClient(VWOClient vwoClient) {
        this.vwoClient = vwoClient;

        // if poll_interval is not present in options, set it to the pollInterval from settings
        updatePollIntervalAndCheckAndPoll(settings, true);
    }

    /**
     * Sets the network manager with the provided client and development mode options.
     * @return The VWOBuilder instance.
     */
    public VWOBuilder setNetworkManager() {
        NetworkManager networkInstance = NetworkManager.getInstance();
        if (this.options != null && this.options.getNetworkClientInterface() != null) {
            networkInstance.attachClient(this.options.getNetworkClientInterface());
        } else {
            networkInstance.attachClient();
        }
        networkInstance.getConfig().setDevelopmentMode(false);
        LoggerService.log(LogLevelEnum.DEBUG, "SERVICE_INITIALIZED", new HashMap<String, String>() {
            {
                put("service", "Network Layer");
            }
        });
        return this;
    }

    /**
     * Sets the segmentation evaluator with the provided segmentation options.
     * @return The instance of this builder.
     */
    public VWOBuilder setSegmentation() {
        if (options!= null && options.getSegmentEvaluator() != null) {
            SegmentationManager.getInstance().attachEvaluator(options.getSegmentEvaluator());
        }
        LoggerService.log(LogLevelEnum.DEBUG, "SERVICE_INITIALIZED", new HashMap<String, String>() {
            {
                put("service", "Segmentation Evaluator");
            }
        });
        return this;
    }

    /**
     * Fetches settings asynchronously, ensuring no parallel fetches.
     * @param forceFetch - Force fetch ignoring cache.
     * @return The fetched settings.
     */
    public String fetchSettings(Boolean forceFetch) {
        // Check if a fetch operation is already in progress
        if (isSettingsFetchInProgress || settingFileManager == null) {
            // Avoid parallel fetches
            return null; // Or throw an exception, or handle as needed
        }

        // Set the flag to indicate that a fetch operation is in progress
        isSettingsFetchInProgress = true;

        try {
            // Retrieve the settings synchronously
            String settings = settingFileManager.getSettings(forceFetch);

            if (!forceFetch) {
                // Store the original settings
                originalSettings = settings;
            }

            // Clear the flag to indicate that the fetch operation is complete
            isSettingsFetchInProgress = false;

            // Return the fetched settings
            return settings;
        } catch (Exception e) {
            LoggerService.log(LogLevelEnum.ERROR, "SETTINGS_FETCH_ERROR", new HashMap<String, String>() {
                {
                    put("err", e.toString());
                    put("accountId", options.getAccountId().toString());
                    put("sdkKey", options.getSdkKey());
                }
            });
            // Clear the flag to indicate that the fetch operation is complete
            isSettingsFetchInProgress = false;

            // Return null or handle the error as needed
            return null;
        }
    }

    /**
     * Gets the settings, fetching them if not cached or if forced.
     * @param forceFetch - Force fetch ignoring cache.
     * @return The fetched settings.
     */
    public String getSettings(Boolean forceFetch) {
        this.settings = fetchSettings(forceFetch);
        return this.settings;
    }

    /**
     * Sets the storage connector for the VWO instance.
     * @return  The instance of this builder.
     */
    public VWOBuilder setStorage(){
        if (options != null && options.getStorage() != null) {
            Storage.getInstance().attachConnector(options.getStorage());
        }
        return this;
    }

    /**
     * Sets the settings manager for the VWO instance.
     * @return The instance of this builder.
     */
    public VWOBuilder setSettingsManager() {
        if (options == null){
            return this;
        }
        settingFileManager = new SettingsManager(options);
        return this;
    }

    /**
     * Sets the logger for the VWO instance.
     * @return The instance of this builder.
     */
    
    public VWOBuilder setLogger() {
        try {
            if (this.options == null || this.options.getLogger() == null || this.options.getLogger().isEmpty()) {
                new LoggerService(new HashMap<>());
            } else {
                new LoggerService(this.options.getLogger());
            }
            LoggerService.log(LogLevelEnum.DEBUG, "SERVICE_INITIALIZED", new HashMap<String, String>() {
                {
                    put("service", "Logger");
                }
            });
        } catch (Exception e) {
            String message = buildMessage("Error occurred while initializing Logger : " + e.getMessage(), null);
            System.err.println(message);
        }
        return this;
    }

    /**
     * Initializes the polling with the provided poll interval.
     * @return The instance of this builder.
     */
    public VWOBuilder initPolling() {
        if (this.options.getPollInterval() != null && DataTypeUtil.isInteger(this.options.getPollInterval()) && this.options.getPollInterval() >= 1000) {
            // this is to check if the poll_interval passed in options is valid
            isValidPollIntervalPassedFromInit = true;
            new Thread(this::checkAndPoll).start();
            return this;
        } else if (this.options.getPollInterval() != null) {
            // only log error if poll_interval is present in options
            LoggerService.log(LogLevelEnum.ERROR, "INIT_OPTIONS_INVALID", new HashMap<String, String>(){
                {
                    put("key", "pollInterval");
                    put("correctType", "number");
                }
            });
            return this;
        }
        return this;
    }

    public void updatePollIntervalAndCheckAndPoll(String settings, boolean shouldCheckAndPoll) {
        // only update the poll_interval if poll_interval is not valid or not present in options
        Settings processedSettings = null;
        if (settings != null && !settings.isEmpty()) {
            try {
                processedSettings = VWOClient.objectMapper.readValue(settings, Settings.class);
            } catch (Exception ignored) {}
        }
        if (!isValidPollIntervalPassedFromInit && processedSettings != null) {
            this.options.setPollInterval(processedSettings.getPollInterval());
            if (processedSettings.getPollInterval() == Constants.DEFAULT_POLL_INTERVAL) {
                LoggerService.log(LogLevelEnum.DEBUG, "USING_POLL_INTERVAL_FROM_SETTINGS", new HashMap<String, String>(){
                    {
                        put("source", "default");
                        put("pollInterval", String.valueOf(Constants.DEFAULT_POLL_INTERVAL));
                    }
                });
            } else {
                LoggerService.log(LogLevelEnum.DEBUG, "USING_POLL_INTERVAL_FROM_SETTINGS", new HashMap<String, String>(){
                    {
                        put("source", "settings");
                        put("pollInterval", options.getPollInterval().toString());
                    }
                });
            }
        }

        if (shouldCheckAndPoll && !isValidPollIntervalPassedFromInit && processedSettings != null) {
            new Thread(this::checkAndPoll).start();
        }
    }

    /**
     * Initializes the usage stats for the VWO instance.
     * @return The instance of this builder.
     */
    public VWOBuilder initUsageStats() {

        // if usageStatsDisabled is not null and is true, then return
        if (this.options.getIsUsageStatsDisabled() != null && this.options.getIsUsageStatsDisabled()) {
            return this;
        }
       
        UsageStatsUtil.getInstance().setUsageStats(this.options);
        

        return this;
    }

    /**
     * Checks and polls for settings updates at the provided interval.
     */
    private void checkAndPoll() {
        String latestSettings = null;
        while (true) {
            try {
                // Sleep for the polling interval
                Thread.sleep(this.options.getPollInterval());
                latestSettings = getSettings(true);
                if (originalSettings != null && latestSettings != null) {
                    JsonNode latestSettingJsonNode = VWOClient.objectMapper.readTree(latestSettings);
                    JsonNode originalSettingsJsonNode = VWOClient.objectMapper.readTree(originalSettings);
                    if (!latestSettingJsonNode.equals(originalSettingsJsonNode)) {
                        LoggerService.log(LogLevelEnum.INFO, "POLLING_SET_SETTINGS", null);
                        // Update VWOClient settings
                        if (vwoClient != null) {
                            String updatedSettings = vwoClient.updateSettings(latestSettings);
                            if (updatedSettings != null) {
                                originalSettings = latestSettings;
                                updatePollIntervalAndCheckAndPoll(originalSettings, false);
                            }
                        }
                    } else {
                        LoggerService.log(LogLevelEnum.INFO, "POLLING_NO_CHANGE_IN_SETTINGS", null);
                    }
                }
            } catch (InterruptedException e) {
                LoggerService.log(LogLevelEnum.ERROR, "POLLING_FETCH_SETTINGS_FAILED", null);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LoggerService.log(LogLevelEnum.ERROR, "Error occurred while polling for settings, Error: " + e.getMessage() + " originalSettings: " + originalSettings + " latestSettings: " + latestSettings);
            }
        }
    }

    /**
     * Gets the original settings string
     * @return The original settings string
     */
    public String getOriginalSettings() {
        return this.originalSettings;
    }

    /**
     * Gets the settings service instance
     * @return The settings service instance
     */
    public SettingsManager getSettingsService() {
        return this.settingFileManager;
    }

    public VWOBuilder initBatching() {
        // Check if gatewayService is provided and skip SDK batching if so
        if (SettingsManager.getInstance().isGatewayServiceProvided) {
            LoggerService.log(LogLevelEnum.WARN, "Gateway service is configured. Event batching will be handled by the gateway. SDK batching is disabled.");
            return this;
        }

        // Check if batch event data is provided in options
        if (this.options.getBatchEventData() != null) {
            int eventsPerRequest = this.options.getBatchEventData().getEventsPerRequest();
            int requestTimeInterval = this.options.getBatchEventData().getRequestTimeInterval();

            boolean isEventsPerRequestValid = DataTypeUtil.isInteger(eventsPerRequest) && eventsPerRequest > 0 && eventsPerRequest <= Constants.MAX_EVENTS_PER_REQUEST;
            boolean isRequestTimeIntervalValid = DataTypeUtil.isInteger(requestTimeInterval) && requestTimeInterval >0;

            // Check data type and values for eventsPerRequest and requestTimeInterval
            if (!isEventsPerRequestValid && !isRequestTimeIntervalValid) {
                LoggerService.log(LogLevelEnum.ERROR, "Values mismatch from the expectation of both parameters. Batching not initialized.");
                return this;
            }

            // Handle invalid data types for individual parameters
            if (!isEventsPerRequestValid) {
                LoggerService.log(LogLevelEnum.ERROR, "Events_per_request values is invalid (should be greater than 0 and less than 5000). Using default value of events_per_request parameter : 100");
                eventsPerRequest = Constants.DEFAULT_EVENTS_PER_REQUEST; // Use default if invalid
            }

            if (!isRequestTimeIntervalValid) {
                LoggerService.log(LogLevelEnum.ERROR, "Request_time_interval values is invalid (should be greater than 0). Using default value of request_time_interval parameter : 600");
                requestTimeInterval = Constants.DEFAULT_REQUEST_TIME_INTERVAL; // Use default if invalid
            }

            // Initialize BatchEventQueue for batching
            BatchEventQueue batchEventQueue = new BatchEventQueue(
                    eventsPerRequest,
                    requestTimeInterval,  // Cast to int since the expected type in BatchEventQueue is int
                    this.options.getBatchEventData().getFlushCallback(),
                    this.options.getAccountId(),
                    this.options.getSdkKey()
            );

            vwoClient.setBatchEventQueue(batchEventQueue); // Link to the vwoClient
            LoggerService.log(LogLevelEnum.DEBUG, "Event Batching initialized successfully in SDK.");
        } else {
            LoggerService.log(LogLevelEnum.DEBUG, "Event Batching functionality not initialized. SDK batching is disabled.");
        }
        return this;
    }

}
