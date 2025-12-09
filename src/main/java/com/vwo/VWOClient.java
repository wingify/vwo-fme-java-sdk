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
import com.vwo.utils.AliasingUtil;
import com.vwo.utils.DataTypeUtil;
import com.vwo.utils.SettingsUtil;
import com.vwo.services.BatchEventQueue;
import com.vwo.utils.EventUtil;
import com.vwo.enums.EventEnum;
import com.vwo.enums.ApiEnum;
import com.vwo.utils.UserIdUtil;

import java.util.HashMap;
import java.util.Map;

public class VWOClient {
    private Settings processedSettings;
    public String settings;
    private VWOInitOptions options;
    private Boolean isSettingsValid = false;
    public static ObjectMapper objectMapper = new ObjectMapper(){
        {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
    };
    private BatchEventQueue batchEventQueue;
    private VWOBuilder vwoBuilder;

    public VWOClient(String settings, VWOBuilder vwoBuilder) {
        try {
            this.options = vwoBuilder.options;
            this.vwoBuilder = vwoBuilder;
            if (settings == null) {
                return;
            }
            this.settings = settings;
            this.processedSettings = objectMapper.readValue(settings, Settings.class);
            if (vwoBuilder.getBatchEventQueue() != null) {
                vwoBuilder.getBatchEventQueue().setSettings(this.processedSettings);
            }
            if (!DataTypeUtil.isNull(this.processedSettings.getCollectionPrefix()) && !this.processedSettings.getCollectionPrefix().isEmpty()) {
                this.vwoBuilder.getSettingsManager().collectionPrefix = this.processedSettings.getCollectionPrefix();
            }
            SettingsUtil.processSettings(this.processedSettings, this.vwoBuilder.getLoggerService());
        } catch (Exception exception) {
           System.err.println("exception occurred while parsing settings " + exception.getMessage());
        }
    }

    /**
     * This method is used to send the sdk init event
     * @param settingsInitTime The time taken to initialize the settings
     */
    protected void sendSdkInitAndUsageStatsEvent(long settingsInitTime) {
        try {
            if ( this.processedSettings == null ) {
                throw new IllegalStateException("processedSettings is null");
            }
            // get sdk meta info from settings
            Map<String, Object> sdkMetaInfo = this.processedSettings.getSdkMetaInfo();
            // if sdk meta info is not present, then return
            // if wasInitializedEarlier in sdk meta info is false or is absent, then send the sdk init event
            if (sdkMetaInfo == null || (sdkMetaInfo.get("wasInitializedEarlier") == null || !sdkMetaInfo.get("wasInitializedEarlier").equals(true))) {
                // check if settings are valid on init
                if (this.vwoBuilder.getSettingsService().isSettingsValidOnInit) {
                    // send the sdk init event
                    EventUtil.sendSdkInitEvent(this.vwoBuilder.getSettingsManager(), this.vwoBuilder.getSettingsManager().settingsFetchTime, settingsInitTime, EventEnum.VWO_SDK_INIT_EVENT.getValue());
                }
            }

            // get usage stats account id from settings
            Integer usageStatsAccountId = this.processedSettings.getUsageStatsAccountId();
            if (!DataTypeUtil.isNull(usageStatsAccountId) && usageStatsAccountId != 0) {
                EventUtil.sendUsageStatsEvent(this.vwoBuilder.getSettingsManager(), usageStatsAccountId);
            }
        } catch (Exception exception) {
            vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "SDK_INIT_EVENT_FAILED", new HashMap<String, Object>() {{
                put("err", exception.getMessage());
                put("an", ApiEnum.INIT.getValue());
            }});
        }
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
            vwoBuilder.getLoggerService().log(LogLevelEnum.DEBUG, "API_CALLED", new HashMap<String, Object>() {{
                put("apiName", apiName);
            }});

            if (context == null || context.getId() == null || context.getId().isEmpty()) {
                getFlag.setIsEnabled(false);
                throw new IllegalArgumentException("User ID is required");
            }

            if (featureKey == null || featureKey.isEmpty()) {
                getFlag.setIsEnabled(false);
                throw new IllegalArgumentException("Feature Key is required");
            }

            if (!this.validateSettings(this.processedSettings, ApiEnum.GET_FLAG)) {
                getFlag.setIsEnabled(false);
                return getFlag;
            }

            // create Service Container instance
            ServiceContainer serviceContainer = new ServiceContainer(context.getId(), this.vwoBuilder.getLoggerService(), this.vwoBuilder.getSettingsManager(), this.options, vwoBuilder.getBatchEventQueue(), this.processedSettings);

            // get userId from gateway service
            if (this.options.getIsAliasingEnabled()) {
                context.setId(UserIdUtil.getUserId(context.getId(), serviceContainer));
                serviceContainer.setUuid(context.getId());
            }

            return GetFlagAPI.getFlag(featureKey, context, serviceContainer);
        } catch (Exception exception) {
            vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "EXECUTION_FAILED", new HashMap<String, Object>() {{
                put("apiName", "getFlag");
                put("err", exception.getMessage());
                put("an", ApiEnum.GET_FLAG.getValue());
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
            vwoBuilder.getLoggerService().log(LogLevelEnum.DEBUG, "API_CALLED", new HashMap<String, Object>() {{
                put("apiName", apiName);
            }});
            
            if (!DataTypeUtil.isString(eventName)) {
                vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_PARAM", new HashMap<String, Object>() {{
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

            if (!this.validateSettings(this.processedSettings, ApiEnum.TRACK_EVENT)) {
                resultMap.put(eventName, false);
                return resultMap;
            }

            // create Service Container instance
            ServiceContainer serviceContainer = new ServiceContainer(context.getId(), this.vwoBuilder.getLoggerService(), this.vwoBuilder.getSettingsManager(), this.options, vwoBuilder.getBatchEventQueue(), this.processedSettings);

            // get userId from gateway service
            if (this.options.getIsAliasingEnabled()) {
                context.setId(UserIdUtil.getUserId(context.getId(), serviceContainer));
                serviceContainer.setUuid(context.getId());
            }

            Boolean result = TrackEventAPI.track(eventName, context, eventProperties, serviceContainer);
            if (result) {
                resultMap.put(eventName, true);
            } else {
                resultMap.put(eventName, false);
            }
            return resultMap;
        } catch (Exception exception) {
            vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "EXECUTION_FAILED", new HashMap<String, Object>() {{
                put("apiName", apiName);
                put("err", exception.getMessage());
                put("an", ApiEnum.TRACK_EVENT.getValue());
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
            vwoBuilder.getLoggerService().log(LogLevelEnum.DEBUG, "API_CALLED", new HashMap<String, Object>() {{
                put("apiName", apiName);
            }});
            if (attributeMap == null || attributeMap.isEmpty()) {
                throw new IllegalArgumentException("TypeError: attributeMap should be a non-empty map of type Map<String, Object>");
            }

            // Validate attribute values
            for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
                Object value = entry.getValue();
                if (!(value instanceof String || value instanceof Number || value instanceof Boolean)) {
                    vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_PARAM", new HashMap<String, Object>() {{
                        put("apiName", apiName);
                        put("key", "attributeValue");
                        put("type", value != null ? value.getClass().getSimpleName() : "null");
                        put("correctType", "String, Number, or Boolean");
                    }}, false);
                    throw new IllegalArgumentException("TypeError: Attribute value must be a String, Number, or Boolean");
                }
            }

            if (context == null || context.getId() == null || context.getId().isEmpty()) {
                throw new IllegalArgumentException("User ID is required");
            }

            if (!this.validateSettings(this.processedSettings, ApiEnum.SET_ATTRIBUTE)) {
                return;
            }

            // create Service Container instance
            ServiceContainer serviceContainer = new ServiceContainer(context.getId(), this.vwoBuilder.getLoggerService(), this.vwoBuilder.getSettingsManager(), this.options, vwoBuilder.getBatchEventQueue(), this.processedSettings);

            // get userId from gateway service
            if (this.options.getIsAliasingEnabled()) {
                context.setId(UserIdUtil.getUserId(context.getId(), serviceContainer));
                serviceContainer.setUuid(context.getId());
            }

            SetAttributeAPI.setAttribute(attributeMap, context, serviceContainer);
        } catch (Exception exception) {
            vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "EXECUTION_FAILED", new HashMap<String, Object>() {{
                put("apiName", apiName);
                put("err", exception.getMessage());
                put("an", ApiEnum.SET_ATTRIBUTE.getValue());
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
            vwoBuilder.getLoggerService().log(LogLevelEnum.DEBUG, String.format(
                "Flushing events for accountId: %d. Queue size: %d",
                accountId,
                this.batchEventQueue.getBatchQueue().size()  // Get the size of the actual batchQueue
            ));
    
            // Call flushAndClearInterval to clear the queue and flush events
            return this.batchEventQueue.flushAndClearInterval();
        } else {
            vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "BATCHING_NOT_ENABLED", new HashMap<String, Object>() {{
                put("an", ApiEnum.FLUSH_EVENTS.getValue());
                put("accountId", accountId);
            }});
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
            this.settings = newSettings;
            if (this.validateSettings(this.processedSettings, ApiEnum.UPDATE_SETTINGS)) {
                // Process the new settings and update the client instance
                SettingsUtil.processSettings(this.processedSettings, this.vwoBuilder.getLoggerService());
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
            vwoBuilder.getLoggerService().log(LogLevelEnum.DEBUG, "API_CALLED", new HashMap<String, Object>() {{
                put("apiName", apiName);
            }});

            if (settings == null || settings.isEmpty()) {
                vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_SETTINGS_SCHEMA", new HashMap<String, Object>() {{
                    put("errors", "Settings object is null");
                    put("accountId", options.getAccountId().toString());
                    put("sdkKey", options.getSdkKey());
                    put("settings", "null");
                    put("an", ApiEnum.UPDATE_SETTINGS.getValue());
                }});
                return null;
            }
            
            // Update the settings on the VWOClient instance
            this.updateSettingsOnVWOClient(settings);
            vwoBuilder.getLoggerService().log(LogLevelEnum.INFO, "SETTINGS_UPDATED", new HashMap<String, Object>() {{
                put("apiName", apiName);
                put("isViaWebhook", "false");
            }});
            return settings;
        } catch (Exception exception) {
            vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "UPDATING_CLIENT_INSTANCE_FAILED_WHEN_WEBHOOK_TRIGGERED", new HashMap<String, Object>() {{
                put("apiName", apiName);
                put("err", exception.getMessage());
                put("isViaWebhook", "false");
                put("an", ApiEnum.UPDATE_SETTINGS.getValue());
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
            this.settings = this.vwoBuilder.getSettingsManager().fetchSettings(isViaWebhook);
            return this.updateSettings(this.settings);
        } catch (Exception exception) {
            vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "UPDATING_CLIENT_INSTANCE_FAILED_WHEN_WEBHOOK_TRIGGERED", new HashMap<String, Object>() {{
                put("apiName", apiName);
                put("isViaWebhook", isViaWebhook.toString());
                put("err", exception.getMessage());
                put("an", ApiEnum.UPDATE_SETTINGS.getValue());
            }});
            return null;
        }
    }


