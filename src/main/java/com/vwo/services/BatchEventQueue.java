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
package com.vwo.services;

import com.vwo.VWOClient;
import com.vwo.constants.Constants;
import com.vwo.models.FlushInterface;
import com.vwo.enums.ApiEnum;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.network_layer.manager.NetworkManager;
import com.vwo.services.LoggerService;
import com.vwo.utils.NetworkUtil;

import java.util.*;
import com.vwo.models.Settings;
import com.vwo.models.request.EventArchPayload;
import com.vwo.services.SettingsManager;

public class BatchEventQueue {
    private Queue<Map<String, Object>> batchQueue = new LinkedList<>();
    private int eventsPerRequest = Constants.DEFAULT_EVENTS_PER_REQUEST;
    private int requestTimeInterval = (int) Constants.DEFAULT_REQUEST_TIME_INTERVAL;
    private Timer timer;
    private boolean isBatchProcessing = false;
    private final int accountId;
    private final String sdkKey;
    private static final Object LockObject = new Object();
    private FlushInterface flushCallback;
    private LoggerService loggerService;
    private SettingsManager settingsManager;

    public BatchEventQueue(int eventsPerRequest, int requestTimeInterval, FlushInterface flushCallback, int accountId, String sdkKey, LoggerService loggerService, SettingsManager settingsManager) {
        this.eventsPerRequest = eventsPerRequest;
        this.requestTimeInterval = requestTimeInterval;
        this.flushCallback = flushCallback;
        this.accountId = accountId;
        this.sdkKey = sdkKey;
        this.loggerService = loggerService;
        this.settingsManager = settingsManager;
        createNewBatchTimer();
        loggerService.log(LogLevelEnum.DEBUG, "BatchEventQueue initialized with eventsPerRequest: " + eventsPerRequest + " and requestTimeInterval: " + requestTimeInterval);
    }

    /**
     * Enqueues an event data into the batch queue.
     * @param eventData The event data to be enqueued.
     */
    public void enqueue(EventArchPayload eventData) {
        synchronized (LockObject) {
            Map<String, Object> payload = VWOClient.objectMapper.convertValue(eventData, Map.class);
            payload = NetworkUtil.removeNullValues(payload);
            batchQueue.add(payload);
            loggerService.log(LogLevelEnum.DEBUG, "Event added to queue. Current queue size: " + batchQueue.size());

            // If batch size reaches the limit, trigger flush
            if (batchQueue.size() >= eventsPerRequest) {
                loggerService.log(LogLevelEnum.DEBUG, "Queue reached max capacity, flushing now...");
                flush(false);
            }
        }
    }

    private void createNewBatchTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                flush(false);
            }
        }, requestTimeInterval * 1000, requestTimeInterval * 1000);

        loggerService.log(LogLevelEnum.DEBUG, "Batch timer initialized with interval: " + requestTimeInterval + " seconds.");
    }

    /**
     * Flush Queue and clear timer.
     *
     * @return Boolean value specifying flush was successful or not.
     */
    public boolean flushAndClearInterval() {
        boolean isSuccess;

        // clear timer
        timer = null;
        isSuccess = flush(true);

        return isSuccess;
    }

    private boolean flush(boolean manual) {
        synchronized (LockObject) {
            if(batchQueue.isEmpty()) {
                loggerService.log(LogLevelEnum.DEBUG, "Queue is empty, skipping flush.");
                return false;
            }
            // Log if flush is manual or automatic
            if (manual) {
                loggerService.log(LogLevelEnum.DEBUG, "Manual flush triggered.");
            }
            // Create a temporary list to hold the events for the batch
            List<Map<String, Object>> eventsToSend = new ArrayList<>(batchQueue);
            batchQueue.clear(); // Clear the queue after taking a snapshot

            // Log before sending batch events
            loggerService.log(LogLevelEnum.DEBUG, "Flushing " + eventsToSend.size() + " events.");

            // Use the shared executor service from NetworkManager instead of creating new one
            // This prevents creating/destroying executors on every flush and provides better resource management
            NetworkManager.getInstance().getExecutorService().execute(() -> {
                try {
                    // Send the batch events and handle the result
                    boolean isSentSuccessfully = sendBatchEvents(eventsToSend);
                    if (isSentSuccessfully) {
                        loggerService.log(LogLevelEnum.INFO,
                                "Batch flush successful. Sent " + eventsToSend.size() + " events.");
                    } else {
                        // Re-enqueue events in case of failure for retry logic
                        synchronized (LockObject) {
                            batchQueue.addAll(eventsToSend);
                        }
                        loggerService.log(LogLevelEnum.ERROR,
                                "BATCH_FLUSH_FAILED", new HashMap<String, Object>() {{
                                    put("an", ApiEnum.FLUSH_EVENTS.getValue());
                                    put("accountId", accountId);
                                }});
                    }
                } catch (Exception ex) {
                    loggerService.log(LogLevelEnum.ERROR, "Error during batch flush: " + ex.getMessage());
                    // Re-enqueue events in case of failure
                    synchronized (LockObject) {
                        batchQueue.addAll(eventsToSend);
                    }
                } finally {
                    // Reset the flag after flush
                    isBatchProcessing = false;
                    // Note: Do NOT shutdown the executor - it's a shared resource
                }
            });
            return true;
        }
    }


    private boolean sendBatchEvents(List<Map<String, Object>> events) {
        try {
            // Call sendPostBatchRequest and capture the return value (success or failure)
            boolean isSentSuccessfully = NetworkUtil.sendPostBatchRequest(settingsManager, events, accountId, sdkKey, this.flushCallback);
            if (this.flushCallback != null) {   
                this.flushCallback.onFlush(null, events.toString());
            }
            return isSentSuccessfully;  // Return whether the request was successful or not
        } catch (Exception ex) {
            if (this.flushCallback != null) {
                this.flushCallback.onFlush(ex.getMessage(), events.toString());
            }
            loggerService.log(LogLevelEnum.ERROR, "Error sending batch to VWO server: " + ex.getMessage());
            return false;  // Return false in case of an error
        }
    }

    public Queue<Map<String, Object>> getBatchQueue() {
        return batchQueue;
    }
}
