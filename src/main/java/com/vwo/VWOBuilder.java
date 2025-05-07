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
import com.vwo.models.user.VWOInitOptions;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.network_layer.manager.NetworkManager;

import com.vwo.packages.segmentation_evaluator.core.SegmentationManager;
import com.vwo.packages.storage.Storage;
import com.vwo.services.LoggerService;
import com.vwo.services.SettingsManager;
import com.vwo.utils.DataTypeUtil;

import java.util.HashMap;

import static com.vwo.utils.LogMessageUtil.*;


public class VWOBuilder {
    private VWOClient vwoClient;
    private final VWOInitOptions options;
    private SettingsManager settingFileManager;
    private String settings;
    private String originalSettings;
    private boolean isSettingsFetchInProgress;

    public VWOBuilder(VWOInitOptions options) {
        this.options = options;
    }

    // Set VWOClient instance
    public void setVWOClient(VWOClient vwoClient) {
        this.vwoClient = vwoClient;
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
        return fetchSettings(forceFetch);
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
        if (this.options.getPollInterval() == null) {
            return this;
        }

        if (this.options.getPollInterval() != null && !DataTypeUtil.isInteger(this.options.getPollInterval())) {
            LoggerService.log(LogLevelEnum.ERROR, "INIT_OPTIONS_INVALID", new HashMap<String, String>(){
                {
                    put("key", "pollInterval");
                    put("correctType", "number");
                }
            });
            return this;
        }

        if (this.options.getPollInterval() != null && this.options.getPollInterval() < 1000) {
            LoggerService.log(LogLevelEnum.ERROR, "INIT_OPTIONS_INVALID", new HashMap<String, String>(){
                {
                    put("key", "pollInterval");
                    put("correctType", "number");
                }
            });
            return this;
        }

        new Thread(this::checkAndPoll).start();

        return this;
    }

    /**
     * Checks and polls for settings updates at the provided interval.
     */
    private void checkAndPoll() {
        int pollingInterval = this.options.getPollInterval();

        while (true) {
            try {
                String latestSettings = getSettings(true);
                if (originalSettings != null && latestSettings != null) {
                    JsonNode latestSettingJsonNode = VWOClient.objectMapper.readTree(latestSettings);
                    JsonNode originalSettingsJsonNode = VWOClient.objectMapper.readTree(originalSettings);
                    if (!latestSettingJsonNode.equals(originalSettingsJsonNode)) {
                        originalSettings = latestSettings;
                        LoggerService.log(LogLevelEnum.INFO, "POLLING_SET_SETTINGS", null);
                        // Update VWOClient settings
                        if (vwoClient != null) {
                            vwoClient.updateSettings(originalSettings);
                        }
                    } else {
                        LoggerService.log(LogLevelEnum.INFO, "POLLING_NO_CHANGE_IN_SETTINGS", null);
                    }
                }
                // Sleep for the polling interval
                Thread.sleep(pollingInterval);
            } catch (InterruptedException e) {
                LoggerService.log(LogLevelEnum.ERROR, "POLLING_FETCH_SETTINGS_FAILED", null);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LoggerService.log(LogLevelEnum.ERROR, "Error is " + e);
            }
        }
    }
}
