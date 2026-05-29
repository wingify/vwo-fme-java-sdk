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

package com.wingify.models.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class Props {
  @JsonProperty("vwo_sdkName")
  private String wingifySdkName;
  @JsonProperty("vwo_sdkVersion")
  private String wingifySdkVersion;
  @JsonProperty("vwo_envKey")
  private String wingifyEnvKey;
  private String variation;
  private Integer id;
  @JsonProperty("isFirst")
  private Integer isFirst;
  @JsonProperty("isCustomEvent")
  private Boolean isCustomEvent;
  @JsonProperty("vwoMeta")
  private Map<String, Object> wingifyMeta;
  @JsonProperty("product")
  private String product;
  @JsonProperty("data")
  private Map<String, Object> data;
  @JsonProperty("fId")
  private Integer fId;

  @JsonIgnore
  private Map<String,Object> additionalProperties = new HashMap<String,Object>();

  @JsonProperty("vwo_sdkName")
  public void setSdkName(String sdkName) {
    this.wingifySdkName = sdkName;
  }

  @JsonProperty("vwo_sdkVersion")
  public void setSdkVersion(String sdkVersion) {
    this.wingifySdkVersion = sdkVersion;
  }

  public String getVariation() {
    return variation;
  }

  public void setVariation(String variation) {
    this.variation = variation;
  }

  public void setIsFirst(Integer isFirst) {
    this.isFirst = isFirst;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public void setIsCustomEvent(Boolean isCustomEvent) {
    this.isCustomEvent = isCustomEvent;
  }

  @JsonProperty("vwo_envKey")
  public void setEnvKey(String envKey) {
    this.wingifyEnvKey = envKey;
  }

  @JsonAnyGetter
  public Map<String, ?> getAdditionalProperties() {
    return additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperties(Map<String, Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  public Map<String, Object> getWingifyMeta() {
    return wingifyMeta;
  }

  public void setWingifyMeta(Map<String, Object> wingifyMeta) {
    this.wingifyMeta = wingifyMeta;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public void setfId(Integer fId) {
    this.fId = fId;
  }
}
