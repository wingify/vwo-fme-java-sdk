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

import static com.vwo.constants.Constants.HOLDOUT_VARIATION_IN;
import static com.vwo.constants.Constants.HOLDOUT_VARIATION_NOT_IN;
import com.vwo.decorators.StorageDecorator;
import com.vwo.models.Feature;
import com.vwo.models.Holdout;
import com.vwo.models.Settings;
import com.vwo.models.Storage;
import com.vwo.models.request.EventArchPayload;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.decision_maker.DecisionMaker;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.services.LoggerService;
import com.vwo.services.StorageService;
import com.vwo.ServiceContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.vwo.utils.ImpressionUtil.sendImpressionForVariationShown;
import static com.vwo.utils.ImpressionUtil.sendImpressionForVariationShownInBatch;
import static com.vwo.utils.NetworkUtil.createHoldoutPayload;
import static com.vwo.utils.DataTypeUtil.isNull;

public class HoldoutUtil {

    /**
     * Gets the applicable holdouts for a given feature ID.
     * 
     * @param settings  - The settings object.
     * @param featureId - The feature ID.
     * @return The applicable holdouts.
     */
    public static List<Holdout> getApplicableHoldouts(Settings settings, int featureId) {
        List<Holdout> holdouts = settings.getHoldouts();
        if (holdouts == null) {
            return new ArrayList<>();
        }
        // filter the holdouts to only include global holdouts and holdouts that have
        // the given feature ID
        return holdouts.stream()
                .filter(holdout -> (holdout.getIsGlobal() != null && holdout.getIsGlobal())
                        || (holdout.getFeatureIds() != null && holdout.getFeatureIds().contains(featureId)))
                .collect(Collectors.toList());
    }

    /**
     * Gets the matched holdout(s) for a given feature ID and context.
     * Evaluates all applicable holdouts, creates batched impressions for all of them,
     * and returns both matched and not-matched holdouts.
     *
     * @param serviceContainer - The service container.
     * @param feature          - The feature object.
     * @param context          - The context object.
     * @param storedData       - The stored data from storage (may contain isInHoldoutId/notInHoldoutId).
     * @return A map with "matchedHoldouts" and "notMatchedHoldouts" lists.
     */
    public static Map<String, Object> getMatchedHoldouts(
            ServiceContainer serviceContainer,
            Feature feature,
            VWOContext context,
            Storage storedData) {
        Settings settings = serviceContainer.getSettings();
        LoggerService loggerService = serviceContainer.getLoggerService();

        // storedData has isInHoldoutId and notInHoldoutId, use these to check if the holdout is already evaluated
        List<Integer> storedIsInHoldoutIds = storedData != null ? storedData.getIsInHoldoutId() : null;
        List<Integer> storedNotInHoldoutIds = storedData != null ? storedData.getNotInHoldoutId() : null;

        // combine both lists to determine all already-evaluated holdout IDs
        List<String> alreadyEvaluatedHoldoutIds = new ArrayList<>();
        if (storedIsInHoldoutIds != null) {
            for (Integer id : storedIsInHoldoutIds) {
                alreadyEvaluatedHoldoutIds.add(id.toString());
            }
        }
        if (storedNotInHoldoutIds != null) {
            for (Integer id : storedNotInHoldoutIds) {
                alreadyEvaluatedHoldoutIds.add(id.toString());
            }
        }

        Integer featureId = feature.getId();
        String featureKey = feature.getKey();
        // get the applicable holdouts for the given feature ID
        List<Holdout> applicableHoldouts = getApplicableHoldouts(settings, featureId);

        Map<String, Object> result = new HashMap<>();
        result.put("matchedHoldouts", new ArrayList<Holdout>());
        result.put("notMatchedHoldouts", new ArrayList<Holdout>());
        result.put("holdoutPayloads", new ArrayList<EventArchPayload>());

        // if there are no applicable holdouts, return empty result
        if (applicableHoldouts == null || applicableHoldouts.isEmpty()) {
            return result;
        }

        List<Holdout> matchedHoldouts = new ArrayList<>();
        // notMatchedHoldouts will be an array of holdouts that are not matched to the user
        List<Holdout> notMatchedHoldouts = new ArrayList<>();
        List<EventArchPayload> holdoutPayloads = new ArrayList<>();

        // iterate through the applicable holdouts
        // for each holdout, validate the segmentation and determine if user is IN or NOT IN
        for (Holdout holdout : applicableHoldouts) {
            String holdoutIdStr = holdout.getId().toString();
            // skip if holdout was already evaluated (present in stored data)
            if (alreadyEvaluatedHoldoutIds.contains(holdoutIdStr)) {
                loggerService.log(LogLevelEnum.DEBUG, "HOLDOUT_SKIP_EVALUATION", new HashMap<String, Object>() {
                    {
                        put("holdoutName", holdout.getName());
                        put("userId", context.getId());
                        put("featureKey", feature.getKey());
                    }
                });
                continue;
            }

            Map<String, Object> segments = holdout.getSegments();
            boolean segmentPass = true;
            if (segments != null && !segments.isEmpty()) {
                segmentPass = serviceContainer.getSegmentationManager().validateSegmentation(segments,
                        (Map<String, Object>) context.getCustomVariables());
                
                if (segmentPass) {
                    loggerService.log(LogLevelEnum.INFO, "SEGMENTATION_PASSED_HOLDOUT", new HashMap<String, Object>() {
                        {
                            put("userId", context.getId());
                            put("experimentId", holdout.getId());
                        }
                    });
                }
            } else {
                loggerService.log(LogLevelEnum.INFO, "HOLDOUT_SEGMENTATION_SKIP", new HashMap<String, Object>() {
                    {
                        put("holdoutId", holdout.getId());
                        put("userId", context.getId());
                    }
                });
            }

            // Determine variationId: HOLDOUT_VARIATION_IN if IN holdout, HOLDOUT_VARIATION_NOT_IN if NOT IN holdout
            int variationId;
            boolean isInHoldout = false;

            // if the segmentation fails, user is NOT IN holdout (variationId = 2)
            if (!segmentPass) {
                loggerService.log(LogLevelEnum.INFO, "SEGMENTATION_FAILED_HOLDOUT", new HashMap<String, Object>() {
                    {
                        put("userId", context.getId());
                        put("experimentId", holdout.getId());
                    }
                });
                variationId = HOLDOUT_VARIATION_NOT_IN; // NOT IN holdout
                notMatchedHoldouts.add(holdout);
            } else {
                // Check traffic allocation
                String hashKey = settings.getAccountId() + "_" + holdout.getId() + "_" + context.getId();
                long bucket = new DecisionMaker().getBucketValueForUser(hashKey, 100);

                // If bucket is within percentTraffic, user is IN holdout (variationId = HOLDOUT_VARIATION_IN)
                // Otherwise, user is NOT IN holdout (variationId = HOLDOUT_VARIATION_NOT_IN)
                isInHoldout = bucket != 0 && holdout.getPercentTraffic() != null && bucket <= holdout.getPercentTraffic();
                variationId = isInHoldout ? HOLDOUT_VARIATION_IN : HOLDOUT_VARIATION_NOT_IN;

                // Add all matched holdouts (user is IN)
                if (isInHoldout) {
                    loggerService.log(LogLevelEnum.INFO, "USER_IN_HOLDOUT", new HashMap<String, Object>() {
                        {
                            put("userId", context.getId());
                            put("featureKey", featureKey);
                            put("holdoutName", holdout.getName());
                        }
                    });
                    matchedHoldouts.add(holdout);
                } else {
                    notMatchedHoldouts.add(holdout);
                }
            }
            // Create holdout payload for ALL applicable holdouts (both IN and NOT IN)
            // campaignId is the holdoutId, variationId is HOLDOUT_VARIATION_IN (IN) or HOLDOUT_VARIATION_NOT_IN (NOT IN) 
            EventArchPayload payload = createHoldoutPayload(
                    serviceContainer,
                    holdout.getId(), // campaignId is the holdoutId
                    variationId, // 1 if IN holdout, 2 if NOT IN holdout
                    context,
                    featureId);

            if (payload != null) {
                holdoutPayloads.add(payload);
            }
        }

        result.put("matchedHoldouts", matchedHoldouts);
        result.put("notMatchedHoldouts", notMatchedHoldouts);
        result.put("holdoutPayloads", holdoutPayloads);
        return result;
    }

