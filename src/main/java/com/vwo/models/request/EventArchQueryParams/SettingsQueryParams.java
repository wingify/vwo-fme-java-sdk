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
package com.vwo.models.request.EventArchQueryParams;

import java.util.HashMap;
import java.util.Map;

public class SettingsQueryParams {
    private String i;
    private String r;
    private String a;

    public SettingsQueryParams(String i, String r, String a) {
        this.i = i;
        this.r = r;
        this.a = a;
    }

    public Map<String, String> getQueryParams() {
        Map<String, String> path = new HashMap<>();
        path.put("i", this.i);
        path.put("r", this.r);
        path.put("a", this.a);
        return path;
    }
}
