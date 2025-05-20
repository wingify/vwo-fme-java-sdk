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

import com.vwo.constants.Constants;
import com.vwo.enums.CampaignTypeEnum;
import com.vwo.models.Campaign;
import com.vwo.models.Variation;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.decision_maker.DecisionMaker;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.segmentation_evaluator.core.SegmentationManager;

import java.util.*;

public class CampaignDecisionService {

    /**
     * This method is used to check if the user is part of the campaign.
     * @param userId  User ID for which the check is to be performed.
     * @param campaign CampaignModel object containing the campaign settings.
     * @return  boolean value indicating if the user is part of the campaign.
     */
    public boolean isUserPartOfCampaign(String userId, Campaign campaign) {
        if (campaign == null || userId == null) {
            return false;
        }
        double trafficAllocation;
        // Check if the campaign is of type ROLLOUT or PERSONALIZE
        // If yes, set the traffic allocation to the weight of the first variation
        String campaignType = campaign.getType();
        boolean isRolloutOrPersonalize = Objects.equals(campaignType, CampaignTypeEnum.ROLLOUT.getValue()) || 
                                       Objects.equals(campaignType, CampaignTypeEnum.PERSONALIZE.getValue());

        // Get salt and traffic allocation based on campaign type
        String salt = isRolloutOrPersonalize ? campaign.getVariations().get(0).getSalt() : campaign.getSalt();
        trafficAllocation = isRolloutOrPersonalize ? campaign.getVariations().get(0).getWeight() : campaign.getPercentTraffic();

        // Generate bucket key using salt if available, otherwise use campaign ID
        String bucketKey = (salt != null && !salt.isEmpty()) ? 
                          salt + "_" + userId : 
                          campaign.getId() + "_" + userId;

        int valueAssignedToUser = new DecisionMaker().getBucketValueForUser(bucketKey);
        boolean isUserPart = valueAssignedToUser != 0 && valueAssignedToUser <= trafficAllocation;

        LoggerService.log(LogLevelEnum.INFO, "USER_PART_OF_CAMPAIGN", new HashMap<String, String>() {{
            put("userId", userId);
            put("notPart", isUserPart? "" : "not");
            put("campaignKey", campaign.getType().equals(CampaignTypeEnum.AB.getValue()) ? campaign.getKey() : campaign.getName() + "_" + campaign.getRuleKey());
        }});
        return isUserPart;
    }

    /**
     * This method is used to get the variation for the user based on the bucket value.
     * @param variations  List of VariationModel objects containing the variations.
     * @param bucketValue  Bucket value assigned to the user.
     * @return  VariationModel object containing the variation for the user.
     */
    public Variation getVariation(List<Variation> variations, int bucketValue) {
        for (Variation variation : variations) {
            if (bucketValue >= variation.getStartRangeVariation() && bucketValue <= variation.getEndRangeVariation()) {
                return variation;
            }
        }
        return null;
    }

    /**
     * This method is used to check if the bucket value falls in the range of the variation.
     * @param variation  VariationModel object containing the variation settings.
     * @param bucketValue  Bucket value assigned to the user.
     * @return  VariationModel object containing the variation if the bucket value falls in the range, otherwise null.
     */
    public Variation checkInRange(Variation variation, int bucketValue) {
        if (bucketValue >= variation.getStartRangeVariation() && bucketValue <= variation.getEndRangeVariation()) {
            return variation;
        }
        return null;
    }

