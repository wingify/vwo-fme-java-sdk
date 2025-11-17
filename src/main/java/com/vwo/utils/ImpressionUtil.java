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
import com.vwo.models.user.VWOContext;
import com.vwo.ServiceContainer;
import com.vwo.models.request.EventArchPayload;
import com.vwo.constants.Constants;
import java.util.HashMap;

import java.util.Map;

public class ImpressionUtil {

    /**
     * Creates and sends an impression for a variation shown event.
     * This function constructs the necessary properties and payload for the event
     * and uses the NetworkUtil to send a POST API request.
     *
     * @param serviceContainer    The service container containing configuration.
     * @param campaignId  The ID of the campaign.
     * @param variationId The ID of the variation shown to the user.
     * @param context     The user context model containing user-specific data.
     */
    public static void createAndSendImpressionForVariationShown(
            ServiceContainer serviceContainer,
            int campaignId,
            int variationId,
            VWOContext context) {
        // Get base properties for the event
        Map<String, String> properties = NetworkUtil.getEventsBaseProperties(
                serviceContainer.getSettingsManager(),
                EventEnum.VWO_VARIATION_SHOWN.getValue(),
                encodeURIComponent(context.getUserAgent()),
                context.getIpAddress());

        // Construct payload data for tracking the user
        EventArchPayload payload = NetworkUtil.getTrackUserPayloadData(
                serviceContainer,
                context.getId(),
                EventEnum.VWO_VARIATION_SHOWN.getValue(),
                campaignId,
                variationId,
                context.getUserAgent(),
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