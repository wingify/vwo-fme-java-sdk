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

import com.vwo.constants.Constants;

/**
 * Retry configuration class.
 * Used to configure network retry behavior with exponential backoff.
 */
public class RetryConfig {
    private Boolean shouldRetry;
    private Integer maxRetries;
    private Integer initialDelay;
    private Integer backoffMultiplier;

    public RetryConfig() {
        this.shouldRetry = Constants.DEFAULT_SHOULD_RETRY;
        this.maxRetries = Constants.DEFAULT_MAX_RETRIES;
        this.initialDelay = Constants.DEFAULT_INITIAL_DELAY;
        this.backoffMultiplier = Constants.DEFAULT_BACKOFF_MULTIPLIER;
    }

    public RetryConfig(Boolean shouldRetry, Integer maxRetries, Integer initialDelay, Integer backoffMultiplier) {
        this.shouldRetry = shouldRetry;
        this.maxRetries = maxRetries;
        this.initialDelay = initialDelay;
        this.backoffMultiplier = backoffMultiplier;
    }

    // Copy constructor
    public RetryConfig(RetryConfig other) {
        this.shouldRetry = other.shouldRetry;
        this.maxRetries = other.maxRetries;
        this.initialDelay = other.initialDelay;
        this.backoffMultiplier = other.backoffMultiplier;
    }

    public Boolean getShouldRetry() {
        return shouldRetry;
    }

    public void setShouldRetry(Boolean shouldRetry) {
        this.shouldRetry = shouldRetry;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(Integer initialDelay) {
        this.initialDelay = initialDelay;
    }

    public Integer getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public void setBackoffMultiplier(Integer backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }
    @Override
    public String toString() {
        return "RetryConfig{" +
                "shouldRetry=" + shouldRetry +
                ", maxRetries=" + maxRetries +
                ", initialDelay=" + initialDelay +
                ", backoffMultiplier=" + backoffMultiplier +
                '}';
    }
}
