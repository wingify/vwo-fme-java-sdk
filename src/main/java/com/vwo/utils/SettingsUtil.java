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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.vwo.enums.CampaignTypeEnum;
import com.vwo.models.Campaign;
import com.vwo.models.Feature;
import com.vwo.models.Settings;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.services.LoggerService;

import static com.vwo.utils.CampaignUtil.setVariationAllocation;

public class SettingsUtil {

    /**
     * Processes the settings file and modifies it as required.
     * This method is called before the settings are used by the SDK.
     * It sets the variation allocation for each campaign.
     * It adds linked campaigns to each feature in the settings based on rules.
     * It adds isGatewayServiceRequired flag to each feature in the settings based on pre segmentation.
     * @param settings - The settings file to modify.
     */
    public static void processSettings(Settings settings) {
        try {
            List<Campaign> campaigns = settings.getCampaigns();

            for (int i = 0; i < campaigns.size(); i++) {
                Campaign campaign = campaigns.get(i);
                setVariationAllocation(campaign);
                campaigns.set(i, campaign);
            }
            addLinkedCampaignsToSettings(settings);
            addIsGatewayServiceRequiredFlag(settings);
        } catch (Exception exception) {
            LoggerService.log(LogLevelEnum.ERROR, "Exception occurred while processing settings " + exception.getMessage());
        }
    }

    /**
     * Adds linked campaigns to each feature in the settings based on rules.
     * @param settings  - The settings file to modify.
     */
    private static void addLinkedCampaignsToSettings(Settings settings) {

        // Create a map for quick access to campaigns by ID
        Map<Integer, Campaign> campaignMap = settings.getCampaigns().stream()
                .collect(Collectors.toMap(Campaign::getId, campaign -> campaign));

        // Loop over all features
        for (Feature feature : settings.getFeatures()) {
            List<Campaign> rulesLinkedCampaignModel = feature.getRules().stream()
                    .map(rule -> {
                        Campaign originalCampaign = campaignMap.get(rule.getCampaignId());
                        if (originalCampaign == null) return null;
                        originalCampaign.setRuleKey(rule.getRuleKey());
                        Campaign campaign = new Campaign();
                        campaign.setModelFromDictionary(originalCampaign);

                        // If a variationId is specified, find and add the variation
                        if (rule.getVariationId() != null) {
                            campaign.getVariations().stream()
                                    .filter(v -> v.getId().equals(rule.getVariationId()))
                                    .findFirst().ifPresent(variation -> campaign.setVariations(Collections.singletonList(variation)));
                        }
                        return campaign;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // Assign the linked campaigns to the feature
            feature.setRulesLinkedCampaign(rulesLinkedCampaignModel);
        }
    }

    /**
     * Adds isGatewayServiceRequired flag to each feature in the settings based on pre segmentation.
     * @param settings  - The settings file to modify.
     */
    private static void addIsGatewayServiceRequiredFlag(Settings settings) {
        // Updated pattern without using lookbehind
        String patternString = "\\b(country|region|city|os|device_type|browser_string|ua)\\b|\"custom_variable\"\\s*:\\s*\\{\\s*\"name\"\\s*:\\s*\"inlist\\([^)]*\\)\"";
        Pattern pattern = Pattern.compile(patternString);

        for (Feature feature : settings.getFeatures()) {
            List<Campaign> rules = feature.getRulesLinkedCampaign();
            for (Campaign rule : rules) {
                Object segments;
                if (Objects.equals(rule.getType(), CampaignTypeEnum.ROLLOUT.getValue()) || Objects.equals(rule.getType(), CampaignTypeEnum.PERSONALIZE.getValue())) {
                    segments = rule.getVariations().get(0).getSegments();
                } else {
                    segments = rule.getSegments();
                }
                if (segments != null) {
                    String jsonSegments = new Gson().toJson(segments);
                    Matcher matcher = pattern.matcher(jsonSegments);
                    boolean foundMatch = false;

                    while (matcher.find()) {
                        String match = matcher.group();
                        if (match.matches("\\b(country|region|city|os|device_type|browser_string|ua)\\b")) {
                            // Check if within "custom_variable" block
                            if (!isWithinCustomVariable(matcher.start(), jsonSegments)) {
                                foundMatch = true;
                                break;
                            }
                        } else {
                            foundMatch = true;
                            break;
                        }
                    }

                    if (foundMatch) {
                        feature.setIsGatewayServiceRequired(true);
                        break;
                    }
                }
            }
        }
    }

    // Helper method to check if a match is within "custom_variable"
    private static boolean isWithinCustomVariable(int startIndex, String input) {
        int index = input.lastIndexOf("\"custom_variable\"", startIndex);
        if (index == -1) return false;

        int closingBracketIndex = input.indexOf("}", index);
        return closingBracketIndex != -1 && startIndex < closingBracketIndex;
    }
}
