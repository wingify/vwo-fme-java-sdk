/**
 * Copyright 2024 Wingify Software Pvt. Ltd.
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
     * @param attributeKey The key of the attribute to set.
     * @param attributeValue The value of the attribute to set.
     * @param context  The user context model containing user-specific data.
     */
    public static void setAttribute(Settings settings, String attributeKey, String attributeValue, VWOContext context) {
        createAndSendImpressionForSetAttribute(settings, attributeKey, attributeValue, context);
    }

    /**
     * Creates and sends an impression for a track event.
     * This function constructs the necessary properties and payload for the event
     * and uses the NetworkUtil to send a POST API request.
     *
     * @param settings   The settings model containing configuration.
     * @param attributeKey  The key of the attribute to set.
     * @param attributeValue  The value of the attribute to set.
     * @param context    The user context model containing user-specific data.
     */
    private static void createAndSendImpressionForSetAttribute(
            Settings settings,
            String attributeKey,
            String attributeValue,
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
                attributeKey,
                attributeValue
        );

        // Send the constructed properties and payload as a POST request
        NetworkUtil.sendPostApiRequest(properties, payload, context.getUserAgent(), context.getIpAddress());
    }
}
