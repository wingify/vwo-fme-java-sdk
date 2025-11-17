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
package com.vwo.utils;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.vwo.enums.CampaignTypeEnum;
import com.vwo.models.*;

public class FunctionUtil {

    /**
     * Clones an object using JSON serialization and deserialization.
     * @param obj  The object to clone.
     * @return   The cloned object.
     */
    public static Object cloneObject(Object obj) {
        if (obj == null) {
            return null;
        }
        // Use JSON serialization and deserialization to perform a deep clone
        return new Gson().fromJson(new Gson().toJson(obj), obj.getClass());
    }

    /**
     * Generates a session ID.
     * @return  The session ID.
     */
    public static long generateSessionId() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Retrieves the current Unix timestamp in milliseconds.
     * @return  The current Unix timestamp in milliseconds.
     */
    public static long getCurrentUnixTimestampInMillis() {
        // Return the current Unix timestamp in milliseconds
        return System.currentTimeMillis();
    }

    /**
     * Retrieves a random number between 0 and 1.
     * @return  A random number between 0 and 1.
     */
    public static double getRandomNumber() {
        // Use Math.random() to generate a random number between 0 and 1
        return Math.random();
    }

    /**
     * Retrieves specific rules based on the type from a feature.
     * @param feature The feature model.
     * @param type The type of the rules to retrieve.
     * @return A list of rules that match the type.
     */
    public static List<Campaign> getSpecificRulesBasedOnType(Feature feature, CampaignTypeEnum type) {
        if (feature == null || feature.getRulesLinkedCampaign() == null) {
            return Collections.emptyList();
        }
        if (type != null) {
            return feature.getRulesLinkedCampaign().stream()
                    .filter(rule -> Objects.equals(rule.getType(), type.getValue()))
                    .collect(Collectors.toList());
        }
        return feature.getRulesLinkedCampaign();
    }

    /**
     * Retrieves all AB and Personalize rules from a feature.
     * @param feature The feature model.
     * @return A list of AB and Personalize rules.
     */
    public static List<Campaign> getAllExperimentRules(Feature feature) {
        if (feature == null || feature.getRulesLinkedCampaign() == null) {
            return Collections.emptyList();
        }
        return feature.getRulesLinkedCampaign().stream()
                .filter(rule -> Objects.equals(rule.getType(), CampaignTypeEnum.AB.getValue()) || Objects.equals(rule.getType(), CampaignTypeEnum.PERSONALIZE.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a feature by its key from the settings.
     * @param settings The settings model.
     * @param featureKey The key of the feature to find.
     * @return The feature if found, otherwise null.
     */
    public static Feature getFeatureFromKey(Settings settings, String featureKey) {
        if (settings == null || settings.getFeatures() == null) {
            return null;
        }
        return settings.getFeatures().stream()
                .filter(feature -> feature.getKey().equals(featureKey))
                .findFirst()
                .orElse(null);
    }

    public static boolean doesEventBelongToAnyFeature(String eventName, Settings settings) {
        return settings.getFeatures().stream()
                .anyMatch(feature -> feature.getMetrics().stream()
                        .anyMatch(metric -> metric.getIdentifier().equals(eventName)));
    }
}
