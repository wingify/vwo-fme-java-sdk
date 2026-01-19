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

import com.vwo.VWOClient;
import com.vwo.enums.EventEnum;
import com.vwo.models.user.VWOContext;
import com.vwo.ServiceContainer;
import com.vwo.models.request.EventArchPayload;
import com.vwo.constants.Constants;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.network_layer.manager.NetworkManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImpressionUtil {

    /**
     * Sends an impression for a variation shown event.
     * This function constructs the necessary properties for the event
     * and uses the NetworkUtil to send a POST API request.
     *
     * @param serviceContainer    The service container containing configuration.
     * @param campaignId  The ID of the campaign.
     * @param variationId The ID of the variation shown to the user.
     * @param context     The user context model containing user-specific data.
     * @param payload     The payload data for tracking the user (created by caller).
     */
    public static void sendImpressionForVariationShown(
            ServiceContainer serviceContainer,
            int campaignId,
            int variationId,
            VWOContext context,
            EventArchPayload payload) {
        // Get base properties for the event
        Map<String, String> properties = NetworkUtil.getEventsBaseProperties(
                serviceContainer.getSettingsManager(),
                EventEnum.VWO_VARIATION_SHOWN.getValue(),
                encodeURIComponent(context.getUserAgent()),
                context.getIpAddress());

        String campaignKeyWithFeatureName = CampaignUtil.getCampaignKeyFromCampaignId(serviceContainer.getSettings(), campaignId);
        String variationName = CampaignUtil.getVariationNameFromCampaignIdAndVariationId(serviceContainer.getSettings(), campaignId, variationId);
        String campaignType = CampaignUtil.getCampaignTypeFromCampaignId(serviceContainer.getSettings(), campaignId);

        String featureKey = (String) serviceContainer.getDebuggerService().getStandardDebugProps().get("fk");
        String campaignKey = "";
        if (featureKey.equals(campaignKeyWithFeatureName)) {
            campaignKey = Constants.IMPACT_ANALYSIS;
        } else {
            // split campaignKeyWithFeatureName with featureKey_ to get the campaignKey
            campaignKey = campaignKeyWithFeatureName.split(featureKey + "_")[1];
        }

        Map<String, Object> featureInfo = new HashMap<>();
        featureInfo.put("featureKey", featureKey);
        featureInfo.put("campaignKey", campaignKey);
        featureInfo.put("campaignType", campaignType);
        featureInfo.put("variationName", variationName);

        // Check if batch event queue is available
        if (serviceContainer.getBatchEventQueue() != null) {
            // Enqueue the event to the batch queue for future processing
            serviceContainer.getBatchEventQueue().enqueue(payload);
        } else {
            // Send the event immediately if batch event queue is not available
            NetworkUtil.sendPostApiRequest(serviceContainer, properties, payload, context, featureInfo);
        }
    }

    /**
     * Sends impressions for variation shown events in batch.
     * This function dispatches all collected payloads in a single network call.
     *
     * @param payloads         List of EventArchPayload objects to send.
     * @param serviceContainer The service container containing configuration.
     */
    public static void sendImpressionForVariationShownInBatch(
            List<EventArchPayload> payloads,
            ServiceContainer serviceContainer) {
        if (payloads == null || payloads.isEmpty()) {
            return;
        }

        // Check if batch event queue is available
        if (serviceContainer.getBatchEventQueue() != null) {
            // Enqueue each payload to the batch queue for future processing
            for (EventArchPayload payload : payloads) {
                serviceContainer.getBatchEventQueue().enqueue(payload);
            }
        } else {
            // Convert payloads to list of maps for batch request
            List<Map<String, Object>> payloadMaps = new ArrayList<>();
            for (EventArchPayload payload : payloads) {
                Map<String, Object> payloadMap = VWOClient.objectMapper.convertValue(payload, Map.class);
                payloadMap = NetworkUtil.removeNullValues(payloadMap);
                payloadMaps.add(payloadMap);
            }
            
            final int eventCount = payloadMaps.size();
            // Send all events in a single batch request asynchronously
            NetworkManager.getInstance().getExecutorService().submit(() -> {
                try {
                    NetworkUtil.sendPostBatchRequest(
                            serviceContainer.getSettingsManager(),
                            payloadMaps,
                            serviceContainer.getSettingsManager().accountId,
                            serviceContainer.getSettingsManager().sdkKey,
                            null
                    );
                    serviceContainer.getLoggerService().log(LogLevelEnum.DEBUG, "BATCH_IMPRESSION_SUCCESS", new HashMap<String, Object>() {
                        {
                            put("eventCount", String.valueOf(eventCount));
                            put("accountId", serviceContainer.getSettingsManager().accountId.toString());
                        }
                    });
                } catch (Exception e) {
                    serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "BATCH_IMPRESSION_FAILED", new HashMap<String, Object>() {
                        {
                            put("err", e.getMessage());
                        }
                    });
                }
            });
        }
    }

    /**
     * Encodes the query parameters to ensure they are URL-safe
     * 
     * @param value The query parameters to encode
     * @return The encoded query parameters
     */
    public static String encodeURIComponent(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}