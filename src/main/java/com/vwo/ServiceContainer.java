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

package com.vwo;

import com.vwo.services.HooksManager;
import com.vwo.services.LoggerService;
import com.vwo.services.SettingsManager;
import com.vwo.models.Settings;
import com.vwo.models.user.VWOInitOptions;
import com.vwo.services.BatchEventQueue;
import com.vwo.packages.segmentation_evaluator.core.SegmentationManager;
import com.vwo.services.DebuggerService;
import com.vwo.utils.UUIDUtils;
import com.vwo.utils.FunctionUtil;

public class ServiceContainer {
    private LoggerService loggerService;
    private SettingsManager settingsManager;
    private HooksManager hooksManager;
    private VWOInitOptions options;
    private BatchEventQueue batchEventQueue;
    private SegmentationManager segmentationManager;
    private Settings settings;
    private DebuggerService debuggerService;
    private String uuid;
    private Long sessionId;

    /**
     * Initializes the ServiceContainer
     * @param loggerService LoggerService instance
     * @param settingsManager SettingsManager instance
     * @param options VWOInitOptions instance
     * @param batchEventQueue BatchEventQueue instance
     * @param settings Settings instance
     */
    public ServiceContainer(String userId, LoggerService loggerService, SettingsManager settingsManager, VWOInitOptions options, BatchEventQueue batchEventQueue, Settings settings) {
        this.loggerService = loggerService;
        this.settingsManager = settingsManager;
        this.hooksManager = new HooksManager(options.getIntegrations());
        this.options = options;
        this.batchEventQueue = batchEventQueue;
        this.segmentationManager = new SegmentationManager(loggerService);
        this.settings = settings;
        this.uuid = UUIDUtils.getUUID(userId, options.getAccountId().toString());
        this.sessionId = FunctionUtil.generateSessionId();
        this.debuggerService = new DebuggerService(uuid, sessionId);
    }

    /**
     * Returns the LoggerService instance
     * @return LoggerService instance
     */
    public LoggerService getLoggerService() {
        return loggerService;
    }

    /**
     * Returns the SettingsManager instance
     * @return SettingsManager instance
     */
    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    /**
     * Returns the HooksManager instance
     * @return HooksManager instance
     */
    public HooksManager getHooksManager() {
        return hooksManager;
    }

    /**
     * Returns the VWOInitOptions instance
     * @return VWOInitOptions instance
     */
    public VWOInitOptions getVWOInitOptions() {
        return options;
    }

    /**
     * Returns the BatchEventQueue instance
     * @return BatchEventQueue instance
     */
    public BatchEventQueue getBatchEventQueue() {
        return batchEventQueue;
    }

    /**
     * Returns the SegmentationManager instance
     * @return SegmentationManager instance
     */
    public SegmentationManager getSegmentationManager() {
        return segmentationManager;
    }

    /**
     * Returns the Settings instance
     * @return Settings instance
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Returns the endpoint with the collection prefix
     * @param endpoint The endpoint to return
     * @return The endpoint with the collection prefix
     */
    public String getEndpointWithCollectionPrefix(String endpoint) {
        if (this.settingsManager.collectionPrefix != null && !this.settingsManager.collectionPrefix.isEmpty()) {
            return "/" + this.settingsManager.collectionPrefix + endpoint;
        }

        return endpoint;
    }

    /**
     * Returns the DebuggerService instance
     * @return DebuggerService instance
     */
    public DebuggerService getDebuggerService() {
        return debuggerService;
    }

    /**
     * Returns the UUID instance
     * @return UUID instance
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Returns the SessionId instance
     * @return SessionId instance
     */
    public Long getSessionId() {
        return sessionId;
    }

    public void setUuid(String userId) {
        this.uuid = UUIDUtils.getUUID(userId, this.options.getAccountId().toString());
        this.debuggerService = new DebuggerService(uuid, sessionId);
    }
}
