/**
 * Copyright 2024-2026 Wingify Software Pvt. Ltd.
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
package com.wingify.models.user;

import com.wingify.WingifyBuilder;
import com.wingify.interfaces.networking.NetworkClientInterface;
import com.wingify.models.BatchEventData;
import com.wingify.packages.segmentation_evaluator.evaluators.SegmentEvaluator;
import com.wingify.packages.storage.Connector;
import com.wingify.interfaces.integration.IntegrationCallback;

import java.util.HashMap;
import java.util.Map;


public class WingifyInitOptions {
    private String sdkKey = "";
    private Integer accountId = 0;
    private IntegrationCallback integrations;
    private Map<String, Object> logger = new HashMap<>();
    private NetworkClientInterface networkClientInterface;
    private SegmentEvaluator segmentEvaluator;
    private Connector storage;
    private Integer pollInterval;

    private WingifyBuilder wingifyBuilder;
    private BatchEventData batchEventData;

    private Map<String, Object> gatewayService = new HashMap<>();
    private Boolean isUsageStatsDisabled = false;
    private Map<String, Object> _wingify_meta = new HashMap<>();
    private Boolean isAliasingEnabled = false;
    private Boolean isViaVWO = false;
    private RetryConfig retryConfig;
    private Map<String, Object> threadPoolConfig = new HashMap<>();
    private String proxyUrl = "";

    public Map<String, Object> getWingifyMetaData() {
        return _wingify_meta;
    }

    public void setWingifyMetaData(Map<String, Object> wingifyMeta) {
        this._wingify_meta = wingifyMeta;
    }

    public String getSdkKey() {
        return sdkKey;
    }

    public void setSdkKey(String sdkKey) {
        this.sdkKey = sdkKey;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public IntegrationCallback getIntegrations() {
        return integrations;
    }

    public void setIntegrations(IntegrationCallback integrations) {
        this.integrations = integrations;
    }

    public Map<String, Object> getLogger() {
        return logger;
    }

    public void setLogger(Map<String, Object> logger) {
        this.logger = logger;
    }

    public Map<String, Object> getGatewayService() {
        return gatewayService;
    }

    public void setGatewayService(Map<String, Object> gatewayService) {
        this.gatewayService = gatewayService;
    }

    public NetworkClientInterface getNetworkClientInterface() {
        return networkClientInterface;
    }

    public void setNetworkClientInterface(NetworkClientInterface networkClientInterface) {
        this.networkClientInterface = networkClientInterface;
    }

    public SegmentEvaluator getSegmentEvaluator() {
        return segmentEvaluator;
    }

    public void setSegmentEvaluator(SegmentEvaluator segmentEvaluator) {
        this.segmentEvaluator = segmentEvaluator;
    }

    public Connector getStorage() {
        return storage;
    }

    public void setStorage(Connector storage) {
        this.storage = storage;
    }

    public Integer getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Integer pollInterval) {
        this.pollInterval = pollInterval;
    }

    public void setWingifyBuilder(WingifyBuilder wingifyBuilder) {
        this.wingifyBuilder = wingifyBuilder;
    }

    public WingifyBuilder getWingifyBuilder() {
        return wingifyBuilder;
    }

    public BatchEventData getBatchEventData() {
        return batchEventData;
    }

    public void setBatchEventData(BatchEventData batchEventData) {
        this.batchEventData = batchEventData;
    }

    public Boolean getIsUsageStatsDisabled() {
        return isUsageStatsDisabled;
    }

    public void setIsUsageStatsDisabled(Boolean isUsageStatsDisabled) {
        this.isUsageStatsDisabled = isUsageStatsDisabled;
    }

    public Boolean getIsAliasingEnabled() {
        return isAliasingEnabled;
    }

    public void setIsAliasingEnabled(Boolean isAliasingEnabled) {
        this.isAliasingEnabled = isAliasingEnabled;
    }

    public Boolean getIsViaVWO() {
        return isViaVWO;
    }

    public void setIsViaVWO(Boolean isViaVWO) {
        this.isViaVWO = isViaVWO;
    }

    /**
     * Gets the retry configuration.
     * @return The retry configuration.
     */
    public RetryConfig getRetryConfig() {
        return retryConfig;
    }

    /**
     * Sets the retry configuration.
     * @param retryConfig The retry configuration to set.
     */
    public void setRetryConfig(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
    }

    /**
     * Gets the thread pool configuration.
     * Supported keys: "maxPoolSize" (Integer), "queueSize" (Integer)
     * @return The thread pool configuration map.
     */
    public Map<String, Object> getThreadPoolConfig() {
        return threadPoolConfig;
    }

    /**
     * Sets the thread pool configuration.
     * Supported keys: "maxPoolSize" (Integer), "queueSize" (Integer)
     * @param threadPoolConfig The thread pool configuration to set.
     */
    public void setThreadPoolConfig(Map<String, Object> threadPoolConfig) {
        this.threadPoolConfig = threadPoolConfig;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }
}
