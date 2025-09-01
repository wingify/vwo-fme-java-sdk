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

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.vwo.VWOClient;
import com.vwo.constants.Constants;
import com.vwo.enums.HeadersEnum;
import com.vwo.enums.UrlEnum;
import com.vwo.models.FlushInterface;
import com.vwo.models.Settings;
import com.vwo.models.request.Event;
import com.vwo.models.request.EventArchData;
import com.vwo.models.request.EventArchPayload;
import com.vwo.models.request.EventArchQueryParams.RequestQueryParams;
import com.vwo.models.request.EventArchQueryParams.SettingsQueryParams;
import com.vwo.models.request.Props;
import com.vwo.models.request.visitor.Visitor;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.network_layer.manager.NetworkManager;
import com.vwo.packages.network_layer.models.RequestModel;
import com.vwo.packages.network_layer.models.ResponseModel;
import com.vwo.ServiceContainer;
import com.vwo.services.SettingsManager;

import com.vwo.enums.EventEnum;

public class NetworkUtil {

    /**
     * Creates the query parameters for the settings API.
     * @param apikey  The sdk key of the account.
     * @param accountId The ID of the account.
     * @return
     */
    public Map<String, String> getSettingsPath(String apikey, Integer accountId) {
        SettingsQueryParams settingsQueryParams = new SettingsQueryParams(apikey, generateRandom(), accountId.toString());
        return settingsQueryParams.getQueryParams();
    }

    /**
     * Creates the base properties for the event arch APIs.
     * @param settingsManager The settings manager containing configuration.
     * @param eventName  The name of the event.
     * @param visitorUserAgent  The user agent of the user.
     * @param ipAddress  The IP address of the user.
     * @return
     */
    public static Map<String, String> getEventsBaseProperties(SettingsManager settingsManager, String eventName, String visitorUserAgent, String ipAddress) {
        RequestQueryParams requestQueryParams = new RequestQueryParams(
            eventName,
            settingsManager.accountId.toString(),
            settingsManager.sdkKey,
            visitorUserAgent,
            ipAddress
        );
        return requestQueryParams.getQueryParams();
    }

    /**
     * Creates the base payload for the event arch APIs.
     * @param settingsManager The settings manager containing configuration.
     * @param userId  The ID of the user.
     * @param eventName The name of the event.
     * @param visitorUserAgent The user agent of the user.
     * @param ipAddress The IP address of the user.
     * @return
     */
    public static EventArchPayload getEventBasePayload(SettingsManager settingsManager, String userId, String eventName, String visitorUserAgent, String ipAddress) {
        String uuid = UUIDUtils.getUUID(userId, settingsManager.accountId.toString());
        EventArchData eventArchData = new EventArchData();
        eventArchData.setMsgId(generateMsgId(uuid));
        eventArchData.setVisId(uuid);
        eventArchData.setSessionId(generateSessionId());
        setOptionalVisitorData(eventArchData, visitorUserAgent, ipAddress);

        Event event = createEvent(settingsManager.sdkKey, eventName);
        eventArchData.setEvent(event);

        Visitor visitor = createVisitor(settingsManager.sdkKey);
        eventArchData.setVisitor(visitor);

        EventArchPayload eventArchPayload = new EventArchPayload();
        eventArchPayload.setD(eventArchData);
        return eventArchPayload;
    }

    /**
     * Sets the optional visitor data for the event arch APIs.
     * @param eventArchData The event model containing the event data.
     * @param visitorUserAgent The user agent of the user.
     * @param ipAddress The IP address of the user.
     */
    private static void setOptionalVisitorData(EventArchData eventArchData, String visitorUserAgent, String ipAddress) {
        if (visitorUserAgent != null && !visitorUserAgent.isEmpty()) {
            eventArchData.setVisitor_ua(visitorUserAgent);
        }

        if (ipAddress != null && !ipAddress.isEmpty()) {
            eventArchData.setVisitor_ip(ipAddress);
        }
    }

