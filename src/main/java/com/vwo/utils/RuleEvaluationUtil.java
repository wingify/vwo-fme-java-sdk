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

import com.vwo.models.Campaign;
import com.vwo.models.Feature;
import com.vwo.models.Settings;
import com.vwo.models.Variation;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.services.LoggerService;
import com.vwo.services.StorageService;

import java.util.HashMap;
import java.util.Map;

import static com.vwo.utils.DecisionUtil.checkWhitelistingAndPreSeg;
import static com.vwo.utils.ImpressionUtil.createAndSendImpressionForVariationShown;

public class RuleEvaluationUtil {

    /**
     * This method is used to evaluate the rule for a given feature and campaign.
     * @param settings  SettingsModel object containing the account settings.
     * @param feature   FeatureModel object containing the feature settings.
     * @param campaign  CampaignModel object containing the campaign settings.
     * @param context   VWOContext object containing the user context.
     * @param evaluatedFeatureMap   Map containing the evaluated feature map.
     * @param megGroupWinnerCampaigns  Map containing the MEG group winner campaigns.
     * @param decision  Map containing the decision object.
     * @return
     */
    public static Map<String, Object> evaluateRule(
            Settings settings,
            Feature feature,
            Campaign campaign,
            VWOContext context,
            Map<String, Object> evaluatedFeatureMap,
            Map<Integer, Integer> megGroupWinnerCampaigns,
            StorageService storageService,
            Map<String, Object> decision
    ) {
        // Perform whitelisting and pre-segmentation checks
        try {
            // Check if the campaign satisfies the whitelisting and pre-segmentation
            Map<String, Object> checkResult = checkWhitelistingAndPreSeg(
                    settings,
                    feature,
                    campaign,
                    context,
                    evaluatedFeatureMap,
                    megGroupWinnerCampaigns,
                    storageService,
                    decision
            );

            // Extract the results of the evaluation
            boolean preSegmentationResult = (Boolean) checkResult.get("preSegmentationResult");
            Variation whitelistedObject = (Variation) checkResult.get("whitelistedObject");

            // If pre-segmentation is successful and a whitelisted object exists, proceed to send an impression
            if (preSegmentationResult && whitelistedObject != null && whitelistedObject.getId() != null) {
                // Update the decision object with campaign and variation details
                decision.put("experimentId", campaign.getId());
                decision.put("experimentKey", campaign.getKey());
                decision.put("experimentVariationId", whitelistedObject.getId());

                // Send an impression for the variation shown
                createAndSendImpressionForVariationShown(
                        settings,
                        campaign.getId(),
                        whitelistedObject.getId(),
                        context
                );
            }

            // Return the results of the evaluation
            Map<String, Object> result = new HashMap<>();
            result.put("preSegmentationResult", preSegmentationResult);
            result.put("whitelistedObject", whitelistedObject);
            result.put("updatedDecision", decision);
            return result;
        } catch (Exception exception) {
            LoggerService.log(LogLevelEnum.ERROR, "Error occurred while evaluating rule: " + exception);
            return new HashMap<>();
        }
    }
}
