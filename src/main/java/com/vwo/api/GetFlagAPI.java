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
package com.vwo.api;

import com.vwo.VWOClient;
import com.vwo.decorators.StorageDecorator;
import com.vwo.enums.ApiEnum;
import com.vwo.enums.CampaignTypeEnum;
import com.vwo.models.*;
import com.vwo.models.user.GetFlag;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.segmentation_evaluator.core.SegmentationManager;
import com.vwo.services.HooksManager;
import com.vwo.services.LoggerService;
import com.vwo.services.StorageService;
import com.vwo.utils.RuleEvaluationUtil;

import java.util.*;

import static com.vwo.utils.CampaignUtil.getVariationFromCampaignKey;
import static com.vwo.utils.DecisionUtil.evaluateTrafficAndGetVariation;
import static com.vwo.utils.FunctionUtil.*;
import static com.vwo.utils.ImpressionUtil.createAndSendImpressionForVariationShown;

public class GetFlagAPI {

    /**
     * This method is used to get the flag value for the given feature key.
     * @param featureKey Feature key for which flag value is to be fetched.
     * @param settings Settings object containing the account settings.
     * @param context  VWOContext object containing the user context.
     * @param hookManager  HooksManager object containing the integrations.
     * @return GetFlag object containing the flag value.
     */
    public static GetFlag getFlag(String featureKey, Settings settings, VWOContext context, HooksManager hookManager) {
        GetFlag getFlag = new GetFlag();
        boolean shouldCheckForExperimentsRules = false;

        Map<String, Object> passedRulesInformation = new HashMap<>();
        Map<String, Object> evaluatedFeatureMap = new HashMap<>();

        // get feature object from feature key
        Feature feature = getFeatureFromKey(settings, featureKey);

        /**
         * Decision object to be sent for the integrations
         */
        Map<String, Object> decision = new HashMap<>();
        decision.put("featureName", feature != null ? feature.getName() : null);
        decision.put("featureId", feature != null ? feature.getId() : null);
        decision.put("featureKey", feature != null ? feature.getKey() : null);
        decision.put("userId", context != null ? context.getId() : null);
        decision.put("api", ApiEnum.GET_FLAG);

        StorageService storageService = new StorageService();
        Map<String, Object> storedDataMap = new StorageDecorator().getFeatureFromStorage(featureKey, context, storageService);

        /**
         * If feature is found in the storage, return the stored variation
         */
        try {
            String storageMapAsString = VWOClient.objectMapper.writeValueAsString(storedDataMap);
            Storage storedData = VWOClient.objectMapper.readValue(storageMapAsString, Storage.class);
            if (storedData != null && storedData.getExperimentVariationId() != null && !storedData.getExperimentVariationId().toString().isEmpty()) {
                if (storedData.getExperimentKey() != null && !storedData.getExperimentKey().isEmpty()) {
                    Variation variation = getVariationFromCampaignKey(settings, storedData.getExperimentKey(), storedData.getExperimentVariationId());
                    // If variation is found in settings, return the variation
                    if (variation != null) {
                        LoggerService.log(LogLevelEnum.INFO, "STORED_VARIATION_FOUND", new HashMap<String, String>() {
                            {
                                put("variationKey", variation.getName());
                                put("userId", context.getId());
                                put("experimentType", "experiment");
                                put("experimentKey", storedData.getExperimentKey());
                            }
                        });
                        getFlag.setIsEnabled(true);
                        getFlag.setVariables(variation.getVariables());
                        return getFlag;
                    }
                }
            } else if (storedData != null && storedData.getRolloutKey() != null && !storedData.getRolloutKey().isEmpty() && storedData.getRolloutId() != null && !storedData.getRolloutId().toString().isEmpty()) {
                Variation variation = getVariationFromCampaignKey(settings, storedData.getRolloutKey(), storedData.getRolloutVariationId());
                // If variation is found in settings, evaluate experiment rules
                if (variation != null) {
                    LoggerService.log(LogLevelEnum.INFO, "STORED_VARIATION_FOUND", new HashMap<String, String>() {
                        {
                            put("variationKey", variation.getName());
                            put("userId", context.getId());
                            put("experimentType", "rollout");
                            put("experimentKey", storedData.getRolloutKey());
                        }
                    });

                    LoggerService.log(LogLevelEnum.DEBUG, "EXPERIMENTS_EVALUATION_WHEN_ROLLOUT_PASSED", new HashMap<String, String>() {
                        {
                            put("userId", context.getId());
                        }
                    });

                    getFlag.setIsEnabled(true);
                    shouldCheckForExperimentsRules = true;
                    Map<String, Object> featureInfo = new HashMap<>();
                    featureInfo.put("rolloutId", storedData.getRolloutId());
                    featureInfo.put("rolloutKey", storedData.getRolloutKey());
                    featureInfo.put("rolloutVariationId", storedData.getRolloutVariationId());
                    evaluatedFeatureMap.put(featureKey, featureInfo);
                    passedRulesInformation.putAll(featureInfo);
                }
            }
        } catch (Exception e) {
            LoggerService.log(LogLevelEnum.ERROR, "Error parsing stored data: " + e.getMessage());
        }

        /**
         * if feature is not found, return false
         */
        if (feature == null) {
            LoggerService.log(LogLevelEnum.ERROR, "FEATURE_NOT_FOUND", new HashMap<String, String>() {{
                put("featureKey", featureKey);
            }});
            getFlag.setIsEnabled(false);
            return getFlag;
        }

        SegmentationManager.getInstance().setContextualData(settings, feature, context);

        /**
         * get all the rollout rules for the feature and evaluate them
         * if any of the rollout rule passes, break the loop and evaluate the traffic
         */
        List<Campaign> rollOutRules = getSpecificRulesBasedOnType(feature, CampaignTypeEnum.ROLLOUT);
        if (!rollOutRules.isEmpty() && !getFlag.isEnabled()){
            List<Campaign> rolloutRulesToEvaluate = new ArrayList<>();
            for (Campaign rule : rollOutRules) {
                Map<String, Object> evaluateRuleResult = RuleEvaluationUtil.evaluateRule(settings, feature, rule, context, evaluatedFeatureMap, new HashMap<>(), storageService, decision);
                boolean preSegmentationResult = (Boolean) evaluateRuleResult.get("preSegmentationResult");
                // If pre-segmentation passes, add the rule to the list of rules to evaluate
                if (preSegmentationResult) {
                    rolloutRulesToEvaluate.add(rule);
                    Map<String, Object> featureMap = new HashMap<>();
                    featureMap.put("rolloutId", rule.getId());
                    featureMap.put("rolloutKey", rule.getKey());
                    featureMap.put("rolloutVariationId", rule.getVariations().get(0).getId());
                    evaluatedFeatureMap.put(featureKey, featureMap);
                    break;
                }
            }

            // Evaluate the passed rollout rule traffic and get the variation
            if (!rolloutRulesToEvaluate.isEmpty()) {
                Campaign passedRolloutCampaign = rolloutRulesToEvaluate.get(0);
                Variation variation = evaluateTrafficAndGetVariation(settings, passedRolloutCampaign, context.getId());
                if (variation != null) {
                    getFlag.setIsEnabled(true);
                    getFlag.setVariables(variation.getVariables());
                    shouldCheckForExperimentsRules = true;
                    updateIntegrationsDecisionObject(passedRolloutCampaign, variation, passedRulesInformation, decision);
                    createAndSendImpressionForVariationShown(settings, passedRolloutCampaign.getId(), variation.getId(), context);
                }
            }
        } else {
            LoggerService.log(LogLevelEnum.DEBUG, "EXPERIMENTS_EVALUATION_WHEN_NO_ROLLOUT_PRESENT", null);
            shouldCheckForExperimentsRules = true;
        }

        /**
         * If any rollout rule passed pre segmentation and traffic evaluation, check for experiment rules
         * If no rollout rule passed, return false
         */
        if (shouldCheckForExperimentsRules) {
            List<Campaign> experimentRulesToEvaluate = new ArrayList<>();
            List<Campaign> experimentRules = getAllExperimentRules(feature);
            Map<Integer, String> megGroupWinnerCampaigns = new HashMap<>();

            for (Campaign rule : experimentRules) {
                // Evaluate the rule here
                Map<String, Object> evaluateRuleResult = RuleEvaluationUtil.evaluateRule(settings, feature, rule, context, evaluatedFeatureMap, megGroupWinnerCampaigns, storageService, decision);
                boolean preSegmentationResult = (Boolean) evaluateRuleResult.get("preSegmentationResult");
                // If pre-segmentation passes, check if the rule has whitelisted variation or not
                if (preSegmentationResult) {
                    Variation whitelistedObject = (Variation) evaluateRuleResult.get("whitelistedObject");
                    // If whitelisted object is null, add the rule to the list of rules to evaluate
                    if (whitelistedObject == null) {
                        experimentRulesToEvaluate.add(rule);
                    } else {
                        // If whitelisted object is not null, update the decision object and send an impression
                        getFlag.setIsEnabled(true);
                        getFlag.setVariables(whitelistedObject.getVariables());
                        passedRulesInformation.put("experimentId", rule.getId());
                        passedRulesInformation.put("experimentKey", rule.getKey());
                        passedRulesInformation.put("experimentVariationId", whitelistedObject.getId());
                    }
                    break;
                }
            }

            // Evaluate the passed experiment rule traffic and get the variation
            if (!experimentRulesToEvaluate.isEmpty()) {
                Campaign campaign = experimentRulesToEvaluate.get(0);
                Variation variation = evaluateTrafficAndGetVariation(settings, campaign, context.getId());
                if (variation != null) {
                    getFlag.setIsEnabled(true);
                    getFlag.setVariables(variation.getVariables());
                    updateIntegrationsDecisionObject(campaign, variation, passedRulesInformation, decision);
                    createAndSendImpressionForVariationShown(settings, campaign.getId(), variation.getId(), context);
                }
            }
        }

        if (getFlag.isEnabled()){
            Map<String, Object> storageMap = new HashMap<>();
            storageMap.put("featureKey", feature.getKey());
            storageMap.put("userId", context.getId());
            storageMap.putAll(passedRulesInformation);
            new StorageDecorator().setDataInStorage(storageMap, storageService);
        }

        // Execute the integrations
        hookManager.set(decision);
        hookManager.execute(hookManager.get());

        /**
         * If the feature has an impact campaign, send an impression for the variation shown
         * If flag enabled - variation 2, else - variation 1
         */
        if (feature.getImpactCampaign() != null && feature.getImpactCampaign().getCampaignId() != null && !feature.getImpactCampaign().getCampaignId().toString().isEmpty()){
            LoggerService.log(LogLevelEnum.INFO, "IMPACT_ANALYSIS", new HashMap<String, String>() {
                {
                    put("userId", context.getId());
                    put("featureKey", featureKey);
                    put("status", getFlag.isEnabled() ? "enabled": "disabled");
                }
            });
            createAndSendImpressionForVariationShown(
                    settings,
                    feature.getImpactCampaign().getCampaignId(),
                    getFlag.isEnabled() ? 2 : 1,
                    context
            );
        }
        return getFlag;
    }

    /**
     * This method is used to update the integrations decision object with the campaign and variation details.
     * @param campaign  CampaignModel object containing the campaign details.
     * @param variation  VariationModel object containing the variation details.
     * @param passedRulesInformation  Map containing the information of the passed rules.
     * @param decision  Map containing the decision object.
     */
    private static void updateIntegrationsDecisionObject(Campaign campaign, Variation variation, Map<String, Object> passedRulesInformation, Map<String, Object> decision) {
        if (Objects.equals(campaign.getType(), CampaignTypeEnum.ROLLOUT.getValue())) {
            passedRulesInformation.put("rolloutId", campaign.getId());
            passedRulesInformation.put("rolloutKey", campaign.getName());
            passedRulesInformation.put("rolloutVariationId", variation.getId());
        } else {
            passedRulesInformation.put("experimentId", campaign.getId());
            passedRulesInformation.put("experimentKey", campaign.getKey());
            passedRulesInformation.put("experimentVariationId", variation.getId());
        }
        decision.putAll(passedRulesInformation);
    }
}