    /**
     * This method is used to bucket the user to a variation based on the bucket value.
     * @param userId  User ID for which the bucketing is to be performed.
     * @param accountId  Account ID for which the bucketing is to be performed.
     * @param campaign  CampaignModel object containing the campaign settings.
     * @return  VariationModel object containing the variation allotted to the user.
     */
    public Variation bucketUserToVariation(String userId, String accountId, Campaign campaign) {
        if (campaign == null || userId == null) {
            return null;
        }

        int multiplier = campaign.getPercentTraffic() != 0 ? 1 : 0;
        int percentTraffic = campaign.getPercentTraffic();
        // get salt from campaign
        String salt = campaign.getSalt();
        String bucketKey;
        // if salt is not null and not empty, use salt else use campaign id
        if (salt != null && !salt.isEmpty()) {
            bucketKey = salt + "_" + accountId + "_" + userId;
        } else {
            bucketKey = campaign.getId() + "_" + accountId + "_" + userId;
        }
        long hashValue = new DecisionMaker().generateHashValue(bucketKey);
        int bucketValue = new DecisionMaker().generateBucketValue(hashValue, Constants.MAX_TRAFFIC_VALUE, multiplier);

        LoggerService.log(LogLevelEnum.DEBUG, "USER_BUCKET_TO_VARIATION", new HashMap<String, String>() {{
            put("userId", userId);
            put("campaignKey", campaign.getRuleKey());
            put("percentTraffic", String.valueOf(percentTraffic));
            put("bucketValue", String.valueOf(bucketValue));
            put("hashValue", String.valueOf(hashValue));
        }});

        return getVariation(campaign.getVariations(), bucketValue);
    }

    /**
     * This method is used to analyze the pre-segmentation decision for the user in the campaign.
     * @param campaign  CampaignModel object containing the campaign settings.
     * @param context  VWOContext object containing the user context.
     * @return  boolean value indicating if the user passes the pre-segmentation.
     */
    public boolean getPreSegmentationDecision(Campaign campaign, VWOContext context) {
        String campaignType = campaign.getType();
        Map<String, Object> segments;

        if (Objects.equals(campaignType, CampaignTypeEnum.ROLLOUT.getValue()) || Objects.equals(campaignType, CampaignTypeEnum.PERSONALIZE.getValue())) {
            segments = campaign.getVariations().get(0).getSegments();
        } else if (Objects.equals(campaignType, CampaignTypeEnum.AB.getValue())) {
            segments = campaign.getSegments();
        } else {
            segments = Collections.emptyMap();
        }

        if (segments.isEmpty()) {
            LoggerService.log(LogLevelEnum.INFO, "SEGMENTATION_SKIP", new HashMap<String, String>() {{
                put("userId", context.getId());
                put("campaignKey",campaign.getType().equals(CampaignTypeEnum.AB.getValue()) ? campaign.getKey() : campaign.getName() + "_" + campaign.getRuleKey());
            }});
            return true;
        } else {
            boolean preSegmentationResult = SegmentationManager.getInstance().validateSegmentation(segments, (Map<String, Object>) context.getCustomVariables());
            LoggerService.log(LogLevelEnum.INFO, "SEGMENTATION_STATUS", new HashMap<String, String>() {{
                put("userId", context.getId());
                put("campaignKey",campaign.getType().equals(CampaignTypeEnum.AB.getValue()) ? campaign.getKey() : campaign.getName() + "_" + campaign.getRuleKey());
                put("status", preSegmentationResult ? "passed" : "failed");
            }});
            return preSegmentationResult;
        }
    }

    /**
     * This method is used to get the variation allotted to the user in the campaign.
     * @param userId  User ID for which the variation is to be allotted.
     * @param accountId  Account ID for which the variation is to be allotted.
     * @param campaign  CampaignModel object containing the campaign settings.
     * @return  VariationModel object containing the variation allotted to the user.
     */
    public Variation getVariationAllotted(String userId, String accountId, Campaign campaign) {
        boolean isUserPart = isUserPartOfCampaign(userId, campaign);
        if (Objects.equals(campaign.getType(), CampaignTypeEnum.ROLLOUT.getValue()) || Objects.equals(campaign.getType(), CampaignTypeEnum.PERSONALIZE.getValue())) {
            return isUserPart ? campaign.getVariations().get(0) : null;
        } else {
            return isUserPart ? bucketUserToVariation(userId, accountId, campaign) : null;
        }
    }
}