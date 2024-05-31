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
package com.vwo.utils;

import java.util.*;

import com.vwo.enums.CampaignTypeEnum;
import com.vwo.enums.StatusEnum;
import com.vwo.models.Campaign;
import com.vwo.models.Feature;
import com.vwo.models.Settings;
import com.vwo.models.Variation;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.decision_maker.DecisionMaker;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.segmentation_evaluator.core.SegmentationManager;
import com.vwo.services.CampaignDecisionService;
import com.vwo.services.LoggerService;
import com.vwo.services.StorageService;

import static com.vwo.utils.CampaignUtil.*;

public class DecisionUtil {
    /**
     * This method is used to evaluate the rule for a given feature and campaign.
     * @param settings  SettingsModel object containing the account settings.
     * @param feature   FeatureModel object containing the feature settings.
     * @param campaign  CampaignModel object containing the campaign settings.
     * @param context  VWOContext object containing the user context.
     * @param evaluatedFeatureMap  Map containing the evaluated feature map.
     * @param megGroupWinnerCampaigns  Map containing the MEG group winner campaigns.
     * @param decision  Map containing the decision object.
     * @return   Map containing the result of the evaluation.
     */
    public static Map<String, Object> checkWhitelistingAndPreSeg(
            Settings settings,
            Feature feature,
            Campaign campaign,
            VWOContext context,
            Map<String, Object> evaluatedFeatureMap,
            Map<Integer, Integer> megGroupWinnerCampaigns,
            StorageService storageService,
            Map<String, Object> decision) {

        String vwoUserId = UUIDUtils.getUUID(context.getId(), settings.getAccountId().toString());
        int campaignId = campaign.getId();

        // If the campaign is of type AB, set the _vwoUserId for variation targeting variables
        if (Objects.equals(campaign.getType(), CampaignTypeEnum.AB.getValue())) {
            // set _vwoUserId for variation targeting variables
            context.setVariationTargetingVariables(
                    new HashMap<String, Object>() {{
                        putAll(context.getVariationTargetingVariables());
                        put("_vwoUserId", campaign.getIsUserListEnabled() ? vwoUserId : context.getId());
                    }});

            decision.put("variationTargetingVariables", context.getVariationTargetingVariables()); // for integration

            // check if the campaign satisfies the whitelisting
            if (campaign.getIsForcedVariationEnabled()) {
                Map<String, Object> whitelistedVariation = checkCampaignWhitelisting(campaign, context);
                if (whitelistedVariation != null) {
                    return new HashMap<String, Object>() {{
                        put("preSegmentationResult", true);
                        put("whitelistedObject", whitelistedVariation.get("variation"));
                    }};
                }
            } else {
                LoggerService.log(LogLevelEnum.INFO, "WHITELISTING_SKIP", new HashMap<String, String>() {{
                    put("userId", context.getId());
                    put("campaignKey", campaign.getRuleKey());
                }});
            }
        }

        // set _vwoUserId for custom variables
        context.setCustomVariables(
                new HashMap<String, Object>() {{
                    putAll(context.getCustomVariables());
                    put("_vwoUserId", campaign.getIsUserListEnabled() ? vwoUserId : context.getId());
                }});


        decision.put("customVariables", context.getCustomVariables()); // for integration

        // Check if RUle being evaluated is part of Mutually Exclusive Group
        String groupId = CampaignUtil.getGroupDetailsIfCampaignPartOfIt(settings, campaignId).get("groupId");
        if (groupId != null && !groupId.isEmpty()) {
            Integer groupWinnerCampaignId = megGroupWinnerCampaigns.get(Integer.parseInt(groupId));
            if (groupWinnerCampaignId != null && !groupWinnerCampaignId.toString().isEmpty() && groupWinnerCampaignId == campaignId) {
                // If the campaign is the winner of the MEG, return true
                return new HashMap<String, Object>() {{
                    put("preSegmentationResult", true);
                    put("whitelistedObject", null);
                }};
            } else if (groupWinnerCampaignId != null && !groupWinnerCampaignId.toString().isEmpty()) {
                // If the campaign is not the winner of the MEG, return false
                return new HashMap<String, Object>() {{
                    put("preSegmentationResult", false);
                    put("whitelistedObject", null);
                }};
            }
        }

        // If Whitelisting is skipped/failed, Check campaign's pre-segmentation
        boolean isPreSegmentationPassed = new CampaignDecisionService().getPreSegmentationDecision(campaign, context);

        if (isPreSegmentationPassed && groupId != null && !groupId.isEmpty()) {
            Variation variationModel = MegUtil.evaluateGroups(
                    settings,
                    feature,
                    Integer.parseInt(groupId),
                    evaluatedFeatureMap,
                    context,
                    storageService
            );
            if (variationModel != null && variationModel.getId() != null && variationModel.getId().equals(campaignId)) {
                return new HashMap<String, Object>() {{
                    put("preSegmentationResult", true);
                    put("whitelistedObject", null);
                }};
            }
            megGroupWinnerCampaigns.put(Integer.parseInt(groupId), variationModel != null && variationModel.getId() != null ? variationModel.getId() : 0);
            return new HashMap<String, Object>() {{
                put("preSegmentationResult", false);
                put("whitelistedObject", null);
            }};
        }
        return new HashMap<String, Object>() {{
            put("preSegmentationResult", isPreSegmentationPassed);
            put("whitelistedObject", null);
        }};
    }

