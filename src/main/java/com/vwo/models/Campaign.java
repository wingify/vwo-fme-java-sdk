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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Campaign {
    @JsonProperty("isAlwaysCheckSegment")
    private Boolean isAlwaysCheckSegment = false;

    @JsonProperty("isUserListEnabled")
    private Boolean isUserListEnabled = false;
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("segments")
    private Map<String, Object> segments;

    @JsonProperty("ruleKey")
    private String ruleKey;

    @JsonProperty("status")
    private String status;

    @JsonProperty("percentTraffic")
    private Integer percentTraffic;

    @JsonProperty("key")
    private String key;

    @JsonProperty("type")
    private String type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("isForcedVariationEnabled")
    private Boolean isForcedVariationEnabled = false;

    @JsonProperty("variations")
    private List<Variation> variations;
    @JsonProperty("startRangeVariation")
    private Integer startRangeVariation = 0;

    @JsonProperty("endRangeVariation")
    private Integer endRangeVariation = 0;

    @JsonProperty("variables")
    private List<Variable> variables;

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }
    @JsonProperty("weight")
    private double weight;

    @JsonProperty("segments")
    public Map<String, Object> getSegments() {
        return segments;
    }

    @JsonProperty("segments")
    public void setSegments(Map<String, Object> segments) {
        this.segments = segments;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("percentTraffic")
    public Integer getPercentTraffic() {
        return percentTraffic;
    }

    @JsonProperty("percentTraffic")
    public void setPercentTraffic(Integer percentTraffic) {
        this.percentTraffic = percentTraffic;
    }


    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("isForcedVariationEnabled")
    public Boolean getIsForcedVariationEnabled() {
        return isForcedVariationEnabled;
    }

    @JsonProperty("isForcedVariationEnabled")
    public void setIsForcedVariationEnabled(Boolean isForcedVariationEnabled) {
        this.isForcedVariationEnabled = isForcedVariationEnabled;
    }

    @JsonProperty("variations")
    public List<Variation> getVariations() {
        return variations;
    }

    @JsonProperty("variations")
    public void setVariations(List<Variation> variations) {
        this.variations = variations;
    }

    @JsonProperty("variables")
    public List<Variable> getVariables() {
        return variables;
    }

    @JsonProperty("variables")
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public void setRuleKey(String ruleKey) {
        this.ruleKey = ruleKey;
    }

    public Boolean getIsAlwaysCheckSegment() {
        return isAlwaysCheckSegment;
    }

    public void setIsAlwaysCheckSegment(Boolean isAlwaysCheckSegment) {
        this.isAlwaysCheckSegment = isAlwaysCheckSegment;
    }

    public Boolean getIsUserListEnabled() {
        return isUserListEnabled;
    }

    public void setIsUserListEnabled(Boolean userListEnabled) {
        isUserListEnabled = userListEnabled;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Integer getStartRangeVariation() {
        return startRangeVariation;
    }

    public void setStartRangeVariation(Integer startRangeVariation) {
        this.startRangeVariation = startRangeVariation;
    }

    public Integer getEndRangeVariation() {
        return endRangeVariation;
    }

    public void setEndRangeVariation(Integer endRangeVariation) {
        this.endRangeVariation = endRangeVariation;
    }

    public void setModelFromDictionary(Campaign model) {
        if (model.getId() != null) {
            this.id = model.getId();
        }
        if (model.getSegments() != null) {
            this.segments = model.getSegments();
        }
        if (model.getStatus() != null) {
            this.status = model.getStatus();
        }
        if (model.getPercentTraffic() != null) {
            this.percentTraffic = model.getPercentTraffic();
        }
        if (model.getKey() != null) {
            this.key = model.getKey();
        }
        if (model.getType() != null) {
            this.type = model.getType();
        }
        if (model.getName() != null) {
            this.name = model.getName();
        }
        if (model.getIsForcedVariationEnabled() != null) {
            this.isForcedVariationEnabled = model.getIsForcedVariationEnabled();
        }
        if (model.getVariations() != null) {
            this.variations = model.getVariations();
        }
        if (model.getVariables() != null) {
            this.variables = model.getVariables();
        }
        if (model.getRuleKey() != null) {
            this.ruleKey = model.getRuleKey();
        }

        if (model.getIsAlwaysCheckSegment() != null) {
            this.isAlwaysCheckSegment = model.getIsAlwaysCheckSegment();
        }

        if (model.getIsUserListEnabled() != null) {
            this.isUserListEnabled = model.getIsUserListEnabled();
        }

        if (model.getWeight() != 0) {
            this.weight = model.getWeight();
        }

        if (model.getStartRangeVariation() != 0) {
            this.startRangeVariation = model.getStartRangeVariation();
        }

        if (model.getEndRangeVariation() != 0) {
            this.endRangeVariation = model.getEndRangeVariation();
        }
    }
}

