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
package com.vwo.models.user;

import com.vwo.VWOBuilder;
import com.vwo.interfaces.networking.NetworkClientInterface;
import com.vwo.packages.segmentation_evaluator.evaluators.SegmentEvaluator;
import com.vwo.packages.storage.Connector;
import com.vwo.interfaces.integration.IntegrationCallback;

import java.util.HashMap;
import java.util.Map;


public class VWOInitOptions {
    private String sdkKey;
    private Integer accountId;
    private IntegrationCallback integrations;
    private Map<String, Object> logger = new HashMap<>();
    private NetworkClientInterface networkClientInterface;
    private SegmentEvaluator segmentEvaluator;
    private Connector storage;
    private Integer pollInterval;

    private VWOBuilder vwoBuilder;

    private Map<String, Object> gatewayService = new HashMap<>();

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

    public void setVwoBuilder(VWOBuilder vwoBuilder) {
        this.vwoBuilder = vwoBuilder;
    }

    public VWOBuilder getVwoBuilder() {
        return vwoBuilder;
    }
}
