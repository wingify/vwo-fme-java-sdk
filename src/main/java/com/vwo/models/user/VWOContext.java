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
package com.vwo.models.user;

import java.util.HashMap;
import java.util.Map;

public class VWOContext {
    private String id;
    private String userAgent = "";
    private String ipAddress = "";
    private Map<String, ?> customVariables = new HashMap<>();

    private Map<String, ?> variationTargetingVariables = new HashMap<>();

    private GatewayService _vwo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Map<String, ?> getCustomVariables() {
        return customVariables;
    }

    public void setCustomVariables(Map<String, ?> customVariables) {
        this.customVariables = customVariables;
    }

    public Map<String, ?> getVariationTargetingVariables() {
        return variationTargetingVariables;
    }

    public void setVariationTargetingVariables(Map<String, ?> variationTargetingVariables) {
        this.variationTargetingVariables = variationTargetingVariables;
    }

    public GatewayService getVwo() {
        return _vwo;
    }

    public void setVwo(GatewayService _vwo) {
        this._vwo = _vwo;
    }
}