    /**
     * Creates the event model for the event arch APIs.
     * @param sdkKey The SDK key.
     * @param eventName The name of the event.
     * @return The event model.
     */
    private static Event createEvent(String sdkKey, String eventName) {
        Event event = new Event();
        Props props = createProps(sdkKey);
        event.setProps(props);
        event.setName(eventName);
        event.setTime(Calendar.getInstance().getTimeInMillis());
        return event;
    }

    /**
     * Creates the visitor model for the event arch APIs.
     * @param sdkKey The SDK key.
     * @return The visitor model.
     */
    private static Props createProps(String sdkKey) {
        Props props = new Props();
        props.setSdkName(Constants.SDK_NAME);
        props.setSdkVersion(Constants.SDK_VERSION);
        props.setEnvKey(sdkKey);
        return props;
    }

    /**
     * Creates the visitor model for the event arch APIs.
     * @param sdkKey The SDK key.
     * @return The visitor model.
     */
    private static Visitor createVisitor(String sdkKey) {
        Visitor visitor = new Visitor();
        Map<String, Object> visitorProps = new HashMap<>();
        visitorProps.put(Constants.VWO_FS_ENVIRONMENT, sdkKey);
        visitor.setProps(visitorProps);
        return visitor;
    }

    /**
     * Returns the payload data for the track user API.
     * @param serviceContainer  The service container containing configuration.
     * @param userId  The ID of the user.
     * @param eventName  The name of the event.
     * @param campaignId The ID of the campaign.
     * @param variationId  The ID of the variation.
     * @param visitorUserAgent  The user agent of the user.
     * @param ipAddress  The IP address of the user.
     * @return
     */
    public static Map<String, Object> getTrackUserPayloadData(ServiceContainer serviceContainer, String userId, String eventName, Integer campaignId, Integer variationId, String visitorUserAgent, String ipAddress) {
        EventArchPayload properties = getEventBasePayload(serviceContainer.getSettingsManager(), userId, eventName, visitorUserAgent, ipAddress);
        properties.getD().getEvent().getProps().setId(campaignId);
        properties.getD().getEvent().getProps().setVariation(variationId.toString());
        properties.getD().getEvent().getProps().setIsFirst(1);
        
        if (UsageStatsUtil.getInstance().getUsageStats() != null && !UsageStatsUtil.getInstance().getUsageStats().isEmpty()) {
            properties.getD().getEvent().getProps().setVwoMeta(UsageStatsUtil.getInstance().getUsageStats());
        }

        serviceContainer.getLoggerService().log(LogLevelEnum.DEBUG, "IMPRESSION_FOR_TRACK_USER", new HashMap<String, String>() {
            {
                put("accountId", serviceContainer.getSettingsManager().accountId.toString());
                put("userId", userId);
                put("campaignId", campaignId.toString());
            }
        });
        Map<String, Object> payload = VWOClient.objectMapper.convertValue(properties, Map.class);
        return removeNullValues(payload);
    }

    /**
     * Returns the payload data for the goal API.
     * @param serviceContainer  The service container containing configuration.
     * @param userId  The ID of the user.
     * @param eventName  The name of the event.
     * @param context  The user context model containing user-specific data.
     * @param eventProperties event properties for the event
     * @return  Map containing the payload data.
     */
    public static Map<String, Object> getTrackGoalPayloadData(ServiceContainer serviceContainer, String userId, String eventName, VWOContext context, Map<String, ?> eventProperties) {
        EventArchPayload properties = getEventBasePayload(serviceContainer.getSettingsManager(), userId, eventName, context.getUserAgent(), context.getIpAddress());
        properties.getD().getEvent().getProps().setIsCustomEvent(true);
        addCustomEventProperties(properties, (Map<String, Object>) eventProperties);
        serviceContainer.getLoggerService().log(LogLevelEnum.DEBUG, "IMPRESSION_FOR_TRACK_GOAL", new HashMap<String, String>() {
            {
                put("eventName", eventName);
                put("accountId", serviceContainer.getSettingsManager().accountId.toString());
                put("userId", userId);
            }
        });
        Map<String, Object> payload = VWOClient.objectMapper.convertValue(properties, Map.class);
        return removeNullValues(payload);
    }

