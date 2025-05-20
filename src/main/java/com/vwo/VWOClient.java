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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vwo.api.GetFlagAPI;
import com.vwo.api.SetAttributeAPI;
import com.vwo.api.TrackEventAPI;
import com.vwo.models.schemas.SettingsSchema;
import com.vwo.models.user.VWOContext;
import com.vwo.models.user.GetFlag;
import com.vwo.models.Settings;
import com.vwo.models.user.VWOInitOptions;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.services.HooksManager;
import com.vwo.services.LoggerService;
import com.vwo.services.UrlService;
import com.vwo.utils.DataTypeUtil;
import com.vwo.utils.SDKMetaUtil;
import com.vwo.utils.SettingsUtil;
import com.vwo.services.BatchEventQueue;
import com.vwo.services.SettingsManager;

import java.util.HashMap;
import java.util.Map;

public class VWOClient {
    private Settings processedSettings;
    public String settings;
    private VWOInitOptions options;
    public static ObjectMapper objectMapper = new ObjectMapper(){
        {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
    };
    private BatchEventQueue batchEventQueue;

    public VWOClient(String settings, VWOInitOptions options) {
        try {
            this.options = options;
            if (settings == null) {
                return;
            }
            this.settings = settings;
            this.processedSettings = objectMapper.readValue(settings, Settings.class);
            SettingsUtil.processSettings(this.processedSettings);
            // init url version with collection prefix
            UrlService.init(this.processedSettings.getCollectionPrefix());
            // init SDKMetaUtil and set sdkVersion
            SDKMetaUtil.init();
            LoggerService.log(LogLevelEnum.INFO, "CLIENT_INITIALIZED", null);
        } catch (Exception exception) {
           LoggerService.log(LogLevelEnum.ERROR, "exception occurred while parsing settings " + exception.getMessage());
        }
    }

    // Getter and Setter for batchEventQueue
    public BatchEventQueue getBatchEventQueue() {
        return batchEventQueue;
    }

    public void setBatchEventQueue(BatchEventQueue batchEventQueue) {
        this.batchEventQueue = batchEventQueue;
    }

    /**
     * This method is used to get the flag value for the given feature key
     * @param featureKey Feature key for which the flag value is to be fetched
     * @param context User context
     * @return GetFlag object containing the flag values
     */
    public GetFlag getFlag(String featureKey, VWOContext context) {
        String apiName = "getFlag";
        GetFlag getFlag = new GetFlag();
        try {
            LoggerService.log(LogLevelEnum.DEBUG, "API_CALLED", new HashMap<String, String>() {{
                put("apiName", apiName);
            }});
            HooksManager hooksManager = new HooksManager(this.options.getIntegrations());
            if (context == null || context.getId() == null || context.getId().isEmpty()) {
                getFlag.setIsEnabled(false);
                throw new IllegalArgumentException("User ID is required");
            }

            if (featureKey == null || featureKey.isEmpty()) {
                getFlag.setIsEnabled(false);
                throw new IllegalArgumentException("Feature Key is required");
            }

            if (this.processedSettings == null || !new SettingsSchema().isSettingsValid(this.processedSettings)) {
                LoggerService.log(LogLevelEnum.ERROR, "SETTINGS_SCHEMA_INVALID", null);
                getFlag.setIsEnabled(false);
                return getFlag;
            }

            return GetFlagAPI.getFlag(featureKey, this.processedSettings, context, hooksManager);
        } catch (Exception exception) {
            LoggerService.log(LogLevelEnum.ERROR, "API_THROW_ERROR", new HashMap<String, String>() {{
                put("apiName", "getFlag");
                put("err", exception.toString());
            }});
            getFlag.setIsEnabled(false);
            return getFlag;
        }
    }

    /**
     * This method is used to track the event
     * @param eventName Event name to be tracked
     * @param context User context
     * @param eventProperties event properties to be sent for the event
     * @return Map containing the event name and its status
     */
    private Map<String, Boolean> track(String eventName, VWOContext context, Map<String, ?> eventProperties) {
        String apiName = "trackEvent";
        Map<String, Boolean> resultMap = new HashMap<>();
        try {
            LoggerService.log(LogLevelEnum.DEBUG, "API_CALLED", new HashMap<String, String>() {{
                put("apiName", apiName);
            }});
            HooksManager hooksManager = new HooksManager(this.options.getIntegrations());
            if (!DataTypeUtil.isString(eventName)) {
                LoggerService.log(LogLevelEnum.ERROR, "API_INVALID_PARAM", new HashMap<String, String>() {{
                    put("apiName", apiName);
                    put("key", "eventName");
                    put("type", DataTypeUtil.getType(eventName));
                    put("correctType", "String");
                }});
                throw new IllegalArgumentException("TypeError: Event-name should be a string");
            }

            if (context == null || context.getId() == null || context.getId().isEmpty()) {
                throw new IllegalArgumentException("User ID is required");
            }

            if (this.processedSettings == null || !new SettingsSchema().isSettingsValid(this.processedSettings)) {
                LoggerService.log(LogLevelEnum.ERROR, "SETTINGS_SCHEMA_INVALID", null);
                resultMap.put(eventName, false);
                return resultMap;
            }

            Boolean result = TrackEventAPI.track(this.processedSettings, eventName, context, eventProperties, hooksManager);
            if (result) {
                resultMap.put(eventName, true);
            } else {
                resultMap.put(eventName, false);
            }
            return resultMap;
        } catch (Exception exception) {
            LoggerService.log(LogLevelEnum.ERROR, "API_THROW_ERROR", new HashMap<String, String>() {{
                put("apiName", apiName);
                put("err", exception.toString());
            }});
            resultMap.put(eventName, false);
            return resultMap;
        }
    }

    /**
     * Overloaded function if event properties need to be passed
     * calls track method to track the event
     * @param eventName Event name to be tracked
     * @param context User context
     * @param eventProperties event properties to be sent for the event
     * @return Map containing the event name and its status
     */
    public Map<String, Boolean> trackEvent(String eventName, VWOContext context, Map<String, ?> eventProperties) {
        return track(eventName, context, eventProperties);
    }

    /**
     * Overloaded function for no event properties
     * calls track method to track the event
     * @param eventName Event name to be tracked
     * @param context User context
     * @return Map containing the event name and its status
     */
    public Map<String, Boolean> trackEvent(String eventName, VWOContext context) {
        return track(eventName, context, new HashMap<>());
    }


    /**
     * Sets an attribute for a user in the context provided.
     * This method validates the types of the inputs before proceeding with the API call.
     * @param attributeMap - Map of attribute key and value to be set
     * @param context User context
     */
    public void setAttribute(Map<String, Object> attributeMap, VWOContext context) {
        String apiName = "setAttribute";
        try {
            LoggerService.log(LogLevelEnum.DEBUG, "API_CALLED", new HashMap<String, String>() {{
                put("apiName", apiName);
            }});
            if (attributeMap == null || attributeMap.isEmpty()) {
                throw new IllegalArgumentException("TypeError: attributeMap should be a non-empty map of type Map<String, Object>");
            }

            // Validate attribute values
            for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
                Object value = entry.getValue();
                if (!(value instanceof String || value instanceof Number || value instanceof Boolean)) {
                    LoggerService.log(LogLevelEnum.ERROR, "API_INVALID_PARAM", new HashMap<String, String>() {{
                        put("apiName", apiName);
                        put("key", "attributeValue");
                        put("type", value != null ? value.getClass().getSimpleName() : "null");
                        put("correctType", "String, Number, or Boolean");
                    }});
                    throw new IllegalArgumentException("TypeError: Attribute value must be a String, Number, or Boolean");
                }
            }

            if (context == null || context.getId() == null || context.getId().isEmpty()) {
                throw new IllegalArgumentException("User ID is required");
            }

            if (this.processedSettings == null || !new SettingsSchema().isSettingsValid(this.processedSettings)) {
                LoggerService.log(LogLevelEnum.ERROR, "SETTINGS_SCHEMA_INVALID", null);
                return;
            }

            SetAttributeAPI.setAttribute(this.processedSettings, attributeMap, context);
        } catch (Exception exception) {
            LoggerService.log(LogLevelEnum.ERROR, "API_THROW_ERROR", new HashMap<String, String>() {{
                put("apiName", apiName);
                put("err", exception.toString());
            }});
        }
    }