    /**
     * This method is used to evaluate the traffic for a given campaign and get the variation.
     * @param settings  SettingsModel object containing the account settings.
     * @param campaign  CampaignModel object containing the campaign settings.
     * @param userId   String containing the user ID.
     * @return  VariationModel object containing the variation details.
     */
    public static Variation evaluateTrafficAndGetVariation(
            Settings settings,
            Campaign campaign,
            String userId) {

        // Get the variation allotted to the user
        Variation variation = new CampaignDecisionService().getVariationAllotted(userId, settings.getAccountId().toString(), campaign);
        if (variation == null) {
            LoggerService.log(LogLevelEnum.INFO, "USER_CAMPAIGN_BUCKET_INFO", new HashMap<String, String>() {{
                put("userId", userId);
                put("campaignKey", campaign.getRuleKey());
                put("status", "did not get any variation");
            }});
            return null;
        }

        LoggerService.log(LogLevelEnum.INFO, "USER_CAMPAIGN_BUCKET_INFO", new HashMap<String, String>() {{
            put("userId", userId);
            put("campaignKey", campaign.getRuleKey());
            put("status", "got variation: " + variation.getName());
        }});
        return variation;
    }

    /**
     * Check for whitelisting
     * @param campaign   Campaign object
     * @param context  Context object containing user information
     * @return   Whitelisted variation or null if not whitelisted
     */
    private static Map<String, Object> checkCampaignWhitelisting(Campaign campaign, VWOContext context) {
        Map<String, Object> whitelistingResult = evaluateWhitelisting(campaign, context);
        StatusEnum status = whitelistingResult != null ? StatusEnum.PASSED : StatusEnum.FAILED;
        String variationString = whitelistingResult != null ? (String) whitelistingResult.get("variationName") : "";
        LoggerService.log(LogLevelEnum.INFO, "WHITELISTING_STATUS", new HashMap<String, String>() {{
            put("userId", context.getId());
            put("campaignKey", campaign.getRuleKey());
            put("status", status.getStatus());
            put("variationString", variationString);
        }});
        return whitelistingResult;
    }

    /**
     * Evaluate whitelisting for a campaign
     * @param campaign  Campaign object
     * @param context  Context object containing user information
     * @return  Whitelisted variation or null if not whitelisted
     */
    private static Map<String, Object> evaluateWhitelisting(Campaign campaign, VWOContext context) {
        List<Variation> targetedVariations = new ArrayList<>();

        for (Variation variation : campaign.getVariations()) {
            if (variation.getSegments() != null && variation.getSegments().isEmpty()) {
                LoggerService.log(LogLevelEnum.INFO, "WHITELISTING_SKIP", new HashMap<String, String>() {{
                    put("userId", context.getId());
                    put("campaignKey", campaign.getRuleKey());
                    put("variation", !variation.getName().isEmpty() ? "for variation: " + variation.getName() : "");
                }});
                continue;
            }

            // Check for segmentation and evaluate
            if (variation.getSegments() != null) {
                boolean segmentationResult = SegmentationManager.getInstance().validateSegmentation(variation.getSegments(), (Map<String, Object>) context.getVariationTargetingVariables());

                if (segmentationResult) {
                    targetedVariations.add((Variation) FunctionUtil.cloneObject(variation));
                }
            }
        }

        Variation whitelistedVariation = null;

        if (targetedVariations.size() > 1) {
            scaleVariationWeights(targetedVariations);
            int currentAllocation = 0;
            int stepFactor = 0;
            for (Variation variation : targetedVariations) {
                stepFactor = assignRangeValues(variation, currentAllocation);
                currentAllocation += stepFactor;
            }
            whitelistedVariation = new CampaignDecisionService().getVariation(targetedVariations, new DecisionMaker().calculateBucketValue(getBucketingSeed(context.getId(), campaign, null)));
        } else if (targetedVariations.size() == 1) {
            whitelistedVariation = targetedVariations.get(0);
        }

        if (whitelistedVariation != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("variation", whitelistedVariation);
            map.put("variationName", whitelistedVariation.getName());
            map.put("variationId", whitelistedVariation.getId());
            return map;
        }

        return null;
    }
}
