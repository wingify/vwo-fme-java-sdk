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
package com.vwo.models;

import com.vwo.constants.Constants;

public class BatchEventData {
    private int eventsPerRequest = Constants.DEFAULT_EVENTS_PER_REQUEST; // Default value
    private int requestTimeInterval = (int) Constants.DEFAULT_REQUEST_TIME_INTERVAL; // Default value (in seconds)
    private FlushInterface flushCallback; 

    // Getter for eventsPerRequest
    public int getEventsPerRequest() {
        return eventsPerRequest;
    }

    // Setter for eventsPerRequest
    public void setEventsPerRequest(int eventsPerRequest) {
        this.eventsPerRequest = eventsPerRequest;
    }

    // Getter for requestTimeInterval
    public int getRequestTimeInterval() {
        return requestTimeInterval;
    }

    // Setter for requestTimeInterval
    public void setRequestTimeInterval(int requestTimeInterval) {
        this.requestTimeInterval = requestTimeInterval;
    }

    // Getter for flushCallback
    public FlushInterface getFlushCallback() {
        return flushCallback;
    }

    // Setter for flushCallback
    public void setFlushCallback(FlushInterface flushCallback) {
        this.flushCallback = flushCallback;
    }
}
