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

import com.vwo.enums.EventEnum;
import com.vwo.enums.DebuggerCategoryEnum;
import com.vwo.services.LoggerService;
import com.vwo.services.SettingsManager;
import com.vwo.constants.Constants;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.network_layer.models.ResponseModel;

import static com.vwo.utils.LogMessageUtil.buildMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility functions for handling debugger service operations including
 * filtering sensitive properties and extracting decision keys.
 */
public class DebuggerServiceUtil {

    /**
     * Extracts only the required fields from a decision object.
     * @param decisionObj The decision object to extract fields from
     * @return A map containing only rolloutKey and experimentKey if they exist
     */
    public static Map<String, Object> extractDecisionKeys(Map<String, Object> decisionObj) {
        Map<String, Object> extractedKeys = new HashMap<>();
        
        if (decisionObj == null) {
            return extractedKeys;
        }

        // Extract rolloutId if present
        if (decisionObj.containsKey("rolloutId") && decisionObj.get("rolloutId") != null) {
            extractedKeys.put("rId", decisionObj.get("rolloutId"));
        }

        // Extract rolloutVariationId if present
        if (decisionObj.containsKey("rolloutVariationId") && decisionObj.get("rolloutVariationId") != null) {
            extractedKeys.put("rvId", decisionObj.get("rolloutVariationId"));
        }

        // Extract experimentId if present
        if (decisionObj.containsKey("experimentId") && decisionObj.get("experimentId") != null) {
            extractedKeys.put("eId", decisionObj.get("experimentId"));
        }

        // Extract experimentVariationId if present
        if (decisionObj.containsKey("experimentVariationId") && decisionObj.get("experimentVariationId") != null) {
            extractedKeys.put("evId", decisionObj.get("experimentVariationId"));
        }

        return extractedKeys;
    }

    /**
     * Creates a network and retry debug event.
     * Determines category (RETRY vs NETWORK) based on response status code.
     * @param response The response model containing status code and error info
     * @param payload The payload for the request (can be null)
     * @param apiName The name of the API (e.g., getFlag, trackEvent)
     * @param extraData Extra data for the message (e.g., feature name, endpoint)
     * @return Map containing debug event properties
     */
    public static Map<String, Object> createNetWorkAndRetryDebugEvent(
            ResponseModel response,
            Object payload,
            String apiName,
            String extraData
    ) {
        try {
            // Determine category based on status code
            String category = DebuggerCategoryEnum.RETRY.getValue();
            String msgType = Constants.NETWORK_CALL_SUCCESS_WITH_RETRIES;
            String logLevel = LogLevelEnum.INFO.toString();
            
            String errorMsg = response.getError() != null ? response.getError().getMessage() : "";
            int attempts = response.getTotalAttempts();
            
            String msg = buildMessage(LoggerService.infoMessages.get("NETWORK_CALL_SUCCESS_WITH_RETRIES"), 
                new HashMap<String, Object>() {{
                    put("extraData", extraData);
                    put("attempts", attempts);
                    put("err", errorMsg);
                }});

            // If not 200, switch to NETWORK category (failure)
            if (response.getStatusCode() != Constants.HTTP_OK) {
                category = DebuggerCategoryEnum.NETWORK.getValue();
                msgType = Constants.NETWORK_CALL_FAILURE_AFTER_MAX_RETRIES;
                logLevel = LogLevelEnum.ERROR.toString();
                msg = buildMessage(LoggerService.errorMessages.get("NETWORK_CALL_FAILURE_AFTER_MAX_RETRIES"), 
                    new HashMap<String, Object>() {{
                        put("extraData", extraData);
                        put("attempts", attempts);
                        put("err", errorMsg);
                    }});
            }

            // Build debug event props
            Map<String, Object> debugEventProps = new HashMap<>();
            debugEventProps.put("cg", category);
            debugEventProps.put("msg_t", msgType);
            debugEventProps.put("msg", msg);
            debugEventProps.put("lt", logLevel);

            if (apiName != null && !apiName.isEmpty()) {
                debugEventProps.put("an", apiName);
            }

            // Extract session ID from payload if available
            if (payload instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payloadMap = (Map<String, Object>) payload;
                if (payloadMap.containsKey("d")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> d = (Map<String, Object>) payloadMap.get("d");
                    if (d != null && d.containsKey("sessionId")) {
                        debugEventProps.put("sId", d.get("sessionId"));
                    }
                }
            }
            
            if (!debugEventProps.containsKey("sId")) {
                debugEventProps.put("sId", FunctionUtil.getCurrentUnixTimestampInMillis());
            }

            return debugEventProps;
        } catch (Exception err) {
            // Fallback on error
            Map<String, Object> errorProps = new HashMap<>();
            errorProps.put("cg", DebuggerCategoryEnum.NETWORK.getValue());
            errorProps.put("an", apiName != null ? apiName : "");
            errorProps.put("msg_t", Constants.NETWORK_CALL_FAILURE_AFTER_MAX_RETRIES);
            errorProps.put("lt", LogLevelEnum.ERROR.toString());
            errorProps.put("sId", FunctionUtil.getCurrentUnixTimestampInMillis());
            return errorProps;
        }
    }

    /**
     * Sends a debug event to VWO.
     * @param settingsManager The settings manager containing configuration
     * @param eventProps The properties for the event
     */
    public static void sendDebugEventToVWO(SettingsManager settingsManager, Map<String, Object> eventProps) {
        try {
            // Create query parameters
            Map<String, String> properties = NetworkUtil.getEventsBaseProperties(
                settingsManager,
                EventEnum.VWO_DEBUGGER_EVENT.getValue(),
                null,
                null
            );

            // Create payload
            Map<String, Object> payload = NetworkUtil.getDebuggerEventPayload(
                settingsManager,
                eventProps != null ? eventProps : new HashMap<>()
            );

            // Send event
            NetworkUtil.sendEventDirectlyToDacdn(
                settingsManager,
                properties,
                payload,
                EventEnum.VWO_DEBUGGER_EVENT.getValue()
            );
        } catch (Exception e) {}
    }
}