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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Groups {
    @JsonProperty("name")
    private String name;
    @JsonProperty("campaigns")
    private List<String> campaigns;

    // this is where algo, priority, weight go
    @JsonProperty("et")
    private Integer et;
    @JsonProperty("p")
    private List<String> p = new ArrayList<>();
    @JsonProperty("wt")
    private Map<String, Double> wt = new HashMap<>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("campaigns")
    public List<String> getCampaigns() {
        return campaigns;
    }

    @JsonProperty("campaigns")
    public void setCampaigns(List<String> campaigns) {
        this.campaigns = campaigns;
    }

    // getters and setters
    @JsonProperty("et")
    public void setEt(int et) {
        this.et = et;
    }

    @JsonProperty("et")
    public Integer getEt() {
        // set default to random
        et = et == null  || et.toString().isEmpty() ? 1 : et;

        return et;
    }

    @JsonProperty("p")
    public void setP(List<String> p) {
        this.p = p;
    }

    @JsonProperty("p")
    public List<String> getP() {
        return p;
    }

    @JsonProperty("wt")
    public void setWt(Map<String, Double> wt) {
        this.wt = wt;
    }

    @JsonProperty("wt")
    public Map<String, Double> getWt() {
        return wt;
    }
}
