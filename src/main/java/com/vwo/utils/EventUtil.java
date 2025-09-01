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

import com.vwo.services.SettingsManager;

import java.util.Map;

/**
 * Utility class for handling SDK events
 */
public class EventUtil {

    /**
     * Sends SDK initialization event
     * This event is triggered when the init function is called.
     * 
     * @param settingsFetchTime Time taken to fetch settings in milliseconds (can be null)
     * @param sdkInitTime Time taken to initialize the SDK in milliseconds (can be null)
     * @param eventName The name of the event to send
     */
    public static void sendSdkInitEvent(SettingsManager settingsManager, Long settingsFetchTime, Long sdkInitTime, String eventName) {
        // Create event properties
        Map<String, String> properties = NetworkUtil.getEventsBaseProperties(
            settingsManager,
            eventName,
            null,
            null
        );

        // Create payload for SDK init event using the new method
        Map<String, Object> payload = NetworkUtil.getSdkInitEventPayload(
            settingsManager,
            eventName,
            settingsFetchTime,
            sdkInitTime
        );

         // Send the event immediately
         NetworkUtil.sendEventDirectlyToDacdn(settingsManager, properties, payload, eventName);
    }
} 