    /**
     * Adds custom event properties to the payload.
     * @param properties The payload data for the event.
     * @param eventProperties The custom event properties to add.
     */
    private static void addCustomEventProperties(EventArchPayload properties, Map<String, Object> eventProperties) {
        if (eventProperties != null) {
            properties.getD().getEvent().getProps().setAdditionalProperties(eventProperties);
        }
    }

    /**
     * Returns the payload data for the attribute API.
     * @param serviceContainer  The service container containing configuration.
     * @param userId  The ID of the user.
     * @param eventName The name of the event.
     * @param attributeMap - Map of attribute key and value to be set
     * @return
     */
    public static Map<String, Object> getAttributePayloadData(ServiceContainer serviceContainer, String userId, String eventName, Map<String, Object> attributeMap) {
        EventArchPayload properties = getEventBasePayload(serviceContainer.getSettingsManager(), userId, eventName, null, null);
        properties.getD().getEvent().getProps().setIsCustomEvent(true);
        properties.getD().getVisitor().getProps().putAll(attributeMap);
        serviceContainer.getLoggerService().log(LogLevelEnum.DEBUG, "IMPRESSION_FOR_SYNC_VISITOR_PROP", new HashMap<String, String>() {
            {
                put("eventName", eventName);
                put("accountId", serviceContainer.getSettingsManager().accountId.toString());
                put("userId", userId);
            }
        });
        Map<String, Object> payload = VWOClient.objectMapper.convertValue(properties, Map.class);
        return removeNullValues(payload);
    }

    /**
     * Returns the payload data for the log to VWO event API.
     * @param messageType The type of the log message.
     * @param message The log message content.
     * @param eventName The name of the event.
     * @return Map containing the event payload data
     */
    public static Map<String, Object> getLogToVWOEventPayload(SettingsManager settingsManager, String messageType, String message, String eventName) {
        String userId = settingsManager.accountId.toString() + "_" + settingsManager.sdkKey;
        EventArchPayload properties = getEventBasePayload(settingsManager, userId, eventName, null, null);

        // set product to FME
        properties.getD().getEvent().getProps().setProduct(Constants.FME);

        // set data object to send log messages to VWO server
        Map<String, Object> data = new HashMap<>();
        // set type of the log message
        data.put("type", messageType);

        // set content of the log message
        Map<String, Object> content = new HashMap<>();
        content.put("title", message);
        content.put("dateTime", Calendar.getInstance().getTimeInMillis());
        // set content of the log message
        data.put("content", content);

        // set data object to send log messages to VWO server
        properties.getD().getEvent().getProps().setData(data);
        Map<String, Object> payload = VWOClient.objectMapper.convertValue(properties, Map.class);
        return removeNullValues(payload);
    }

