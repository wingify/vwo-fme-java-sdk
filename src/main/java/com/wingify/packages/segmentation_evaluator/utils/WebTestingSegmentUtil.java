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
package com.wingify.packages.segmentation_evaluator.utils;

import com.wingify.WingifyClient;
import com.wingify.ServiceContainer;
import com.wingify.models.user.WingifyUserContext;
import com.wingify.packages.logger.enums.LogLevelEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wingify.utils.DataTypeUtil.isObject;
import static com.wingify.utils.DataTypeUtil.isString;

public final class WebTestingSegmentUtil {
    private WebTestingSegmentUtil() {
    }
  
    /**
     * Result of evaluating a {@code campaignVariation} operand: whether the rule matched and whether the token shape was valid.
     * This class is used to store the result of evaluating a campaign variation operand.
     */
    public static class WebTestingCampaignVariationEval {
        private final boolean result;
        private final boolean invalidFormat;
        
        /**
         * Constructor to initialize the result and invalid format
         * 
         * @param result boolean indicating if the variation rule evaluated to true
         * @param invalidFormat boolean indicating if the operand string format was invalid
         */
        public WebTestingCampaignVariationEval(boolean result, boolean invalidFormat) {
            this.result = result;
            this.invalidFormat = invalidFormat;
        }

        public boolean isResult() {
            return result;
        }

        public boolean isInvalidFormat() {
            return invalidFormat;
        }
    }

    /**
     * Normalizes Web Testing campaign map keys and variation values to strings.
     *
     * @param rawAssignments the raw assignments map from the context (campaign id → variation id)
     * @return normalized map with string keys and values for regex matching; empty map if {@code rawAssignments} is null
     */
    public static Map<String, String> normalizeWebTestingCampaignsMap(Map<?, ?> rawAssignments) {
        // Turn the raw assignments map into a simple string map for regex matching.
        Map<String, String> campaignIdToVariationId = new HashMap<>();
        if (rawAssignments == null) {
            return campaignIdToVariationId;
        }
        // Iterate over the raw assignments map
        for (Map.Entry<?, ?> entry : rawAssignments.entrySet()) {
            Object campaignIdObj = entry.getKey();
            Object assignedVariationId = entry.getValue();
            if (campaignIdObj == null || assignedVariationId == null) {
                continue;
            }
            // Convert the campaign id to a string
            String campaignId = String.valueOf(campaignIdObj);
            // If the campaign id is empty, continue
            if (campaignId.isEmpty()) {
                continue;
            }
            // Put the campaign id and assigned variation id into the map
            campaignIdToVariationId.put(campaignId, String.valueOf(assignedVariationId));
        }
        return campaignIdToVariationId;
    }

