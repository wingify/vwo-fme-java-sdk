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
package com.vwo.packages.segmentation_evaluator.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SegmentUtil {

    /**
     * Checks if the actual values match the expected values specified in the map.
     * @param expectedMap A map of expected values for different keys.
     * @param actualMap A map of actual values to compare against.
     * @return A boolean indicating if all actual values match the expected values.
     */
    public static boolean checkValuePresent(Map<String, List<String>> expectedMap, Map<String, String> actualMap) {
        for (String key : actualMap.keySet()) {
            if (expectedMap.containsKey(key)) {
                List<String> expectedValues = expectedMap.get(key);
                // convert expectedValues to lowercase
                expectedValues.replaceAll(String::toLowerCase);
                String actualValue = actualMap.get(key);

                // Handle wildcard patterns for all keys
                for (String val : expectedValues) {
                    if (val.startsWith("wildcard(") && val.endsWith(")")) {
                        String wildcardPattern = val.substring(9, val.length() - 1); // Extract pattern from wildcard string
                        Pattern regex = Pattern.compile(wildcardPattern.replace("*", ".*"), Pattern.CASE_INSENSITIVE); // Convert wildcard pattern to regex
                        Matcher matcher = regex.matcher(actualValue);
                        if (matcher.matches()) {
                            return true; // Match found, return true
                        }
                    }
                }

                // Direct value check for all keys
                if (expectedValues.contains(actualValue.trim().toLowerCase())) {
                    return true; // Direct value match found, return true
                }
            }
        }
        return false; // No matches found
    }

    /**
     * Compares expected location values with user's location to determine a match.
     * @param expectedLocationMap A map of expected location values.
     * @param userLocation The user's actual location.
     * @return A boolean indicating if the user's location matches the expected values.
     */
    public static boolean valuesMatch(Map<String, Object> expectedLocationMap, Map<String, String> userLocation) {
        for (Map.Entry<String, Object> entry : expectedLocationMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (userLocation.containsKey(key)) {
                String normalizedValue1 = normalizeValue(value);
                String normalizedValue2 = normalizeValue(userLocation.get(key));
                if (!normalizedValue1.equals(normalizedValue2)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true; // If all values match, return true
    }

    /**
     * Normalizes a value to a consistent format for comparison.
     * @param value The value to normalize.
     * @return The normalized value.
     */
    public static String normalizeValue(Object value) {
        if (value == null) {
            return null;
        }
        // Remove quotes and trim whitespace
        return value.toString().replaceAll("^\"|\"$", "").trim();
    }

    // Helper method to extract the first key-value pair from a map
    public static Map.Entry<String, JsonNode> getKeyValue(JsonNode node) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        return fields.next();
    }

    /**
     * Matches a string against a regular expression and returns the match result.
     * @param string - The string to match against the regex.
     * @param regex - The regex pattern as a string.
     * @return The results of the regex match, or null if an error occurs.
     */
    public static Boolean matchWithRegex(String string, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(string);
            return matcher.find();
        } catch (Exception e) {
            // Return null if an error occurs during regex matching
            return false;
        }
    }
}
