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

import java.util.Map;
import java.util.HashMap;

public class DebuggerService {
    /**
     * Map of debug event props for each category
     * key: category name
     * value: map of debug props
     */
    private Map<String, Map<String, Object>> debugEventProps;

    /**
     * Map of standard debug props
     * key: prop name
     * value: prop value
     */
    private Map<String, Object> standardDebugProps;

    /**
     * Constructor for DebuggerService.
     * @param uuid
     * @param sessionId
     */
    public DebuggerService(String uuid, Long sessionId) {
        this.debugEventProps = new HashMap<String, Map<String, Object>>();
        this.standardDebugProps = new HashMap<String, Object>() {
            {
                put("uuid", uuid);
                put("sId", sessionId);
            }
        };
    }

    /**
     * Adds a map of standard debug props. This is used to add props that are common to all categories.
     * @param standardDebugProps Map of standard debug props
     */
    public void addStandardDebugProps(Map<String, Object> standardDebugProps) {
        this.standardDebugProps.putAll(standardDebugProps);
    }

    /**
     * Adds a single standard debug prop.
     * @param key Prop name
     * @param value Prop value
     */
    public void addStandardDebugProp(String key, Object value) {
        this.standardDebugProps.put(key, value);
    }

    /**
     * Returns the standard debug props.
     * @return Map of standard debug props
     */
    public Map<String, Object> getStandardDebugProps() {
        return this.standardDebugProps;
    }

    /**
     * Adds a map of debug props to a specific category.
     * @param category Category name
     * @param eventProps Map of debug props
     */
    public void addCategoryDebugProps(String category, Map<String, Object> eventProps) {
        this.debugEventProps.put(category, eventProps);
    }

    /**
     * Adds a single debug prop to a specific category.
     * @param category Category name
     * @param key Prop name
     * @param value Prop value
     */
    public void addCategoryDebugProp(String category, String key, Object value) {
        Map<String, Object> eventProps = this.debugEventProps.get(category);
        if (eventProps == null) {
            eventProps = new HashMap<String, Object>();
            this.debugEventProps.put(category, eventProps);
        }
        eventProps.put(key, value);
    }

    /**
     * Returns a map of all debug event props for a specific category, with standard props merged into each category.
     * Category-specific props override standard props.
     * @param category Category name
     * @return Map of debug event props for a specific category
     */
    public Map<String, Object> getDebugEventProps(String category) {
        // Copy all categories and merge standard props into each
        Map<String, Object> categoryProps = new HashMap<String, Object>();
        categoryProps.putAll(this.standardDebugProps); // Add standard props first
        categoryProps.putAll(this.debugEventProps.get(category)); // Category-specific props override standard ones
        return categoryProps;
    }

    /**
     * Clears all debug event props and standard props.
     */
    public void clear() {
        this.debugEventProps.clear();
        this.standardDebugProps.clear();
    }
}