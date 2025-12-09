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
package com.vwo.models;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vwo.constants.Constants;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Settings {
    @JsonProperty("features")
    @JsonDeserialize(using = EmptyObjectAsEmptyListDeserializer.class)
    private List<Feature> features;
    @JsonProperty("accountId")
    private Integer accountId;
    @JsonProperty("usageStatsAccountId")
    private Integer usageStatsAccountId;
    @JsonProperty("groups")
    private Map<String, Groups> groups;
    @JsonProperty("campaignGroups")
    private Map<String, Integer> campaignGroups;
    @JsonProperty("isNBv2")
    private Boolean isNBv2 = false;
    @JsonProperty("campaigns")
    @JsonDeserialize(using = EmptyObjectAsEmptyListDeserializer.class)
    private List<Campaign> campaigns;
    @JsonProperty("isNB")
    private Boolean isNB = false;
    @JsonProperty("sdkKey")
    private String sdkKey;
    @JsonProperty("sdkMetaInfo")
    private Map<String, Object> sdkMetaInfo;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("collectionPrefix")
    private String collectionPrefix;
    @JsonProperty("pollInterval")
    private int pollInterval = Constants.DEFAULT_POLL_INTERVAL;

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getUsageStatsAccountId() {
        return usageStatsAccountId;
    }

    public void setUsageStatsAccountId(Integer usageStatsAccountId) {
        this.usageStatsAccountId = usageStatsAccountId;
    }

    public Map<String, Groups> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Groups> groups) {
        this.groups = groups;
    }

    public Map<String, Integer> getCampaignGroups() {
        return campaignGroups;
    }

    public void setCampaignGroups(Map<String, Integer> campaignGroups) {
        this.campaignGroups = campaignGroups;
    }

    public Boolean isNBv2() {
        return isNBv2;
    }

    public void setNBv2(Boolean NBv2) {
        isNBv2 = NBv2;
    }

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    public Boolean isNB() {
        return isNB;
    }

    public void setNB(Boolean NB) {
        isNB = NB;
    }

    public String getSdkKey() {
        return sdkKey;
    }

    public void setSdkKey(String sdkKey) {
        this.sdkKey = sdkKey;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCollectionPrefix() {
        return collectionPrefix;
    }

    public void setCollectionPrefix(String collectionPrefix) {
        this.collectionPrefix = collectionPrefix;
    }

    public void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public Map<String, Object> getSdkMetaInfo() {
        return sdkMetaInfo;
    }

    public void setSdkMetaInfo(Map<String, Object> sdkMetaInfo) {
        this.sdkMetaInfo = sdkMetaInfo;
    }
}
