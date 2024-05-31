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

public class Storage {
    private String featureKey;
    private String user;
    private Integer rolloutId;
    private String rolloutKey;
    private Integer rolloutVariationId;
    private Integer experimentId;
    private String experimentKey;
    private Integer experimentVariationId;

    public String getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getRolloutId() {
        return rolloutId;
    }

    public void setRolloutId(Integer rolloutId) {
        this.rolloutId = rolloutId;
    }

    public String getRolloutKey() {
        return rolloutKey;
    }

    public void setRolloutKey(String rolloutKey) {
        this.rolloutKey = rolloutKey;
    }

    public Integer getRolloutVariationId() {
        return rolloutVariationId;
    }

    public void setRolloutVariationId(Integer rolloutVariationId) {
        this.rolloutVariationId = rolloutVariationId;
    }

    public Integer getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public String getExperimentKey() {
        return experimentKey;
    }

    public void setExperimentKey(String experimentKey) {
        this.experimentKey = experimentKey;
    }

    public Integer getExperimentVariationId() {
        return experimentVariationId;
    }

    public void setExperimentVariationId(Integer experimentVariationId) {
        this.experimentVariationId = experimentVariationId;
    }
}
