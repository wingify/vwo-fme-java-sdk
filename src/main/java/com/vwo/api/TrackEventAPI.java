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
import com.vwo.enums.ApiEnum;
import com.vwo.models.Settings;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.services.HooksManager;
import com.vwo.services.LoggerService;
import com.vwo.utils.FunctionUtil;
import com.vwo.utils.NetworkUtil;

import java.util.HashMap;
import java.util.Map;

import static com.vwo.utils.ImpressionUtil.encodeURIComponent;

public class TrackEventAPI {

    /**
     * This method is used to track an event for the user.
     * @param settings The settings model containing configuration.
     * @param eventName The name of the event to track.
     * @param context The user context model containing user-specific data.
     * @param eventProperties event properties for the event
     * @param hooksManager The hooks manager instance.
     * @return Boolean indicating if the event was successfully tracked.
     */
    public static Boolean track(Settings settings, String eventName, VWOContext context, Map<String, ?> eventProperties, HooksManager hooksManager) {
        try {
            if (FunctionUtil.doesEventBelongToAnyFeature(eventName, settings)) {
                createAndSendImpressionForTrack(settings, eventName, context, eventProperties);
                Map<String, Object> objectToReturn = new HashMap<>();
                objectToReturn.put("eventName", eventName);
                objectToReturn.put("api", ApiEnum.TRACK.getValue());
                hooksManager.set(objectToReturn);
                hooksManager.execute(hooksManager.get());
                return true;
            } else {
                LoggerService.log(LogLevelEnum.ERROR, "EVENT_NOT_FOUND", new HashMap<String, String>() {
                    {
                        put("eventName", eventName);
                    }
                });
                return false;
            }
        } catch (Exception e) {
            LoggerService.log(LogLevelEnum.ERROR, "Error in tracking event: " + eventName + " Error: " + e);
            return false;
        }
    }

    /**
     * Creates and sends an impression for a track event.
     * This function constructs the necessary properties and payload for the event
     * and uses the NetworkUtil to send a POST API request.
     *
     * @param settings   The settings model containing configuration.
     * @param eventName  The name of the event to track.
     * @param context    The user context model containing user-specific data.
     * @param eventProperties event properties for the event
     */
    private static void createAndSendImpressionForTrack(
            Settings settings,
            String eventName,
            VWOContext context,
            Map<String, ?> eventProperties
    ) {
        // Get base properties for the event
        Map<String, String> properties = NetworkUtil.getEventsBaseProperties(
                eventName,
                encodeURIComponent(context.getUserAgent()),
                context.getIpAddress()
        );

        // Construct payload data for tracking the user
        Map<String, Object> payload = NetworkUtil.getTrackGoalPayloadData(
                settings,
                context.getId(),
                eventName,
                context,
                eventProperties
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