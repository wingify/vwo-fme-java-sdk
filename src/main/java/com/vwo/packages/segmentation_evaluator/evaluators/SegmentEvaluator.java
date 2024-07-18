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
package com.vwo.packages.segmentation_evaluator.evaluators;

import com.fasterxml.jackson.databind.JsonNode;
import com.vwo.VWOClient;
import com.vwo.decorators.StorageDecorator;
import com.vwo.models.Feature;
import com.vwo.models.Settings;
import com.vwo.models.Storage;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.segmentation_evaluator.enums.SegmentOperatorValueEnum;
import com.vwo.services.LoggerService;
import com.vwo.services.StorageService;

import java.util.*;
import static com.vwo.packages.segmentation_evaluator.utils.SegmentUtil.*;

public class SegmentEvaluator {
    public VWOContext context;
    public Settings settings;
    public Feature feature;

    /**
     * Validates if the segmentation defined in the DSL is applicable based on the provided properties.
     * @param dsl The domain-specific language defining the segmentation rules.
     * @param properties The properties against which the DSL rules are evaluated.
     * @return A boolean indicating if the segmentation is valid.
     */
    public boolean isSegmentationValid(JsonNode dsl, Map<String, Object> properties) {
        Map.Entry<String, JsonNode> entry = getKeyValue(dsl);
        String operator = entry.getKey();
        JsonNode subDsl = entry.getValue();

        // Evaluate based on the type of segmentation operator
        SegmentOperatorValueEnum operatorEnum = SegmentOperatorValueEnum.fromValue(operator);

        switch (operatorEnum) {
            case NOT:
                boolean result = isSegmentationValid(subDsl, properties);
                return !result;
            case AND:
                return every(subDsl, properties);
            case OR:
                return some(subDsl, properties);
            case CUSTOM_VARIABLE:
                return new SegmentOperandEvaluator().evaluateCustomVariableDSL(subDsl, properties);
            case USER:
                return new SegmentOperandEvaluator().evaluateUserDSL(subDsl.toString(), properties);
            case UA:
                return new SegmentOperandEvaluator().evaluateUserAgentDSL(subDsl.toString(), context);
            default:
                return false;
        }
    }

