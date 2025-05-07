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

import com.vwo.constants.Constants;
import com.vwo.enums.CampaignTypeEnum;
import com.vwo.models.*;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.services.LoggerService;

import java.util.*;

public class CampaignUtil {

    /**
     * Sets the variation allocation for a given campaign based on its type.
     * If the campaign type is ROLLOUT or PERSONALIZE, it handles the campaign using `_handleRolloutCampaign`.
     * Otherwise, it assigns range values to each variation in the campaign.
     * @param campaign The campaign for which to set the variation allocation.
     */
    public static void setVariationAllocation(Campaign campaign) {
        // Check if the campaign type is roll out or PERSONALIZE
        if (Objects.equals(campaign.getType(), CampaignTypeEnum.ROLLOUT.getValue()) || Objects.equals(campaign.getType(), CampaignTypeEnum.PERSONALIZE.getValue())) {
            handleRolloutCampaign(campaign);
        } else {
            int currentAllocation = 0;
            // Iterate over each variation in the campaign
            for (Variation variation : campaign.getVariations()) {
                // Assign range values to the variation and update the current allocation
                int stepFactor = assignRangeValues(variation, currentAllocation);
                currentAllocation += stepFactor;
                LoggerService.log(LogLevelEnum.INFO, "VARIATION_RANGE_ALLOCATION", new HashMap<String, String>() {
                    {
                        put("campaignKey", campaign.getKey());
                        put("variationKey", variation.getName());
                        put("variationWeight", String.valueOf(variation.getWeight()));
                        put("startRange", String.valueOf(variation.getStartRangeVariation()));
                        put("endRange", String.valueOf(variation.getEndRangeVariation()));
                    }
                });
            }
        }
    }

    /**
     * Assigns start and end range values to a variation based on its weight.
     * @param data The variation model to assign range values.
     * @param currentAllocation The current allocation value before this variation.
     * @return The step factor calculated from the variation's weight.
     */
    public static int assignRangeValues(Variation data, int currentAllocation) {
        // Calculate the bucket range based on the variation's weight
        int stepFactor = getVariationBucketRange(data.getWeight());

        // Set the start and end range of the variation
        if (stepFactor > 0) {
            data.setStartRangeVariation(currentAllocation + 1);
            data.setEndRangeVariation(currentAllocation + stepFactor);
        } else {
            data.setStartRangeVariation(-1);
            data.setEndRangeVariation(-1);
        }
        return stepFactor;
    }

    /**
     * Scales the weights of variations to sum up to 100%.
     * @param variations The list of variations to scale.
     */
    public static void scaleVariationWeights(List<Variation> variations) {
        // Calculate the total weight of all variations
        double totalWeight = variations.stream().mapToDouble(Variation::getWeight).sum();

        // If total weight is zero, assign equal weight to each variation
        if (totalWeight == 0) {
            double equalWeight = 100.0 / variations.size();
            for (Variation variation : variations) {
                variation.setWeight(equalWeight);
            }
        } else {
            // Scale each variation's weight to make the total 100%
            for (Variation variation : variations) {
                variation.setWeight((variation.getWeight() / totalWeight) * 100);
            }
        }
    }

    /**
     * Generates a bucketing seed based on user ID, campaign, and optional group ID.
     * @param userId The user ID.
     * @param campaign The campaign object.
     * @param groupId The optional group ID.
     * @return The bucketing seed.
     */
    public static String getBucketingSeed(String userId, Campaign campaign, Integer groupId) {
        // Return a seed combining group ID and user ID if group ID is provided
        if (groupId != null) {
            return groupId + "_" + userId;
        }

        // get campaign type
        String campaignType = campaign.getType();
        // check if campaign type is rollout or personalize
        boolean isRolloutOrPersonalize = Objects.equals(campaignType, CampaignTypeEnum.ROLLOUT.getValue()) || 
                                       Objects.equals(campaignType, CampaignTypeEnum.PERSONALIZE.getValue());

        // Get salt based on campaign type
        String salt = isRolloutOrPersonalize ? campaign.getVariations().get(0).getSalt() : campaign.getSalt();
        // if salt is not null and not empty, use salt else use campaign id
        String bucketKey = (salt != null && !salt.isEmpty()) ? 
                          salt + "_" + userId : 
                          campaign.getId() + "_" + userId;
        // Return a seed combining campaign ID and user ID otherwise
        return bucketKey;
    }