    /**
     * Parses {@code context.platformVariables.webTestingCampaigns} from either a JSON string or plain object map.
     * 
     * @param context WingifyUserContext containing the platform variables
     * @param serviceContainer ServiceContainer for logging debugging and error messages
     * @return Normalized map of campaign id to variation id, or null if parsing fails or variables are absent
     */
    public static Map<String, String> parseWebTestingCampaignsFromContext(WingifyUserContext context, ServiceContainer serviceContainer) {
        if (context == null || context.getPlatformVariables() == null) {
            return null;
        }
        // Get the web testing campaigns from the context
        Object webTestingCampaignsInput = context.getPlatformVariables().get("webTestingCampaigns");
        // If the web testing campaigns are not found, return null
        if (webTestingCampaignsInput == null) {
            return null;
        }

        // If the web testing campaigns are a map, normalize them
        if (isObject(webTestingCampaignsInput) && webTestingCampaignsInput instanceof Map<?, ?>) {
            return normalizeWebTestingCampaignsMap((Map<?, ?>) webTestingCampaignsInput);
        }

        // If the web testing campaigns are a string, parse it only if it's an object.
        if (isString(webTestingCampaignsInput)) {
            String trimmedWebTestingCampaignsJson = String.valueOf(webTestingCampaignsInput).trim();
            if (trimmedWebTestingCampaignsJson.isEmpty()) {
                // Empty JSON string is invalid.
                return null;
            }
            try {
                // Duplicate keys in JSON silently take last-value-wins; warn the caller so they can fix the source data.
                // (Detection is only possible here when input is a raw string — already-parsed Maps lose duplicates before we see them.)
                if (hasDuplicateJsonKeys(trimmedWebTestingCampaignsJson)) {
                    serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_WEB_TESTING_CAMPAIGNS_DUPLICATE_KEY", new HashMap<String, Object>() {{
                        putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                    }});
                }
                Object parsedAssignments = WingifyClient.objectMapper.readValue(trimmedWebTestingCampaignsJson, Object.class);
                if (isObject(parsedAssignments) && parsedAssignments instanceof Map<?, ?>) {
                    return normalizeWebTestingCampaignsMap((Map<?, ?>) parsedAssignments);
                }
                // Parsed fine but it's an array/string/etc. Invalid shape.
                serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_WEB_TESTING_CAMPAIGNS_JSON", new HashMap<String, Object>() {{
                    putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                }});
            } catch (Exception ignored) {
                // Malformed JSON; treat like missing assignments.
                serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_WEB_TESTING_CAMPAIGNS_JSON", new HashMap<String, Object>() {{
                    putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                }});
                return null;
            }
            return null;
        }

        // Booleans/numbers/other odd types are invalid.
        String kind = webTestingCampaignsInput instanceof java.util.List<?> ? "array" : webTestingCampaignsInput.getClass().getSimpleName();
        serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_WEB_TESTING_CAMPAIGNS_TYPE", new HashMap<String, Object>() {{
            put("kind", kind);
            putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
        }});
        return null;
    }

    /**
     * Evaluates {@code campaignVariation} operand encoding against user's assigned variations.
     *
     * @param campaignVariationOperand token from the segment DSL (e.g. {@code "122_4"})
     * @param assignedVariationsByCampaignId map from campaign id to assigned variation id; {@code null} means no assignments
     * @return WebTestingCampaignVariationEval containing the evaluation result and format validity
     */
    public static WebTestingCampaignVariationEval evaluateWebTestingCampaignVariation(
            String campaignVariationOperand,
            Map<String, String> assignedVariationsByCampaignId) {
        // Null means empty assignments map.
        Map<String, String> assignments = assignedVariationsByCampaignId == null ? Collections.emptyMap() : assignedVariationsByCampaignId;

        Matcher match;

        //regex to match -> !123 — user should not be in campaign 123.
        match = Pattern.compile("^!(\\d+)$").matcher(campaignVariationOperand);
        if (match.matches()) {
            String campaignId = match.group(1);
            return new WebTestingCampaignVariationEval(!assignments.containsKey(campaignId), false);
        }

        //regex to match -> 123_!4 — in campaign 123 but not the variation 4.
        match = Pattern.compile("^(\\d+)_!(\\d+)$").matcher(campaignVariationOperand);
        if (match.matches()) {
            String campaignId = match.group(1);
            String variationId = match.group(2);
            if (!assignments.containsKey(campaignId)) {
                return new WebTestingCampaignVariationEval(false, false);
            }
            return new WebTestingCampaignVariationEval(!variationId.equals(assignments.get(campaignId)), false);
        }

        //regex to match -> 123_4 — must be exactly that campaign and variation.
        match = Pattern.compile("^(\\d+)_(\\d+)$").matcher(campaignVariationOperand);
        if (match.matches()) {
            String campaignId = match.group(1);
            String variationId = match.group(2);
            if (!assignments.containsKey(campaignId)) {
                return new WebTestingCampaignVariationEval(false, false);
            }
            return new WebTestingCampaignVariationEval(variationId.equals(assignments.get(campaignId)), false);
        }

        //regex to match -> 123 — in the campaign, any variation counts.
        match = Pattern.compile("^(\\d+)$").matcher(campaignVariationOperand);
        if (match.matches()) {
            String campaignId = match.group(1);
            return new WebTestingCampaignVariationEval(assignments.containsKey(campaignId), false);
        }

        // Invalid format.
        return new WebTestingCampaignVariationEval(false, true);
    }

    /**
     * Scans a raw JSON string for duplicate top-level keys.
     * Jackson silently keeps the last value for duplicates, so we catch this before parsing.
     * 
     * @param jsonString the raw JSON string to be scanned
     * @return boolean true if duplicate keys are found, false otherwise
     */
    private static boolean hasDuplicateJsonKeys(String jsonString) {
        Pattern keyPattern = Pattern.compile("\"([^\"]+)\"\\s*:");
        Matcher keyMatcher = keyPattern.matcher(jsonString);
        Set<String> seenKeys = new HashSet<>();
        while (keyMatcher.find()) {
            if (!seenKeys.add(keyMatcher.group(1))) {
                return true;
            }
        }
        return false;
    }
}