    /**
     * Sends a POST request to the VWO server.
     * @param properties The properties required for the request.
     * @param payload  The payload data for the request.
     * @param userAgent The user agent of the user.
     * @param ipAddress The IP address of the user.
     */
    public static void sendPostApiRequest(ServiceContainer serviceContainer, Map<String, String> properties, Map<String, Object> payload, String userAgent, String ipAddress) {
        try {
            NetworkManager.getInstance().attachClient();
            String eventName = properties.get("en");
            Map<String, String> headers = createHeaders(userAgent, ipAddress);
            RequestModel request = new RequestModel(serviceContainer.getBaseUrl(), "POST", UrlEnum.EVENTS.getUrl(), properties, payload, headers, serviceContainer.getSettingsManager().protocol, serviceContainer.getSettingsManager().port);
            NetworkManager.getInstance().getExecutorService().submit(() -> {
                try {
                    ResponseModel response = NetworkManager.getInstance().post(request, null);
                    if (response != null && response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                        UsageStatsUtil.getInstance().clearUsageStats();
                    } else {
                        serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "NETWORK_CALL_EXCEPTION", new HashMap<String, String>() {
                            {
                                put("event", eventName);
                                put("endPoint", UrlEnum.EVENTS.getUrl());
                                put("accountId", serviceContainer.getSettingsManager().accountId.toString());
                                put("err", response.getError().getMessage());
                            }
                        });
                    }   
                } catch (Exception exception) {
                    serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "NETWORK_CALL_EXCEPTION", new HashMap<String, String>() {
                        {
                            put("event", eventName);
                            put("endPoint", UrlEnum.EVENTS.getUrl());
                            put("accountId", serviceContainer.getSettingsManager().accountId.toString());
                            put("err", exception.getMessage());
                        }
                    });
                }
            });
        } catch (Exception exception) {
            serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "NETWORK_CALL_FAILED", new HashMap<String, String>() {
                {
                    put("method", "POST");
                    put("err", exception.toString());
                }
            });
        }
    }

    /**
     * Sends a batch POST request to the VWO server with the specified payload and account details.
     *
     * @param payload   The payload data to be sent in the request body. This can include event-related information.
     * @param accountId The account ID to associate with the request, used as a query parameter.
     * @param sdkKey    The API key to authenticate the request in the headers.
     */
    public static Boolean sendPostBatchRequest(Settings settings, Object payload, int accountId, String sdkKey, FlushInterface flushCallback) {
        // Create the batch payload
        Map<String, Object> batchPayload = new HashMap<>();
        batchPayload.put("ev", payload);

        // Create the query parameters
        Map<String, String> query = new HashMap<>();
        query.put("a", String.valueOf(accountId));
        query.put("env", sdkKey);

        String url = Constants.HOST_NAME;
        if (settings.getCollectionPrefix() != null && !settings.getCollectionPrefix().isEmpty()) {
            url = url + "/" + settings.getCollectionPrefix();
        }

        // Create the request model
        RequestModel requestModel = new RequestModel(
                url,
                "POST",
                UrlEnum.BATCH_EVENTS.getUrl(),
                query,
                batchPayload,
                new HashMap<String, String>() {{
                    put("Authorization", sdkKey);
                    put("Content-Type", "application/json");
                }},
                Constants.HTTPS_PROTOCOL,
                0
        );

        // Send the request asynchronously
        NetworkManager.getInstance().post(requestModel, flushCallback);  // Return the result of postAsync 
        return true;
    }    

    /**
     * Sends a messaging event to the VWO server.
     * @param settingsManager The settings manager containing configuration.
     * @param properties The properties required for the request.
     * @param payload The payload data for the request.
     */
    public static void sendEventDirectlyToDacdn(SettingsManager settingsManager, Map<String, String> properties, Map<String, Object> payload, String eventName) {
        NetworkManager.getInstance().attachClient();
        Map<String, String> headers = createHeaders(null, null);
        
        RequestModel request = new RequestModel(Constants.HOST_NAME, "POST", UrlEnum.EVENTS.getUrl(), properties, payload, headers, Constants.HTTPS_PROTOCOL, 0);
        
        NetworkManager.getInstance().getExecutorService().submit(() -> {
            try {
                ResponseModel response = NetworkManager.getInstance().post(request, null);
                if (response != null && response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    UsageStatsUtil.getInstance().clearUsageStats();
                } else {
                    if (!eventName.equals(EventEnum.VWO_ERROR.getValue())) {
                        settingsManager.loggerService.log(LogLevelEnum.ERROR, "NETWORK_CALL_EXCEPTION", new HashMap<String, String>() {
                            {
                                put("event", eventName);
                                put("endPoint", UrlEnum.EVENTS.getUrl());
                                put("accountId", settingsManager.accountId.toString());
                                put("err", response.getError().getMessage());
                            }
                        });
                    }
                }
            } catch (Exception exception) {
                if (!eventName.equals(EventEnum.VWO_ERROR.getValue())) {
                    settingsManager.loggerService.log(LogLevelEnum.ERROR, "NETWORK_CALL_EXCEPTION", new HashMap<String, String>() {
                        {
                            put("event", eventName);
                        put("endPoint", UrlEnum.EVENTS.getUrl());
                        put("accountId", settingsManager.accountId.toString());
                        put("err", exception.getMessage());
                        }
                    });
                }
            }
        });
    }

    /**
     * Removes null values from the map. If the value is a map, recursively removes null values from the nested map.
     * @param originalMap The map containing null/non-null values
     * @return  Map containing non-null values.
     */
    public static Map<String, Object> removeNullValues(Map<String, Object> originalMap) {
        Map<String, Object> cleanedMap = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                // Recursively remove null values from nested maps
                value = removeNullValues((Map<String, Object>) value);
            }
            if (value != null) {
                cleanedMap.put(entry.getKey(), value);
            }
        }

        return cleanedMap;
    }

    /**
     * Generates the UUID for the user.
     * @return The UUID for the user.
     */
    private static String generateRandom() {
        return Double.toString(Math.random());
    }

    /**
     * Generates a message ID for the event. The message ID is a combination of the UUID and the current timestamp.
     * @param uuid The UUID of the user.
     * @return The message ID.
     */
    private static String generateMsgId(String uuid) {
        return uuid + "-" + Calendar.getInstance().getTimeInMillis();
    }

    /**
     * Generates a session ID for the event.
     * @return The session ID.
     */
    private static long generateSessionId() {
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }

    /**
     * Creates the headers for the request. Adds the user agent and IP address to the headers if they are not null or empty.
     * @param userAgent The user agent of the user.
     * @param ipAddress The IP address of the user.
     * @return Map containing the headers.
     */
    private static Map<String, String> createHeaders(String userAgent, String ipAddress) {
        Map<String, String> headers = new HashMap<>();
        if (userAgent != null && !userAgent.isEmpty()) headers.put(HeadersEnum.USER_AGENT.getHeader(), userAgent);
        if (ipAddress != null && !ipAddress.isEmpty()) headers.put(HeadersEnum.IP.getHeader(), ipAddress);
        return headers;
    }

    /**
     * Constructs the payload for SDK init called event.
     * @param settingsManager The settings manager containing configuration.
     * @param eventName The name of the event.
     * @param settingsFetchTime Time taken to fetch settings in milliseconds (can be null).
     * @param sdkInitTime Time taken to initialize the SDK in milliseconds (can be null).
     * @return The constructed payload with required fields.
     */
    public static Map<String, Object> getSdkInitEventPayload(SettingsManager settingsManager, String eventName, Long settingsFetchTime, Long sdkInitTime) {
        // Get settings and create user ID
        String userId = settingsManager.accountId.toString() + "_" + settingsManager.sdkKey;
        EventArchPayload properties = getEventBasePayload(settingsManager, userId, eventName, null, null);
        // Set the required fields as specified
        properties.getD().getEvent().getProps().setEnvKey(settingsManager.sdkKey);
        properties.getD().getEvent().getProps().setProduct(Constants.FME);
        
        // Create data object
        Map<String, Object> data = new HashMap<>();
        data.put("isSDKInitialized", true);
        data.put("settingsFetchTime", settingsFetchTime);
        data.put("sdkInitTime", sdkInitTime);
        
        // Set additional properties
        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("data", data);
        properties.getD().getEvent().getProps().setAdditionalProperties(additionalProps);

        // Convert to Map and return
        Map<String, Object> payload = VWOClient.objectMapper.convertValue(properties, Map.class);
        return removeNullValues(payload);
    }

}
