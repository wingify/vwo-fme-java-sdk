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

import com.vwo.constants.Constants;
import com.vwo.models.FlushInterface;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.services.LoggerService;
import com.vwo.utils.NetworkUtil;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public BatchEventQueue(int eventsPerRequest, int requestTimeInterval, FlushInterface flushCallback, int accountId, String sdkKey) {
        this.eventsPerRequest = eventsPerRequest;
        this.requestTimeInterval = requestTimeInterval;
        this.flushCallback = flushCallback;
        this.accountId = accountId;
        this.sdkKey = sdkKey;

        createNewBatchTimer();
        LoggerService.log(LogLevelEnum.DEBUG, "BatchEventQueue initialized with eventsPerRequest: " + eventsPerRequest + " and requestTimeInterval: " + requestTimeInterval);
    }

    public void enqueue(Map<String, Object> eventData) {
        synchronized (LockObject) {
            batchQueue.add(eventData);
            LoggerService.log(LogLevelEnum.DEBUG, "Event added to queue. Current queue size: " + batchQueue.size());

            // If batch size reaches the limit, trigger flush
            if (batchQueue.size() >= eventsPerRequest) {
                LoggerService.log(LogLevelEnum.DEBUG, "Queue reached max capacity, flushing now...");
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

        LoggerService.log(LogLevelEnum.DEBUG, "Batch timer initialized with interval: " + requestTimeInterval + " seconds.");
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
                LoggerService.log(LogLevelEnum.DEBUG, "Queue is empty, skipping flush.");
                return false;
            }
            // Log if flush is manual or automatic
            if (manual) {
                LoggerService.log(LogLevelEnum.DEBUG, "Manual flush triggered.");
            }
                // Create a temporary list to hold the events for the batch
                List<Map<String, Object>> eventsToSend = new ArrayList<>(batchQueue);
                batchQueue.clear(); // Clear the queue after taking a snapshot

                // Log before sending batch events
                LoggerService.log(LogLevelEnum.DEBUG, "Flushing " + eventsToSend.size() + " events.");

                // Send the batch events
                // Flag to track success or failure asynchronously
                final boolean[] isSentSuccessfully = { false };

                // Use ExecutorService to handle background task (better than Thread)
                ExecutorService executorService = Executors.newSingleThreadExecutor(); // Use a single-thread pool
                executorService.submit(() -> {
                    try {
                        // Send the batch events and handle the result
                        isSentSuccessfully[0] = sendBatchEvents(eventsToSend);
                        if (isSentSuccessfully[0]) {
                            LoggerService.log(LogLevelEnum.INFO,
                                    "Batch flush successful. Sent " + eventsToSend.size() + " events.");
                        } else {
                            // Re-enqueue events in case of failure for retry logic
                            batchQueue.addAll(eventsToSend);
                            LoggerService.log(LogLevelEnum.ERROR,
                                    "Failed to send batch events. Re-enqueuing events for retry.");
                        }
                    } catch (Exception ex) {
                        LoggerService.log(LogLevelEnum.ERROR, "Error during batch flush: " + ex.getMessage());
                        // Re-enqueue events in case of failure
                        batchQueue.addAll(eventsToSend);
                    } finally {
                        // Reset the flag after flush
                        isBatchProcessing = false;
                        // Shutdown the executor service gracefully
                        executorService.shutdown();
                    }
                });

                return isSentSuccessfully[0];
        }
    }


    private boolean sendBatchEvents(List<Map<String, Object>> events) {
        try {
            // Call sendPostBatchRequest and capture the return value (success or failure)
            boolean isSentSuccessfully = NetworkUtil.sendPostBatchRequest(events, accountId, sdkKey, this.flushCallback);

            return isSentSuccessfully;  // Return whether the request was successful or not
        } catch (Exception ex) {
            LoggerService.log(LogLevelEnum.ERROR, "Error sending batch to VWO server: " + ex.getMessage());
            return false;  // Return false in case of an error
        }
    }

    public Queue<Map<String, Object>> getBatchQueue() {
        return batchQueue;
    }
}
