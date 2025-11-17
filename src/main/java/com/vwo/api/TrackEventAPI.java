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

import com.vwo.enums.ApiEnum;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.ServiceContainer;
import com.vwo.utils.FunctionUtil;
import com.vwo.utils.NetworkUtil;
import com.vwo.models.request.EventArchPayload;

import java.util.HashMap;
import java.util.Map;

import static com.vwo.utils.ImpressionUtil.encodeURIComponent;

public class TrackEventAPI {

    /**
     * This method is used to track an event for the user.
     * @param eventName The name of the event to track.
     * @param context The user context model containing user-specific data.
     * @param eventProperties event properties for the event
     * @param serviceContainer The service container instance.
     * @return Boolean indicating if the event was successfully tracked.
     */
    public static Boolean track(String eventName, VWOContext context, Map<String, ?> eventProperties, ServiceContainer serviceContainer) {
        serviceContainer.getDebuggerService().addStandardDebugProp("an", ApiEnum.TRACK_EVENT.getValue());
        try {
            if (FunctionUtil.doesEventBelongToAnyFeature(eventName, serviceContainer.getSettings())) {
                createAndSendImpressionForTrack(eventName, context, eventProperties, serviceContainer);
                Map<String, Object> objectToReturn = new HashMap<>();
                objectToReturn.put("eventName", eventName);
                objectToReturn.put("api", ApiEnum.TRACK_EVENT.getValue());
                serviceContainer.getHooksManager().set(objectToReturn);
                serviceContainer.getHooksManager().execute(serviceContainer.getHooksManager().get());
                return true;
            } else {
                serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "EVENT_NOT_FOUND", new HashMap<String, Object>() {
                    {
                        put("eventName", eventName);
                        putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                    }
                });
                return false;
            }
        } catch (Exception e) {
            serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "EXECUTION_FAILED", new HashMap<String, Object>() {
                {
                    put("err", e.getMessage());
                    putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                }
            });
            return false;
        }
    }

    /**
     * Creates and sends an impression for a track event.
     * This function constructs the necessary properties and payload for the event
     * and uses the NetworkUtil to send a POST API request.
     * @param eventName  The name of the event to track.
     * @param context    The user context model containing user-specific data.
     * @param eventProperties event properties for the event
     * @param serviceContainer The service container instance.
     * @return void
     */
    private static void createAndSendImpressionForTrack(
            String eventName,
            VWOContext context,
            Map<String, ?> eventProperties,
            ServiceContainer serviceContainer
    ) {
        // Get base properties for the event
        Map<String, String> properties = NetworkUtil.getEventsBaseProperties(
                serviceContainer.getSettingsManager(),
                eventName,
                encodeURIComponent(context.getUserAgent()),
                context.getIpAddress()
        );

        // Construct payload data for tracking the user
        EventArchPayload payload = NetworkUtil.getTrackGoalPayloadData(
                serviceContainer,
                context.getId(),
                eventName,
                context,
                eventProperties
        );

        // Check if batch event queue is available
        if (serviceContainer.getBatchEventQueue() != null) {
            // Enqueue the event to the batch queue for future processing
            serviceContainer.getBatchEventQueue().enqueue(payload);
        } else {
            // Send the event immediately if batch event queue is not available
            NetworkUtil.sendPostApiRequest(serviceContainer, properties, payload, context, null);
        }
    }
}