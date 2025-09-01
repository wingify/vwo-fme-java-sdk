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
package com.vwo.models.request.EventArchQueryParams;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RequestQueryParams {

    private String en;
    private String a;
    private String env;
    private Long eTime;
    private double random;
    private String p;
    private String visitor_ua;
    private String visitor_ip;

    public RequestQueryParams (String eventName, String accountId, String sdkKey, String visitorUserAgent, String ipAddress) {
        this.en = eventName;
        this.a = accountId;
        this.env = sdkKey;
        this.eTime = Calendar.getInstance().getTimeInMillis();
        this.random = Math.random();
        this.p = "FS";
        this.visitor_ua = visitorUserAgent;
        this.visitor_ip = ipAddress;
    }

    public Map<String, String> getQueryParams() {
        Map<String, String> path = new HashMap<>();
        path.put("en", this.en);
        path.put("a", this.a);
        path.put("env", this.env);
        path.put("eTime", this.eTime.toString());
        path.put("random", String.valueOf(this.random));
        path.put("p", this.p);
        path.put("visitor_ua", this.visitor_ua);
        path.put("visitor_ip", this.visitor_ip);
        return path;
    }
}
