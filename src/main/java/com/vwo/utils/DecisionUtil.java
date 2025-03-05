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

import com.vwo.VWOClient;
import com.vwo.constants.Constants;
import com.vwo.decorators.StorageDecorator;
import com.vwo.enums.CampaignTypeEnum;
import com.vwo.enums.StatusEnum;
import com.vwo.models.*;
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
            Map<Integer, String> megGroupWinnerCampaigns,
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
        String groupId = CampaignUtil.getGroupDetailsIfCampaignPartOfIt(settings, campaign.getId(), campaign.getType().equals(CampaignTypeEnum.PERSONALIZE.getValue()) ? campaign.getVariations().get(0).getId() : -1).get("groupId");
        if (groupId != null && !groupId.isEmpty()) {
            // check if the group is already evaluated for the user
            String groupWinnerCampaignId = megGroupWinnerCampaigns.get(Integer.parseInt(groupId));
            if (groupWinnerCampaignId != null && !groupWinnerCampaignId.isEmpty()) {
                if (campaign.getType().equals(CampaignTypeEnum.AB.getValue())) {
                    if (groupWinnerCampaignId.equals(String.valueOf(campaignId))) {
                        // If the campaign is the winner of the MEG, return true
                        return new HashMap<String, Object>() {{
                            put("preSegmentationResult", true);
                            put("whitelistedObject", null);
                        }};
                    }
                } else if (campaign.getType().equals(CampaignTypeEnum.PERSONALIZE.getValue())) {
                    // if personalise then check if the reqeusted variation is the winner
                    if (groupWinnerCampaignId.equals(campaign.getId() + "_" + campaign.getVariations().get(0).getId())) {
                        // If the campaign is the winner of the MEG, return true
                        return new HashMap<String, Object>() {{
                            put("preSegmentationResult", true);
                            put("whitelistedObject", null);
                        }};
                    }
                }
                // If the campaign is not the winner of the MEG, return false
                return new HashMap<String, Object>() {{
                    put("preSegmentationResult", false);
                    put("whitelistedObject", null);
                }};
            } else {
                // check in storage if the group is already evaluated for the user
                Map<String, Object> storedDataMap = new StorageDecorator().getFeatureFromStorage(Constants.VWO_META_MEG_KEY + groupId, context, storageService);
                try {
                    String storageMapAsString = VWOClient.objectMapper.writeValueAsString(storedDataMap);
                    Storage storedData = VWOClient.objectMapper.readValue(storageMapAsString, Storage.class);
                    if (storedData != null && storedData.getExperimentId() != null && storedData.getExperimentKey() != null) {
                        LoggerService.log(LogLevelEnum.INFO, "MEG_CAMPAIGN_FOUND_IN_STORAGE", new HashMap<String, String>(){
                            {
                                put("campaignKey", storedData.getExperimentKey());
                                put("userId", context.getId());
                            }
                        });
                        if (storedData.getExperimentId() == campaignId) {
                            if (campaign.getType().equals(CampaignTypeEnum.PERSONALIZE.getValue())) {
                                // if personalise then check if the reqeusted variation is the winner
                                if (storedData.getExperimentVariationId().equals(campaign.getVariations().get(0).getId())) {
                                    return new HashMap<String, Object>() {{
                                        put("preSegmentationResult", true);
                                        put("whitelistedObject", null);
                                    }};
                                } else {
                                    // store the campaign in local cache, so that it can be used later without looking into user storage again
                                    megGroupWinnerCampaigns.put(Integer.parseInt(groupId), storedData.getExperimentId() + "_" + storedData.getExperimentVariationId());
                                    return new HashMap<String, Object>() {{
                                        put("preSegmentationResult", false);
                                        put("whitelistedObject", null);
                                    }};
                                }
                            } else {
                                // return the campaign if the called campaignId matches
                                return new HashMap<String, Object>() {{
                                    put("preSegmentationResult", true);
                                    put("whitelistedObject", null);
                                }};
                            }
                        }
                        // if experimentId is not -1 then campaign is personalise campaign, store the details and return
                        if (storedData.getExperimentVariationId() != -1) {
                            megGroupWinnerCampaigns.put(Integer.parseInt(groupId), storedData.getExperimentId() + "_" + storedData.getExperimentVariationId());
                        } else {
                            // else store the campaignId only and return
                            megGroupWinnerCampaigns.put(Integer.parseInt(groupId), String.valueOf(storedData.getExperimentId()));
                        }
                        return new HashMap<String, Object>() {{
                            put("preSegmentationResult", false);
                            put("whitelistedObject", null);
                        }};
                    }
                } catch (Exception e) {
                    LoggerService.log(LogLevelEnum.ERROR, "STORED_DATA_ERROR", new HashMap<String, String>() {{
                        put("err", e.toString());
                    }});
                }
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
            // this condition would be true only when the current campaignId match with group winner campaignId
            // for personalise campaign, all personalise variations have same campaignId, so we check for campaignId_variationId
            if (variationModel != null && variationModel.getId() != null && variationModel.getId().equals(campaignId)) {
                // if campaign is AB then return true
                if (Objects.equals(variationModel.getType(), CampaignTypeEnum.AB.getValue())) {
                    return new HashMap<String, Object>() {{
                        put("preSegmentationResult", true);
                        put("whitelistedObject", null);
                    }};
                } else {
                    // if personalise then check if the requested variation is the winner
                    if (variationModel.getVariations().get(0).getId().equals(campaign.getVariations().get(0).getId())) {
                        return new HashMap<String, Object>() {{
                            put("preSegmentationResult", true);
                            put("whitelistedObject", null);
                        }};
                    } else {
                        // store the campaign in local cache, so that it can be used later
                        megGroupWinnerCampaigns.put(Integer.parseInt(groupId), variationModel.getId() + "_" + variationModel.getVariations().get(0).getId());
                        return new HashMap<String, Object>() {{
                            put("preSegmentationResult", false);
                            put("whitelistedObject", null);
                        }};
                    }
                }
            } else if (variationModel != null && variationModel.getId() != null) { // when there is a winner but not the current campaign
                if (variationModel.getType().equals(CampaignTypeEnum.AB.getValue())) {
                    // if campaign is AB then store only the campaignId
                    megGroupWinnerCampaigns.put(Integer.parseInt(groupId), String.valueOf(variationModel.getId()));
                } else {
                    // if campaign is personalise then store the campaignId_variationId
                    megGroupWinnerCampaigns.put(Integer.parseInt(groupId), variationModel.getId() + "_" + variationModel.getVariations().get(0).getId());
                }
                return new HashMap<String, Object>() {{
                    put("preSegmentationResult", false);
                    put("whitelistedObject", null);
                }};
            }
            // store -1 if no winner found, so that we don't evaluate the group again as the result would be the same for the current getFlag call
            megGroupWinnerCampaigns.put(Integer.parseInt(groupId), String.valueOf(-1));
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
                put("campaignKey", campaign.getType().equals(CampaignTypeEnum.AB.getValue()) ? campaign.getKey() : campaign.getName() + "_" + campaign.getRuleKey());
                put("status", "did not get any variation");
            }});
            return null;
        }

        LoggerService.log(LogLevelEnum.INFO, "USER_CAMPAIGN_BUCKET_INFO", new HashMap<String, String>() {{
            put("userId", userId);
            put("campaignKey", campaign.getType().equals(CampaignTypeEnum.AB.getValue()) ? campaign.getKey() : campaign.getName() + "_" + campaign.getRuleKey());
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
            put("campaignKey", campaign.getType().equals(CampaignTypeEnum.AB.getValue()) ? campaign.getKey() : campaign.getName() + "_" + campaign.getRuleKey());
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
                    put("campaignKey", campaign.getType().equals(CampaignTypeEnum.AB.getValue()) ? campaign.getKey() : campaign.getName() + "_" + campaign.getRuleKey());
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
