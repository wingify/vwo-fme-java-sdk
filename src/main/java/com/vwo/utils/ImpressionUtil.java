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
import com.vwo.models.Settings;
import com.vwo.models.user.VWOContext;
import com.vwo.VWO;

import java.util.Map;

public class ImpressionUtil {

    /**
     * Creates and sends an impression for a variation shown event.
     * This function constructs the necessary properties and payload for the event
     * and uses the NetworkUtil to send a POST API request.
     *
     * @param settings    The settings model containing configuration.
     * @param campaignId  The ID of the campaign.
     * @param variationId The ID of the variation shown to the user.
     * @param context     The user context model containing user-specific data.
     */
    public static void createAndSendImpressionForVariationShown(
            Settings settings,
            int campaignId,
            int variationId,
            VWOContext context) {
        // Get base properties for the event
        Map<String, String> properties = NetworkUtil.getEventsBaseProperties(
                EventEnum.VWO_VARIATION_SHOWN.getValue(),
                encodeURIComponent(context.getUserAgent()),
                context.getIpAddress());

        // Construct payload data for tracking the user
        Map<String, Object> payload = NetworkUtil.getTrackUserPayloadData(
                settings,
                context.getId(),
                EventEnum.VWO_VARIATION_SHOWN.getValue(),
                campaignId,
                variationId,
                context.getUserAgent(),
                context.getIpAddress());

        // Get the instance of VWO
        VWO vwoInstance = VWO.getInstance();

        // Check if batch event queue is available
        if (vwoInstance.getBatchEventQueue() != null) {
            // Enqueue the event to the batch queue for future processing
            vwoInstance.getBatchEventQueue().enqueue(payload);
        } else {
            // Send the event immediately if batch event queue is not available
            NetworkUtil.sendPostApiRequest(properties, payload, context.getUserAgent(), context.getIpAddress());
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