    /**
     * Retrieves a variation by its ID within a specific campaign identified by its key.
     * @param settings The settings model containing all campaigns.
     * @param campaignKey The key of the campaign.
     * @param variationId The ID of the variation to retrieve.
     * @return The found variation model or null if not found.
     */
    public static Variation getVariationFromCampaignKey(Settings settings, String campaignKey, int variationId) {
        // Find the campaign by its key
        Campaign campaign = settings.getCampaigns().stream()
                .filter(c -> c.getKey().equals(campaignKey))
                .findFirst()
                .orElse(null);

        if (campaign != null) {
            // Find the variation by its ID within the found campaign
            Variation variation = campaign.getVariations().stream()
                    .filter(v -> v.getId() == variationId)
                    .findFirst()
                    .orElse(null);
            return variation;
        }
        return null;
    }

    /**
     * Sets the allocation ranges for a list of campaigns.
     * @param campaigns The list of campaigns to set allocations for.
     */
    public static void setCampaignAllocation(List<Variation> campaigns) {
        int currentAllocation = 0;
        for (Variation campaign : campaigns) {
            // Assign range values to each campaign and update the current allocation
            int stepFactor = assignRangeValuesMEG(campaign, currentAllocation);
            currentAllocation += stepFactor;
        }
    }

    /**
     * Determines if a campaign is part of a group.
     * @param settings The settings model containing group associations.
     * @param campaignId The ID of the campaign to check.
     * @return An object containing the group ID and name if the campaign is part of a group, otherwise an empty object.
     */
    public static Map<String, String> getGroupDetailsIfCampaignPartOfIt(Settings settings, int campaignId, int variationId) {
         // If variationId is null, that means that campaign is testing campaign
         // If variationId is not null, that means that campaign is personalization campaign and we need to append variationId to campaignId using _
         // then check if the current campaign is part of any group
        Map<String, String> groupDetails = new HashMap<>();
        String campaignToCheck = String.valueOf(campaignId);
        if (variationId != -1) {
            campaignToCheck = campaignToCheck + "_" + variationId;
        }
        if (settings.getCampaignGroups() != null && settings.getCampaignGroups().containsKey(campaignToCheck)) {
            int groupId = settings.getCampaignGroups().get(campaignToCheck);
            String groupName = settings.getGroups().get(String.valueOf(groupId)).getName();
            groupDetails.put("groupId", String.valueOf(groupId));
            groupDetails.put("groupName", groupName);
            return groupDetails;
        }
        return groupDetails;
    }

    /**
     * Finds all groups associated with a feature specified by its key.
     * @param settings The settings model containing all features and groups.
     * @param featureKey The key of the feature to find groups for.
     * @return An array of groups associated with the feature.
     */
    public static List<Map<String, String>> findGroupsFeaturePartOf(Settings settings, String featureKey) {
        // Initialize an array to store all rules for the given feature to fetch campaignId and variationId later
        List<Rule> ruleArrayList = new ArrayList<>();
        for (Feature feature : settings.getFeatures()) {
            if (feature.getKey().equals(featureKey)) {
                feature.getRules().forEach(rule -> {
                    // Add rule to the array if it's not already present
                    if (!ruleArrayList.contains(rule)) {
                        ruleArrayList.add(rule);
                    }
                });
            }
        }

        // Initialize an array to store all groups associated with the feature
        List<Map<String, String>> groups = new ArrayList<>();
        // Iterate over each rule to find the group details
        for (Rule rule : ruleArrayList) {
            Map<String, String> group = getGroupDetailsIfCampaignPartOfIt(settings, rule.getCampaignId(), rule.getType().equals(CampaignTypeEnum.PERSONALIZE.getValue()) ? rule.getVariationId() : -1);
            // Add group to the array if it's not already present
            if (!group.isEmpty() && groups.stream().noneMatch(g -> g.get("groupId").equals(group.get("groupId")))) {
                groups.add(group);
            }
        }
        return groups;
    }

    /**
     * Retrieves campaigns by a specific group ID.
     * @param settings The settings model containing all groups.
     * @param groupId The ID of the group.
     * @return An array of campaigns associated with the specified group ID.
     */
    public static List<String> getCampaignsByGroupId(Settings settings, int groupId) {
        // find the group
        Groups group = settings.getGroups().get(String.valueOf(groupId));
        return group.getCampaigns();
    }

