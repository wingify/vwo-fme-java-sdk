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
package com.wingify.enums;

public enum EventEnum {
    WINGIFY_VARIATION_SHOWN("vwo_variationShown"),
    WINGIFY_SYNC_VISITOR_PROP("vwo_syncVisitorProp"),
    WINGIFY_SDK_INIT_EVENT("vwo_fmeSdkInit"),
    WINGIFY_ERROR("vwo_log"),
    WINGIFY_DEBUGGER_EVENT("vwo_sdkDebug"),
    WINGIFY_HOLDOUT("vwo_holdout"),
    WINGIFY_USAGE_STATS("vwo_sdkUsageStats");

    private final String value;

    EventEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