    /**
     * This method is used to validate the settings
     * @param settings Settings to be validated
     * @return Boolean value indicating if the settings are valid
     */
    private Boolean validateSettings(Settings processedSettings, ApiEnum apiEnum) {
        try {
            if (processedSettings == null) {
                vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_SETTINGS_SCHEMA", new HashMap<String, Object>() {{
                    put("errors", "Settings object is null");
                    put("accountId", options.getAccountId().toString());
                    put("sdkKey", options.getSdkKey());
                    put("settings", "null");
                    put("an", apiEnum.getValue());
                }});
                return false;
            }
            SettingsSchema validationResult = new SettingsSchema().validateSettings(processedSettings);
            if (!validationResult.isValid()) {
                vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_SETTINGS_SCHEMA", new HashMap<String, Object>() {{
                    put("errors", validationResult.getErrorsAsString());
                    put("accountId", options.getAccountId().toString());
                    put("sdkKey", options.getSdkKey());
                    put("settings", settings);
                    put("an", apiEnum.getValue());
                }});
                return false;
            }
            return true;
        } catch (Exception exception) {
            vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_SETTINGS_SCHEMA", new HashMap<String, Object>() {{
                put("errors", exception.getMessage());
                put("accountId", options.getAccountId().toString());
                put("sdkKey", options.getSdkKey());
                put("settings", settings);
                put("an", apiEnum.getValue());
            }});
            return false;
        }
    }

    /**
     * This method is used to set the alias for a given user id
     * @param userId User id to be aliased
     * @param aliasId Alias id to be set for the user id
     * @return Boolean value indicating if the alias was set successfully
     */
    public Boolean setAlias(String userId, String aliasId) {
        String apiName = "setAlias";
        try {
            vwoBuilder.getLoggerService().log(LogLevelEnum.DEBUG, "API_CALLED", new HashMap<String, Object>() {{
                put("apiName", apiName);
            }});

            // check if aliasing is enabled
            if (!this.options.getIsAliasingEnabled()) {
                throw new IllegalArgumentException("Aliasing is not enabled");
            }

            // check if gateway service is provided
            if (!vwoBuilder.getSettingsManager().isGatewayServiceProvided) {
                throw new IllegalArgumentException("Gateway service is not provided");
            }

            // check if user id is provided
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("User ID is required");
            }

            // check if alias id is provided
            if (aliasId == null || aliasId.isEmpty() || aliasId.trim().isEmpty()) {
                throw new IllegalArgumentException("Alias ID is required");
            }

            // remove whitespaces from alias id
            aliasId = aliasId.trim().replaceAll("\\s+", "");

            // trim userId
            userId = userId.trim();

            // check if user id and alias id are the same
            if (userId.equals(aliasId)) {
                throw new IllegalArgumentException("User ID and Alias ID cannot be the same");
            }

            // create Service Container instance
            ServiceContainer serviceContainer = new ServiceContainer(userId, this.vwoBuilder.getLoggerService(), this.vwoBuilder.getSettingsManager(), this.options, vwoBuilder.getBatchEventQueue(), this.processedSettings);

            // set alias on gateway service
            return AliasingUtil.setAlias(userId, aliasId, serviceContainer);
        } catch (Exception exception) {
            vwoBuilder.getLoggerService().log(LogLevelEnum.ERROR, "EXECUTION_FAILED", new HashMap<String, Object>() {{
                put("apiName", apiName);
                put("err", exception.getMessage());
                put("an", ApiEnum.SET_ALIAS.getValue());
            }});
            return false;
        }
    }

    /**
     * This overloaded method is used to set the alias for a given user id
     * @param context User context
     * @param aliasId Alias id to be set for the user id
     * @return Boolean value indicating if the alias was set successfully
     */
    public Boolean setAlias(VWOContext context, String aliasId) {
        return setAlias(context.getId(), aliasId);
    }
}   