    /**
     * Sets a single attribute for a user in the context provided.
     * This is an overloaded version that accepts individual key and value parameters.
     * @param key - The attribute key to be set
     * @param value - The attribute value to be set
     * @param context User context
     */
    public void setAttribute(String key, Object value, VWOContext context) {
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put(key, value);
        setAttribute(attributeMap, context);
    }

    public boolean flushEvents() {
        int accountId = this.processedSettings.getAccountId(); // Fetch account ID from settings
        if (this.batchEventQueue != null) {
            // Access the size of the batchQueue directly
            LoggerService.log(LogLevelEnum.DEBUG, String.format(
                "Flushing events for accountId: %d. Queue size: %d",
                accountId,
                this.batchEventQueue.getBatchQueue().size()  // Get the size of the actual batchQueue
            ));
    
            // Call flushAndClearInterval to clear the queue and flush events
            return this.batchEventQueue.flushAndClearInterval();
        } else {
            LoggerService.log(LogLevelEnum.ERROR, String.format(
                "Cannot flush events. Batching is not initialized for accountId: %d",
                accountId
            ));
            return false;
        }
    }

     /**
     * This method is used to update the settings on the VWOClient instance
     * It validates the new settings and updates the processedSettings
     * @param newSettings New settings to be updated
     */
    private void updateSettingsOnVWOClient(String newSettings) {
        try {
            if (newSettings == null || newSettings.isEmpty()) {
                throw new IllegalArgumentException("Settings cannot be empty");
            }
            // Read the new settings and update the processedSettings
            this.processedSettings = objectMapper.readValue(newSettings, Settings.class);

            // Check if the new settings are valid
            boolean settingsValid = new SettingsSchema().isSettingsValid(this.processedSettings);
            if (settingsValid) {
                // Process the new settings and update the client instance
                SettingsUtil.processSettings(this.processedSettings);
            } else {
                throw new IllegalStateException("Settings schema is invalid");
            }
        } catch (Exception exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    /**
     * This method is used to update the settings by fetching from server
     */
    public String updateSettings() {
        return this.updateSettings(true);
    }

    /**
     * This method is used to update the settings with provided settings string
     * @param settings New settings to be updated
     */
    public String updateSettings(String settings) {
        String apiName = "updateSettings";
        try {
            LoggerService.log(LogLevelEnum.DEBUG, "API_CALLED", new HashMap<String, String>() {{
                put("apiName", apiName);
            }});

            String settingsToUpdate = settings;
            if (settings == null || settings.isEmpty()) {
                settingsToUpdate = this.updateSettings(true);
            }
            // Update the settings on the VWOClient instance
            this.updateSettingsOnVWOClient(settingsToUpdate);
            LoggerService.log(LogLevelEnum.INFO, "SETTINGS_UPDATED", new HashMap<String, String>() {{
                put("apiName", apiName);
            }});
            return settingsToUpdate;
        } catch (Exception exception) {
            LoggerService.log(LogLevelEnum.ERROR, "SETTINGS_FETCH_FAILED", new HashMap<String, String>() {{
                put("apiName", apiName);
                put("err", exception.toString());
            }});
            return null;
        }
    }

    /**
     * This method is used to update the settings
     * @param isViaWebhook Boolean value to indicate if the settings are being fetched via webhook
     */
    public String updateSettings(Boolean isViaWebhook) {
        String apiName = "updateSettings";
        try {
            // Fetch the new settings from the server
            this.settings = SettingsManager.getInstance().fetchSettings(isViaWebhook);
            return this.updateSettings(this.settings);
        } catch (Exception exception) {
            LoggerService.log(LogLevelEnum.ERROR, "SETTINGS_FETCH_FAILED", new HashMap<String, String>() {{
                put("apiName", apiName);
                put("isViaWebhook", isViaWebhook.toString());
                put("err", exception.toString());
            }});
            return null;
        }
    }
}