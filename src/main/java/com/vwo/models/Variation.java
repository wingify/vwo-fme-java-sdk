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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Variation {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("key")
    private String key;

    @JsonProperty("name")
    private String name;

    @JsonProperty("weight")
    private double weight;

    @JsonProperty("startRangeVariation")
    private Integer startRangeVariation = 0;

    @JsonProperty("endRangeVariation")
    private Integer endRangeVariation = 0;

    @JsonProperty("variables")
    private List<Variable> variables = new ArrayList<>();

    @JsonProperty("variations")
    private List<Variation> variations = new ArrayList<>();

    @JsonProperty("segments")
    private Map<String, Object> segments = new HashMap<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public List<Variation> getVariations() {
        return variations;
    }

    public void setVariations(List<Variation> variations) {
        this.variations = variations;
    }

    public Map<String, Object> getSegments() {
        return segments;
    }

    public void setSegments(Map<String, Object> segments) {
        this.segments = segments;
    }

}
