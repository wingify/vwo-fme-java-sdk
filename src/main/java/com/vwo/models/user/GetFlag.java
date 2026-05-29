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
package com.vwo.models.user;

import com.wingify.models.Variable;

import java.util.List;

/**
 * Backward-compatible feature flag response for existing VWO integrations.
 *
 * @deprecated Use {@link com.wingify.models.user.GetFlag} instead.
 */
@Deprecated
public class GetFlag extends com.wingify.models.user.GetFlag {

    public GetFlag() {
        super();
    }

    public GetFlag(Boolean isEnabled, List<Variable> variables, long sessionId, String uuid) {
        super(isEnabled, variables, sessionId, uuid);
    }

    /**
     * Creates a {@code GetFlag} instance from a Wingify feature flag result.
     */
    public GetFlag(com.wingify.models.user.GetFlag getFlag) {
        super(getFlag.isEnabled(), getFlag.getVariablesValue(), getFlag.getSessionId(), getFlag.getUuid());
    }
}