    /**
     * Evaluates if any of the DSL nodes are valid using the OR logic.
     * @param dslNodes Array of DSL nodes to evaluate.
     * @param customVariables Custom variables provided for evaluation.
     * @return A boolean indicating if any of the nodes are valid.
     */
    public boolean some(JsonNode dslNodes, Map<String, Object> customVariables) {
        Map<String, List<String>> uaParserMap = new HashMap<>();
        int keyCount = 0; // Initialize count of keys encountered
        boolean isUaParser = false;

        for (JsonNode dsl : dslNodes) {
            Iterator<String> fieldNames = dsl.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                // Check for user agent related keys
                SegmentOperatorValueEnum keyEnum = SegmentOperatorValueEnum.fromValue(key);
                if (keyEnum == SegmentOperatorValueEnum.OPERATING_SYSTEM ||
                        keyEnum == SegmentOperatorValueEnum.BROWSER_AGENT ||
                        keyEnum == SegmentOperatorValueEnum.DEVICE_TYPE ||
                        keyEnum == SegmentOperatorValueEnum.DEVICE ) {
                    isUaParser = true;
                    JsonNode value = dsl.get(key);

                    if (!uaParserMap.containsKey(key)) {
                        uaParserMap.put(key, new ArrayList<>());
                    }

                    // Ensure value is treated as an array of strings
                    if (value.isArray()) {
                        for (JsonNode val : value) {
                            if (val.isTextual()) {
                                uaParserMap.get(key).add(val.asText());
                            }
                        }
                    } else if (value.isTextual()) {
                        uaParserMap.get(key).add(value.asText());
                    }

                    keyCount++; // Increment count of keys encountered
                }

                // Check for feature toggle based on feature ID
                if (keyEnum == SegmentOperatorValueEnum.FEATURE_ID) {
                    JsonNode featureIdObject = dsl.get(key);
                    Iterator<String> featureIdKeys = featureIdObject.fieldNames();
                    if (featureIdKeys.hasNext()) {
                        String featureIdKey = featureIdKeys.next();
                        String featureIdValue = featureIdObject.get(featureIdKey).asText();

                        if (featureIdValue.equals("on") || featureIdValue.equals("off")) {
                            List<Feature> features = settings.getFeatures();
                            Feature feature = features.stream()
                                    .filter(f -> f.getId() == Integer.parseInt(featureIdKey))
                                    .findFirst()
                                    .orElse(null);

                            if (feature != null) {
                                String featureKey = feature.getKey();
                                boolean result = checkInUserStorage(settings, featureKey, context);
                                if (featureIdValue.equals("off")) {
                                    return !result;
                                }
                                return result;
                            } else {
                                LoggerService.log(LogLevelEnum.DEBUG, "Feature not found with featureIdKey: " + featureIdKey);
                                return false; // Handle the case when feature is not found
                            }
                        }
                    }
                }
            }

            // Check if the count of keys encountered is equal to dslNodes.size()
            if (isUaParser && keyCount == dslNodes.size()) {
                try {
                    boolean uaParserResult = checkUserAgentParser(uaParserMap);
                    return uaParserResult;
                } catch (Exception err) {
                    LoggerService.log(LogLevelEnum.ERROR, "Failed to validate User Agent. Error: " + err);
                }
            }

            // Recursively check each DSL node
            if (isSegmentationValid(dsl, customVariables)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluates all DSL nodes using the AND logic.
     * @param dslNodes Array of DSL nodes to evaluate.
     * @param customVariables Custom variables provided for evaluation.
     * @return A boolean indicating if all nodes are valid.
     */
    public boolean every(JsonNode dslNodes, Map<String, Object> customVariables) {
        Map<String, Object> locationMap = new HashMap<>();
        for (JsonNode dsl : dslNodes) {
            Iterator<String> fieldNames = dsl.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                // Check if the DSL node contains location-related keys
                SegmentOperatorValueEnum keyEnum = SegmentOperatorValueEnum.fromValue(key);
                if (keyEnum == SegmentOperatorValueEnum.COUNTRY ||
                        keyEnum == SegmentOperatorValueEnum.REGION ||
                        keyEnum == SegmentOperatorValueEnum.CITY) {
                    addLocationValuesToMap(dsl, locationMap);
                    // Check if the number of location keys matches the number of DSL nodes
                    if (locationMap.size() == dslNodes.size()) {
                        return checkLocationPreSegmentation(locationMap);
                    }
                    continue;
                }
                boolean res = isSegmentationValid(dsl, customVariables);
                if (!res) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Adds location values from a DSL node to a map.
     * @param dsl DSL node containing location data.
     * @param locationMap Map to store location data.
     */
    public void addLocationValuesToMap(JsonNode dsl, Map<String, Object> locationMap) {
        // Add country, region, and city information to the location map if present
        SegmentOperatorValueEnum keyEnum = SegmentOperatorValueEnum.fromValue(dsl.fieldNames().next());
        if (keyEnum == SegmentOperatorValueEnum.COUNTRY) {
            locationMap.put(keyEnum.getValue(), dsl.get(keyEnum.getValue()).asText());
        }
        if (keyEnum == SegmentOperatorValueEnum.REGION) {
            locationMap.put(keyEnum.getValue(), dsl.get(keyEnum.getValue()).asText());
        }
        if (keyEnum == SegmentOperatorValueEnum.CITY) {
            locationMap.put(keyEnum.getValue(), dsl.get(keyEnum.getValue()).asText());
        }
    }

    /**
     * Checks if the user's location matches the expected location criteria.
     * @param locationMap Map of expected location values.
     * @return A boolean indicating if the location matches.
     */
    public boolean checkLocationPreSegmentation(Map<String, Object> locationMap) {
        // Ensure user's IP address is available
        if (context == null || context.getIpAddress() == null || context.getIpAddress().isEmpty()) {
            LoggerService.log(LogLevelEnum.INFO, "To evaluate location pre Segment, please pass ipAddress in context object");
            return false;
        }
        // Check if location data is available and matches the expected values
        if (context.getVwo() == null || context.getVwo().getLocation() == null || context.getVwo().getLocation().isEmpty()) {
            return false;
        }
        return valuesMatch(locationMap, context.getVwo().getLocation());
    }

    /**
     * Checks if the user's device information matches the expected criteria.
     * @param uaParserMap Map of expected user agent values.
     * @return A boolean indicating if the user agent matches.
     */
    public boolean checkUserAgentParser(Map<String, List<String>> uaParserMap) {
        // Ensure user's user agent is available
        if (context == null || context.getUserAgent() == null || context.getUserAgent().isEmpty()) {
            LoggerService.log(LogLevelEnum.INFO, "To evaluate user agent related segments, please pass userAgent in context object");
            return false;
        }
        // Check if user agent data is available and matches the expected values
        if (context.getVwo() == null || context.getVwo().getUserAgent() == null || context.getVwo().getUserAgent().isEmpty()) {
            return false;
        }

        return checkValuePresent(uaParserMap, context.getVwo().getUserAgent());
    }

    /**
     * Checks if the feature is enabled for the user by querying the storage.
     * @param settings The settings model containing configuration.
     * @param featureKey The key of the feature to check.
     * @param context The context object to check against.
     * @return A boolean indicating if the feature is enabled for the user.
     */
    public boolean checkInUserStorage(Settings settings, String featureKey, VWOContext context) {
        StorageService storageService = new StorageService();
        Map<String, Object> storedDataMap = new StorageDecorator().getFeatureFromStorage(featureKey, context, storageService);
        try {
            String storageMapAsString = VWOClient.objectMapper.writeValueAsString(storedDataMap);
            Storage storedData = VWOClient.objectMapper.readValue(storageMapAsString, Storage.class);

            return storedData != null && storedDataMap.size() > 1;
        } catch (Exception exception) {
            LoggerService.log(LogLevelEnum.ERROR, "Error in checking feature in user storage. Got error: " + exception);
            return false;
        }
    }

}