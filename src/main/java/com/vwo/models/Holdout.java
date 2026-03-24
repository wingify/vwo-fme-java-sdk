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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Holdout {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("segments")
    private Map<String, Object> segments;

    @JsonProperty("percentTraffic")
    private Integer percentTraffic;

    @JsonProperty("isGlobal")
    private Boolean isGlobal;

    @JsonProperty("featureIds")
    private List<Integer> featureIds;

    @JsonProperty("metrics")
    @JsonAlias("m")
    private List<Metric> metrics;

    @JsonProperty("isGatewayServiceRequired")
    private Boolean isGatewayServiceRequired = false;

    @JsonProperty("name")
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Map<String, Object> getSegments() {
        return segments;
    }

    public void setSegments(Map<String, Object> segments) {
        this.segments = segments;
    }

    public Integer getPercentTraffic() {
        return percentTraffic;
    }

    public void setPercentTraffic(Integer percentTraffic) {
        this.percentTraffic = percentTraffic;
    }

    public Boolean getIsGlobal() {
        return isGlobal;
    }

    public void setIsGlobal(Boolean global) {
        isGlobal = global;
    }

    public List<Integer> getFeatureIds() {
        return featureIds;
    }

    public void setFeatureIds(List<Integer> featureIds) {
        this.featureIds = featureIds;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    public Boolean getIsGatewayServiceRequired() {
        return isGatewayServiceRequired;
    }

    public void setIsGatewayServiceRequired(Boolean gatewayServiceRequired) {
        isGatewayServiceRequired = gatewayServiceRequired;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
