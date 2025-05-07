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
package com.vwo.services;
import com.vwo.interfaces.integration.IntegrationCallback;

import java.util.Map;

public class HooksManager {
    private IntegrationCallback callback;
    private Map<String, Object> decision;

    public HooksManager(IntegrationCallback callback) {
        this.callback = callback;
    }

    /**
     * Executes the callback
     *
     * @param properties Properties from the callback
     */
    public void execute(Map<String, Object> properties) {
        if (this.callback != null) {
            this.callback.execute(properties);
        }
    }

    /**
     * Sets properties to the decision object
     *
     * @param properties Properties to set
     */
    public void set(Map<String, Object> properties) {
        if (this.callback != null) {
            this.decision = properties;
        }
    }

    /**
     * Retrieves the decision object
     *
     * @return The decision object
     */
    public Map<String, Object> get() {
        return this.decision;
    }
}
