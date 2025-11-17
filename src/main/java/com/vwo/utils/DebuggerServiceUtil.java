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
import com.vwo.enums.ApiEnum;
import com.vwo.enums.CampaignTypeEnum;
import com.vwo.services.LoggerService;
import com.vwo.services.SettingsManager;
import com.vwo.models.request.EventArchPayload;
import com.vwo.constants.Constants;
import com.vwo.packages.logger.enums.LogLevelEnum;

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
     * Creates debug event properties for network category
     * @param payload The payload containing event data
     * @return Map containing debug event properties
     */
    public static Map<String, Object> createNetworkDebugEvent(
            EventArchPayload payload, Map<String, Object> featureInfo, Integer accountId, String error) {
        
        try {
            Map<String, Object> debugEventProps = new HashMap<>();
            
            debugEventProps.put("cg", DebuggerCategoryEnum.NETWORK.getValue());
            debugEventProps.put("tRa", 0);
            debugEventProps.put("uuid", payload.getD().getVisId());
            debugEventProps.put("eId", payload.getD().getEvent().getProps().getId());
            debugEventProps.put("sId", payload.getD().getSessionId());
            debugEventProps.put("vId", payload.getD().getEvent().getProps().getVariation());
            debugEventProps.put("msg_t", Constants.NETWORK_CALL_EXCEPTION);
            debugEventProps.put("lt", LogLevelEnum.ERROR.toString());

            String eventName = payload.getD().getEvent().getName();
            if (eventName.equals(EventEnum.VWO_VARIATION_SHOWN.getValue())) {
                String extraData;
                debugEventProps.put("an", ApiEnum.GET_FLAG.getValue());
                if (featureInfo != null && (featureInfo.get("campaignKey").equals(CampaignTypeEnum.ROLLOUT.getValue()) || featureInfo.get("campaignKey").equals(CampaignTypeEnum.PERSONALIZE.getValue()))) {
                    extraData = "feature: " + featureInfo.get("featureKey") + " and rule: " + featureInfo.get("variationName");
                } else {
                    extraData = "feature: " + featureInfo.get("featureKey") + " and rule: " + featureInfo.get("campaignKey") + " and variation: " + featureInfo.get("variationName");
                }
                debugEventProps.put("msg", buildMessage(LoggerService.errorMessages.get("NETWORK_CALL_EXCEPTION"), new HashMap<String, Object>() {{
                    put("extraData", extraData);
                    put("accountId", accountId.toString());
                    put("err", error);
                }}));
            } else if (eventName.equals(EventEnum.VWO_SYNC_VISITOR_PROP.getValue())) {
                debugEventProps.put("an", ApiEnum.SET_ATTRIBUTE.getValue());
                debugEventProps.put("msg", buildMessage(LoggerService.errorMessages.get("NETWORK_CALL_EXCEPTION"), new HashMap<String, Object>() {{
                    put("extraData", ApiEnum.SET_ATTRIBUTE.getValue());
                    put("accountId", accountId.toString());
                    put("err", error);
                }}));
            } else {
                debugEventProps.put("an", ApiEnum.TRACK_EVENT.getValue());
                debugEventProps.put("msg", buildMessage(LoggerService.errorMessages.get("NETWORK_CALL_EXCEPTION"), new HashMap<String, Object>() {{
                    put("extraData", "event: " + eventName);
                    put("accountId", accountId.toString());
                    put("err", error);
                }}));
            }

            return debugEventProps;
            
        } catch (Exception err) {
            Map<String, Object> errorProps = new HashMap<>();
            errorProps.put("cg", DebuggerCategoryEnum.NETWORK.getValue());
            errorProps.put("err", err.toString());
            errorProps.put("msg_t", Constants.NETWORK_CALL_EXCEPTION);
            errorProps.put("lt", LogLevelEnum.ERROR.toString());
            errorProps.put("msg", buildMessage(LoggerService.errorMessages.get("NETWORK_CALL_EXCEPTION"), new HashMap<String, Object>() {{
                put("extraData", "event: " + payload.getD().getEvent().getName());
                put("accountId", accountId.toString());
                put("err", err.toString());
            }}));
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