    /**
     * Retrieves feature keys from a list of campaign IDs.
     * @param settings The settings model containing all features.
     * @param campaignIdWithVariation An array of campaign IDs.
     * @return An array of feature keys associated with the provided campaign IDs.
     */
    public static List<String> getFeatureKeysFromCampaignIds(Settings settings, List<String> campaignIdWithVariation) {
      List<String> featureKeys = new ArrayList<>();
      for (String campaign : campaignIdWithVariation) {
          // split key with _ to separate campaignId and variationId
          String[] campaignIdVariationId = campaign.split("_");
          int campaignId = Integer.parseInt(campaignIdVariationId[0]);
          Integer variationId = (campaignIdVariationId.length > 1) ? Integer.parseInt(campaignIdVariationId[1]) : null;
          // Iterate over each feature to find the feature key
         for (Feature feature : settings.getFeatures()) {
             // Break if feature key is already added
             if (featureKeys.contains(feature.getKey())) {
                 continue;
             }
             feature.getRules().forEach(rule -> {
                 if (rule.getCampaignId() == campaignId) {
                     // Check if variationId is provided and matches the rule's variationId
                     if (variationId != null) {
                         // Add feature key if variationId matches
                         if (Objects.equals(rule.getVariationId(), variationId)) {
                             featureKeys.add(feature.getKey());
                         }
                     } else {
                         // Add feature key if no variationId is provided
                         featureKeys.add(feature.getKey());
                     }
                 }
             });
         }
      }
      return featureKeys;
    }

    /**
     * Retrieves campaign IDs from a specific feature key.
     * @param settings The settings model containing all features.
     * @param featureKey The key of the feature.
     * @return An array of campaign IDs associated with the specified feature key.
     */
    public static List<Integer> getCampaignIdsFromFeatureKey(Settings settings, String featureKey) {
        List<Integer> campaignIds = new ArrayList<>();
        for (Feature feature : settings.getFeatures()) {
            if (feature.getKey().equals(featureKey)) {
                feature.getRules().forEach(rule -> campaignIds.add(rule.getCampaignId()));
            }
        }
        return campaignIds;
    }

    /**
     * Assigns range values to a campaign based on its weight.
     * @param data The campaign data containing weight.
     * @param currentAllocation The current allocation value before this campaign.
     * @return The step factor calculated from the campaign's weight.
     */
    public static int assignRangeValuesMEG(Variation data, int currentAllocation) {
        int stepFactor = getVariationBucketRange(data.getWeight());

        if (stepFactor > 0) {
            data.setStartRangeVariation(currentAllocation + 1);
            data.setEndRangeVariation(currentAllocation + stepFactor);
        } else {
            data.setStartRangeVariation(-1);
            data.setEndRangeVariation(-1);
        }
        return stepFactor;
    }

    /**
     * Retrieves the rule type using a campaign ID from a specific feature.
     * @param feature The feature containing rules.
     * @param campaignId The campaign ID to find the rule type for.
     * @return The rule type if found, otherwise an empty string.
     */
    public static String getRuleTypeUsingCampaignIdFromFeature(Feature feature, int campaignId) {
        return feature.getRules().stream()
                .filter(rule -> rule.getCampaignId() == campaignId)
                .map(Rule::getType)
                .findFirst()
                .orElse("");
    }

    /**
     * Calculates the bucket range for a variation based on its weight.
     * @param variationWeight The weight of the variation.
     * @return The calculated bucket range.
     */
    private static int getVariationBucketRange(double variationWeight) {
        if (variationWeight <= 0) {
            return 0;
        }
        int startRange = (int) Math.ceil(variationWeight * 100);
        return Math.min(startRange, Constants.MAX_TRAFFIC_VALUE);
    }

    /**
     * Handles the rollout campaign by setting start and end ranges for all variations.
     * @param campaign The campaign to handle.
     */
    private static void handleRolloutCampaign(Campaign campaign) {
        // Set start and end ranges for all variations in the campaign
        for (Variation variation : campaign.getVariations()) {
            int endRange = (int) (variation.getWeight() * 100);
            variation.setStartRangeVariation(1);
            variation.setEndRangeVariation(endRange);
            LoggerService.log(LogLevelEnum.INFO, "VARIATION_RANGE_ALLOCATION", new HashMap<String, String>() {
                {
                    put("campaignKey", campaign.getKey());
                    put("variationKey", variation.getName());
                    put("variationWeight", String.valueOf(variation.getWeight()));
                    put("startRange", String.valueOf(variation.getStartRangeVariation()));
                    put("endRange", String.valueOf(variation.getEndRangeVariation()));
                }
            });
        }
    }
}