    /**
     * Sends network calls for holdouts that are applicable but not yet stored in storage.
     * This handles the case when new holdouts are added to settings after a user's data was stored.
     *
     * @param serviceContainer - The service container.
     * @param feature          - The feature model.
     * @param context          - The context object.
     * @param storedData       - The stored data from storage.
     * @param storageService   - The storage service.
     */
    public static List<Integer> sendNetworkCallsForNotInHoldouts(
            ServiceContainer serviceContainer,
            Feature feature,
            VWOContext context,
            Storage storedData,
            StorageService storageService) {
        List<Holdout> applicableHoldouts = getApplicableHoldouts(serviceContainer.getSettings(), feature.getId());
        List<Integer> updatedNotInHoldoutIds = new ArrayList<>(
                storedData != null && storedData.getNotInHoldoutId() != null
                        ? storedData.getNotInHoldoutId()
                        : new ArrayList<>()
        );
        List<Integer> isInHoldoutIds = storedData != null && storedData.getIsInHoldoutId() != null 
                ? storedData.getIsInHoldoutId() 
                : new ArrayList<>();
        List<EventArchPayload> batchPayloads = new ArrayList<>();
        int originalSize = updatedNotInHoldoutIds.size();

        // create payload for applicable holdouts that are not stored in storage
        for (Holdout holdout : applicableHoldouts) {
            // if storedData has notInHoldoutIds, check if the current holdoutId is not in the array and not in isInHoldoutIds
            if (!updatedNotInHoldoutIds.contains(holdout.getId()) && !isInHoldoutIds.contains(holdout.getId())) {
                // update the holdout ids list
                updatedNotInHoldoutIds.add(holdout.getId());

                EventArchPayload payload = createHoldoutPayload(
                        serviceContainer,
                        holdout.getId(),
                        HOLDOUT_VARIATION_NOT_IN,
                        context,
                        feature.getId());

                if (serviceContainer.getSettingsManager().isGatewayServiceProvided && payload != null) {
                    sendImpressionForVariationShown(serviceContainer, holdout.getId(), HOLDOUT_VARIATION_NOT_IN, context, payload);
                } else if (payload != null) {
                    batchPayloads.add(payload);
                }
            }
        }

        // Write to storage once after processing all holdouts
        if (updatedNotInHoldoutIds.size() > originalSize) {
            new StorageDecorator().setDataInStorage(
                    new HashMap<String, Object>() {{
                        put("featureKey", feature.getKey());
                        put("userId", context.getId());
                        put("notInHoldoutId", updatedNotInHoldoutIds);
                    }},
                    storageService,
                    serviceContainer
            );
        }

        if (!batchPayloads.isEmpty()) {
            if (!serviceContainer.getSettingsManager().isGatewayServiceProvided) {
                sendImpressionForVariationShownInBatch(batchPayloads, serviceContainer);
            }
        }

        return updatedNotInHoldoutIds;
    }
}
