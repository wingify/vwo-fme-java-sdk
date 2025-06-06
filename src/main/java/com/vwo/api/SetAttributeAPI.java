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
package com.vwo.api;

import com.vwo.VWO;
import com.vwo.enums.EventEnum;
import com.vwo.models.Settings;
import com.vwo.models.user.VWOContext;
import com.vwo.utils.NetworkUtil;

import java.util.Map;

import static com.vwo.utils.ImpressionUtil.encodeURIComponent;

public class SetAttributeAPI {
    /**
     * This method is used to set an attribute for the user.
     * @param settings The settings model containing configuration.
     * @param attributeMap - Map of attribute key and value to be set
     * @param context  The user context model containing user-specific data.
     */
    public static void setAttribute(Settings settings, Map<String, Object> attributeMap, VWOContext context) {
        createAndSendImpressionForSetAttribute(settings, attributeMap, context);
    }

    /**
     * Creates and sends an impression for a track event.
     * This function constructs the necessary properties and payload for the event
     * and uses the NetworkUtil to send a POST API request.
     *
     * @param settings   The settings model containing configuration.
     * @param attributeMap - Map of attribute key and value to be set
     * @param context    The user context model containing user-specific data.
     */
    private static void createAndSendImpressionForSetAttribute(
            Settings settings,
            Map<String, Object> attributeMap,
            VWOContext context
    ) {
        // Get base properties for the event
        Map<String, String> properties = NetworkUtil.getEventsBaseProperties(
                settings,
                EventEnum.VWO_SYNC_VISITOR_PROP.getValue(),
                encodeURIComponent(context.getUserAgent()),
                context.getIpAddress()
        );

        // Construct payload data for tracking the user
        Map<String, Object> payload = NetworkUtil.getAttributePayloadData(
                settings,
                context.getId(),
                EventEnum.VWO_SYNC_VISITOR_PROP.getValue(),
                attributeMap
        );

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

}
