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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Feature {
    @JsonProperty("key")
    private String key;
    @JsonProperty("metrics")
    private List<Metric> metrics;
    @JsonProperty("status")
    private String status;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("rules")
    private List<Rule> rules;
    @JsonProperty("impactCampaign")
    private ImpactCampaign impactCampaign = new ImpactCampaign();
    @JsonProperty("name")
    private String name;
    @JsonProperty("type")
    private String type;
    @JsonProperty("rulesLinkedCampaign")
    private List<Campaign> rulesLinkedCampaign = new ArrayList<>();
    @JsonProperty("isGatewayServiceRequired")
    private Boolean isGatewayServiceRequired = false;
    @JsonProperty("variables")
    private List<Variable> variables;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public ImpactCampaign getImpactCampaign() {
        return impactCampaign;
    }

    public void setImpactCampaign(ImpactCampaign impactCampaign) {
        this.impactCampaign = impactCampaign;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRulesLinkedCampaign(List<Campaign> rulesLinkedCampaign) {
        this.rulesLinkedCampaign = rulesLinkedCampaign;
    }

    public List<Campaign> getRulesLinkedCampaign() {
        return rulesLinkedCampaign;
    }

    public Boolean getIsGatewayServiceRequired() {
        return isGatewayServiceRequired;
    }

    public void setIsGatewayServiceRequired(Boolean gatewayServiceRequired) {
        isGatewayServiceRequired = gatewayServiceRequired;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }
}
