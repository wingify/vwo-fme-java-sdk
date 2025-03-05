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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vwo.VWOClient;
import com.vwo.constants.Constants;
import com.vwo.decorators.StorageDecorator;
import com.vwo.enums.CampaignTypeEnum;
import com.vwo.models.*;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.decision_maker.DecisionMaker;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.services.CampaignDecisionService;
import com.vwo.services.LoggerService;
import com.vwo.services.StorageService;

import java.util.*;
import java.util.stream.Collectors;

import static com.vwo.utils.CampaignUtil.*;
import static com.vwo.utils.DecisionUtil.evaluateTrafficAndGetVariation;
import static com.vwo.utils.FunctionUtil.*;
import static com.vwo.utils.RuleEvaluationUtil.evaluateRule;

public class MegUtil {

    /**
     * Evaluates groups for a given feature and group ID.
     *
     * @param settings - The settings model.
     * @param feature - The feature model to evaluate.
     * @param groupId - The ID of the group.
     * @param evaluatedFeatureMap - A map containing evaluated features.
     * @param context - The context model.
     * @return The evaluation result.
     */
    public static Variation evaluateGroups(Settings settings, Feature feature, int groupId,
                                           Map<String, Object> evaluatedFeatureMap, VWOContext context, StorageService storageService) {
        List<String> featureToSkip = new ArrayList<>();
        Map<String, List<Campaign>> campaignMap = new HashMap<>();

        // get all feature keys and all campaignIds from the groupId
        Map<String, List<?>> featureKeysAndGroupCampaignIds = getFeatureKeysFromGroup(settings, groupId);
        List<String> featureKeys = (List<String>) featureKeysAndGroupCampaignIds.get("featureKeys");
        List<String> groupCampaignIds = (List<String>) featureKeysAndGroupCampaignIds.get("groupCampaignIds");

        for (String featureKey : featureKeys) {
            Feature currentFeature = getFeatureFromKey(settings, featureKey);

            // check if the feature is already evaluated
            if (featureToSkip.contains(featureKey)) {
                continue;
            }

            // evaluate the feature rollout rules
            boolean isRolloutRulePassed = isRolloutRuleForFeaturePassed(settings, currentFeature, evaluatedFeatureMap, featureToSkip, context, storageService);
            if (isRolloutRulePassed) {
                for (Feature feature1 : settings.getFeatures()) {
                    if (feature1.getKey().equals(featureKey)) {
                        for (Campaign campaign : feature1.getRulesLinkedCampaign()) {
                            if(groupCampaignIds.contains(campaign.getId().toString()) || groupCampaignIds.contains(campaign.getId() + "_" + campaign.getVariations().get(0).getId())) {
                                campaignMap.putIfAbsent(featureKey, new ArrayList<>());
                                List<Campaign> campaigns = campaignMap.get(featureKey);
                                if (campaigns.stream().noneMatch(c -> c.getRuleKey().equals(campaign.getRuleKey()))) {
                                    campaigns.add(campaign);
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<String, Object> eligibleCampaignsMap = getEligibleCampaigns(settings, campaignMap, context, storageService);
        List<Campaign> eligibleCampaigns = (List<Campaign>) eligibleCampaignsMap.get("eligibleCampaigns");
        List<Campaign> eligibleCampaignsWithStorage = (List<Campaign>) eligibleCampaignsMap.get("eligibleCampaignsWithStorage");

        return findWinnerCampaignAmongEligibleCampaigns(settings, feature.getKey(), eligibleCampaigns, eligibleCampaignsWithStorage, groupId, context, storageService);
    }

    /**
     * Retrieves feature keys associated with a group based on the group ID.
     *
     * @param settings - The settings model.
     * @param groupId - The ID of the group.
     * @return An object containing feature keys and group campaign IDs.
     */
    public static Map<String, List<?>> getFeatureKeysFromGroup(Settings settings, int groupId) {
        List<String> groupCampaignIds = getCampaignsByGroupId(settings, groupId);
        List<String> featureKeys = getFeatureKeysFromCampaignIds(settings, groupCampaignIds);

        Map<String, List<?>> result = new HashMap<>();
        result.put("featureKeys", featureKeys);
        result.put("groupCampaignIds", groupCampaignIds);

        return result;
    }

    /**
     * Evaluates the feature rollout rules for a given feature.
     *
     * @param settings - The settings model.
     * @param feature - The feature model to evaluate.
     * @param evaluatedFeatureMap - A map containing evaluated features.
     * @param featureToSkip - A list of features to skip during evaluation.
     * @param context - The context model.
     * @return true if the feature passes the rollout rules, false otherwise.
     */
    private static boolean isRolloutRuleForFeaturePassed(Settings settings, Feature feature, Map<String, Object> evaluatedFeatureMap,
                                                         List<String> featureToSkip, VWOContext context,
                                                         StorageService storageService) {
        if (evaluatedFeatureMap.containsKey(feature.getKey()) &&
                ((Map<String, Object>) evaluatedFeatureMap.get(feature.getKey())).containsKey("rolloutId")) {
            return true;
        }

        List<Campaign> rollOutRules = getSpecificRulesBasedOnType(feature, CampaignTypeEnum.ROLLOUT);
        if (!rollOutRules.isEmpty()) {
            Campaign ruleToTestForTraffic = null;

            for (Campaign rule : rollOutRules) {
                Map<String, Object> preSegmentationResult = evaluateRule(settings, feature, rule, context, evaluatedFeatureMap, null, storageService, new HashMap<>());
                if ((Boolean) preSegmentationResult.get("preSegmentationResult")) {
                    ruleToTestForTraffic = rule;
                    break;
                }
            }

            if (ruleToTestForTraffic != null) {
                Variation variation = evaluateTrafficAndGetVariation(settings, ruleToTestForTraffic, context.getId());
                if (variation != null) {
                    Map<String, Object> rollOutInformation = new HashMap<>();
                    rollOutInformation.put("rolloutId", variation.getId());
                    rollOutInformation.put("rolloutKey", variation.getName());
                    rollOutInformation.put("rolloutVariationId", variation.getId());
                    evaluatedFeatureMap.put(feature.getKey(), rollOutInformation);
                    return true;
                }
            }

            // no rollout rule passed
            featureToSkip.add(feature.getKey());
            return false;
        }

        // no rollout rule, evaluate experiments
        LoggerService.log(LogLevelEnum.INFO, "MEG_SKIP_ROLLOUT_EVALUATE_EXPERIMENTS", new HashMap<String, String>(){
            {
                put("featureKey", feature.getKey());
            }
        });
        return true;
    }

    /**
     * Retrieves eligible campaigns based on the provided campaign map and context.
     *
     * @param settings - The settings model.
     * @param campaignMap - A map containing feature keys and corresponding campaigns.
     * @param context - The context model.
     * @return An object containing eligible campaigns, campaigns with storage, and ineligible campaigns.
     */
    private static Map<String, Object> getEligibleCampaigns(Settings settings, Map<String, List<Campaign>> campaignMap,
                                                            VWOContext context, StorageService storageService) {
        List<Campaign> eligibleCampaigns = new ArrayList<>();
        List<Campaign> eligibleCampaignsWithStorage = new ArrayList<>();
        List<Campaign> inEligibleCampaigns = new ArrayList<>();

        for (Map.Entry<String, List<Campaign>> entry : campaignMap.entrySet()) {
            String featureKey = entry.getKey();
            List<Campaign> campaigns = entry.getValue();

            for (Campaign campaign : campaigns) {
                 Map<String, Object> storedDataMap = new StorageDecorator().getFeatureFromStorage(featureKey, context, storageService);
                 try {
                     String storageMapAsString = VWOClient.objectMapper.writeValueAsString(storedDataMap);
                     Storage storedData = VWOClient.objectMapper.readValue(storageMapAsString, Storage.class);
                     if (storedData != null && storedData.getExperimentVariationId() != null && !storedData.getExperimentVariationId().toString().isEmpty()) {
                         if (storedData.getExperimentKey() != null && !storedData.getExperimentKey().isEmpty() && storedData.getExperimentKey().equals(campaign.getKey())) {
                             Variation variation = getVariationFromCampaignKey(settings, storedData.getExperimentKey(), storedData.getExperimentVariationId());
                             if (variation != null) {
                                 LoggerService.log(LogLevelEnum.INFO, "MEG_CAMPAIGN_FOUND_IN_STORAGE", new HashMap<String, String>(){
                                     {
                                         put("campaignKey", storedData.getExperimentKey());
                                         put("userId", context.getId());
                                     }
                                 });
                                 if (eligibleCampaignsWithStorage.stream().noneMatch(c -> c.getKey().equals(campaign.getKey()))) {
                                     eligibleCampaignsWithStorage.add(campaign);
                                 }
                                 continue;
                             }
                         }
                     }
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
                // Check if user is eligible for the campaign
                if (new CampaignDecisionService().getPreSegmentationDecision(campaign, context) &&
                        new CampaignDecisionService().isUserPartOfCampaign(context.getId(), campaign)) {
                    LoggerService.log(LogLevelEnum.INFO, "MEG_CAMPAIGN_ELIGIBLE", new HashMap<String, String>(){
                        {
                            put("campaignKey", campaign.getType().equals(CampaignTypeEnum.AB.getValue()) ? campaign.getKey() : campaign.getName() + "_" + campaign.getRuleKey());
                            put("userId", context.getId());
                        }
                    });
                    eligibleCampaigns.add(campaign);
                    continue;
                }

                inEligibleCampaigns.add(campaign);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("eligibleCampaigns", eligibleCampaigns);
        result.put("eligibleCampaignsWithStorage", eligibleCampaignsWithStorage);
        result.put("inEligibleCampaigns", inEligibleCampaigns);

        return result;
    }

    /**
     * Evaluates the eligible campaigns and determines the winner campaign.
     *
     * @param settings - The settings model.
     * @param featureKey - The key of the feature.
     * @param eligibleCampaigns - A list of eligible campaigns.
     * @param eligibleCampaignsWithStorage - A list of eligible campaigns with storage.
     * @param groupId - The ID of the group.
     * @param context - The context model.
     * @param storageService - The storage service.
     * @return The winner campaign.
     */
    private static Variation findWinnerCampaignAmongEligibleCampaigns(Settings settings, String featureKey,
                                                                      List<Campaign> eligibleCampaigns,
                                                                      List<Campaign> eligibleCampaignsWithStorage,
                                                                      int groupId, VWOContext context, StorageService storageService) {
        List<Integer> campaignIds = getCampaignIdsFromFeatureKey(settings, featureKey);
        Variation winnerCampaign = null;
        try {
            Groups group = settings.getGroups().get(String.valueOf(groupId));
            int megAlgoNumber = group != null && !group.getEt().toString().isEmpty()
                    ? group.getEt() : Constants.RANDOM_ALGO;
            if (eligibleCampaignsWithStorage.size() == 1) {
                try {
                    String campaignModel = VWOClient.objectMapper.writeValueAsString(eligibleCampaignsWithStorage.get(0));
                    winnerCampaign = VWOClient.objectMapper.readValue(campaignModel, Variation.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                Variation finalWinnerCampaign = winnerCampaign;
                LoggerService.log(LogLevelEnum.INFO, "MEG_WINNER_CAMPAIGN", new HashMap<String, String>(){
                    {
                        put("campaignKey", finalWinnerCampaign.getType().equals(CampaignTypeEnum.AB.getValue()) ? finalWinnerCampaign.getKey() : finalWinnerCampaign.getName() + "_" + finalWinnerCampaign.getRuleKey());
                        put("groupId", String.valueOf(groupId));
                        put("userId", context.getId());
                    }
                });
            } else if (eligibleCampaignsWithStorage.size() > 1 && megAlgoNumber == Constants.RANDOM_ALGO) {
                winnerCampaign = normalizeWeightsAndFindWinningCampaign(eligibleCampaignsWithStorage, context, campaignIds, groupId, storageService);
            } else if (eligibleCampaignsWithStorage.size() > 1) {
                winnerCampaign = getCampaignUsingAdvancedAlgo(settings, eligibleCampaignsWithStorage, context, campaignIds, groupId, storageService);
            }

            if (eligibleCampaignsWithStorage.isEmpty()) {
                if (eligibleCampaigns.size() == 1) {
                    try {
                        String campaignModel = VWOClient.objectMapper.writeValueAsString(eligibleCampaigns.get(0));
                        winnerCampaign = VWOClient.objectMapper.readValue(campaignModel, Variation.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    Variation finalWinnerCampaign1 = winnerCampaign;
                    LoggerService.log(LogLevelEnum.INFO, "MEG_WINNER_CAMPAIGN", new HashMap<String, String>(){
                        {
                            put("campaignKey", finalWinnerCampaign1.getType().equals(CampaignTypeEnum.AB.getValue()) ? finalWinnerCampaign1.getKey() : finalWinnerCampaign1.getName() + "_" + finalWinnerCampaign1.getRuleKey());
                            put("groupId", String.valueOf(groupId));
                            put("userId", context.getId());
                            put("algo", "");
                        }
                    });
                } else if (eligibleCampaigns.size() > 1 && megAlgoNumber == Constants.RANDOM_ALGO) {
                    winnerCampaign = normalizeWeightsAndFindWinningCampaign(eligibleCampaigns, context, campaignIds, groupId, storageService);
                } else if (eligibleCampaigns.size() > 1) {
                    winnerCampaign = getCampaignUsingAdvancedAlgo(settings, eligibleCampaigns, context, campaignIds, groupId, storageService);
                }
            }
        } catch (Exception exception) {
            LoggerService.log(LogLevelEnum.ERROR, "MEG: error inside findWinnerCampaignAmongEligibleCampaigns" + exception);
        }
        return winnerCampaign;
    }

    /**
     * Normalizes the weights of shortlisted campaigns and determines the winning campaign using random allocation.
     *
     * @param shortlistedCampaigns - A list of shortlisted campaigns.
     * @param context - The context model.
     * @param calledCampaignIds - A list of campaign IDs that have been called.
     * @param groupId - The ID of the group.
     * @param storageService - The storage service.
     * @return The winning campaign or null if none is found.
     */
    private static Variation normalizeWeightsAndFindWinningCampaign(List<Campaign> shortlistedCampaigns,
                                                                    VWOContext context, List<Integer> calledCampaignIds, int groupId, StorageService storageService) {
        try {
            shortlistedCampaigns.forEach(campaign -> campaign.setWeight((Math.round(100.0/shortlistedCampaigns.size()) * 10000)/10000.0));

            List<Variation> variations = shortlistedCampaigns.stream()
                    .map(campaign -> {
                        try {
                            String campaignModel = VWOClient.objectMapper.writeValueAsString(campaign);
                            return VWOClient.objectMapper.readValue(campaignModel, Variation.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            setCampaignAllocation(variations);
            Variation winnerVariation = new CampaignDecisionService().getVariation(
                    variations, new DecisionMaker().calculateBucketValue(getBucketingSeed(context.getId(), null, groupId))
            );

            if (winnerVariation != null) {
                LoggerService.log(LogLevelEnum.INFO, "MEG_WINNER_CAMPAIGN", new HashMap<String, String>(){
                    {
                        put("campaignKey", winnerVariation.getType().equals(CampaignTypeEnum.AB.getValue()) ? winnerVariation.getKey() : winnerVariation.getName() + "_" + winnerVariation.getRuleKey());
                        put("groupId", String.valueOf(groupId));
                        put("userId", context.getId());
                        put("algo", "using random algorithm");
                    }
                });

                Map<String, Object> storageMap = new HashMap<>();
                storageMap.put("featureKey", Constants.VWO_META_MEG_KEY + groupId);
                storageMap.put("userId", context.getId());
                storageMap.put("experimentId", winnerVariation.getId());
                storageMap.put("experimentKey", winnerVariation.getKey());
                storageMap.put("experimentVariationId", winnerVariation.getType().equals(CampaignTypeEnum.PERSONALIZE.getValue()) ? winnerVariation.getVariations().get(0).getId() : -1);
                new StorageDecorator().setDataInStorage(storageMap, storageService);

                if (calledCampaignIds.contains(winnerVariation.getId())) {
                    return winnerVariation;
                }
            } else {
                LoggerService.log(LogLevelEnum.INFO,"No winner campaign found for MEG group: " + groupId);
            }

        } catch (Exception exception) {
            LoggerService.log(LogLevelEnum.ERROR, "MEG: error inside normalizeWeightsAndFindWinningCampaign");
        }
        return null;
    }

    /**
     * Advanced algorithm to find the winning campaign based on priority order and weighted random distribution.
     *
     * @param settings - The settings model.
     * @param shortlistedCampaigns - A list of shortlisted campaigns.
     * @param context - The context model.
     * @param calledCampaignIds - A list of campaign IDs that have been called.
     * @param groupId - The ID of the group.
     * @param storageService - The storage service.
     * @return The winning campaign or null if none is found.
     */
    private static Variation getCampaignUsingAdvancedAlgo(Settings settings, List<Campaign> shortlistedCampaigns,
                                                          VWOContext context, List<Integer> calledCampaignIds, int groupId, StorageService storageService) {
        Variation winnerCampaign = null;
        boolean found = false;
        try {
            Groups group = settings.getGroups().get(String.valueOf(groupId));
            List<String> priorityOrder = group != null && !group.getP().isEmpty()
                    ? group.getP() : new ArrayList<>();
            Map<String, Double> wt = group != null && !group.getWt().isEmpty()
                    ? group.getWt() : new HashMap<>();
            for (String integer : priorityOrder) {
                for (Campaign shortlistedCampaign : shortlistedCampaigns) {
                    if (Objects.equals(String.valueOf(shortlistedCampaign.getId()), integer)) {
                        String campaignModel = VWOClient.objectMapper.writeValueAsString(cloneObject(shortlistedCampaign));
                        winnerCampaign = VWOClient.objectMapper.readValue(campaignModel, Variation.class);
                        found = true;
                        break;
                    } else if ((shortlistedCampaign.getId() + "_" + shortlistedCampaign.getVariations().get(0).getId()).equals(integer)) {
                        String campaignModel = VWOClient.objectMapper.writeValueAsString(cloneObject(shortlistedCampaign));
                        winnerCampaign = VWOClient.objectMapper.readValue(campaignModel, Variation.class);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }

            if (winnerCampaign == null) {
                List<Campaign> participatingCampaignList = new ArrayList<>();
                for (Campaign campaign : shortlistedCampaigns) {
                    int campaignId = campaign.getId();
                    if (wt.containsKey(String.valueOf(campaignId))) {
                        Campaign clonedCampaign = (Campaign) cloneObject(campaign);
                        clonedCampaign.setWeight(wt.get(String.valueOf(campaignId)));
                        participatingCampaignList.add(clonedCampaign);
                    } else if (wt.containsKey(campaignId + "_" + campaign.getVariations().get(0).getId())) {
                        Campaign clonedCampaign = (Campaign) cloneObject(campaign);
                        clonedCampaign.setWeight(wt.get(campaignId + "_" + campaign.getVariations().get(0).getId()));
                        participatingCampaignList.add(clonedCampaign);
                    }
                }

                List<Variation> variations = participatingCampaignList.stream()
                        .map(campaign -> {
                            try {
                                String campaignModel = VWOClient.objectMapper.writeValueAsString(campaign);
                                return VWOClient.objectMapper.readValue(campaignModel, Variation.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList());

                setCampaignAllocation(variations);
                winnerCampaign = new CampaignDecisionService().getVariation(
                        variations, new DecisionMaker().calculateBucketValue(getBucketingSeed(context.getId(), null, groupId))
                );
            }

            Variation finalWinnerCampaign = winnerCampaign;


            if (winnerCampaign != null) {
                LoggerService.log(LogLevelEnum.INFO, "MEG_WINNER_CAMPAIGN", new HashMap<String, String>(){
                    {
                        put("campaignKey", finalWinnerCampaign.getType().equals(CampaignTypeEnum.AB.getValue()) ? finalWinnerCampaign.getKey() : finalWinnerCampaign.getName() + "_" + finalWinnerCampaign.getRuleKey());
                        put("groupId", String.valueOf(groupId));
                        put("userId", context.getId());
                        put("algo", "using advanced algorithm");
                    }
                });

                Map<String, Object> storageMap = new HashMap<>();
                storageMap.put("featureKey", Constants.VWO_META_MEG_KEY + groupId);
                storageMap.put("userId", context.getId());
                storageMap.put("experimentId", winnerCampaign.getId());
                storageMap.put("experimentKey", winnerCampaign.getKey());
                storageMap.put("experimentVariationId", winnerCampaign.getType().equals(CampaignTypeEnum.PERSONALIZE.getValue()) ? winnerCampaign.getVariations().get(0).getId() : -1);
                new StorageDecorator().setDataInStorage(storageMap, storageService);

                if (calledCampaignIds.contains(winnerCampaign.getId())) {
                    return winnerCampaign;
                }
            }  else {
                LoggerService.log(LogLevelEnum.INFO,"No winner campaign found for MEG group: " + groupId);
            }
        } catch (Exception exception) {
            LoggerService.log(LogLevelEnum.ERROR, "MEG: error inside getCampaignUsingAdvancedAlgo " + exception.getMessage());
        }

        return null;
    }

    /**
     * Converts the weight map to a map of integers.
     * @param wt - The weight map.
     * @return The converted map.
     */
    private static Map<Integer, Integer> convertWtToMap(Map<String, Integer> wt) {
        Map<Integer, Integer> wtToReturn = new HashMap<>();
        for (Map.Entry<String, Integer> entry : wt.entrySet()) {
            wtToReturn.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }
        return wtToReturn;
    }
}
