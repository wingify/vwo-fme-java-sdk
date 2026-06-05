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
package com.wingify.packages.segmentation_evaluator.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.wingify.WingifyClient;
import com.wingify.constants.Constants;
import com.wingify.enums.UrlEnum;
import com.wingify.models.Feature;
import com.wingify.models.Holdout;
import com.wingify.models.user.GatewayService;
import com.wingify.models.user.WingifyUserContext;
import com.wingify.packages.logger.enums.LogLevelEnum;
import com.wingify.packages.segmentation_evaluator.enums.SegmentOperatorValueEnum;
import com.wingify.packages.segmentation_evaluator.evaluators.SegmentOperandEvaluator;
import com.wingify.packages.segmentation_evaluator.evaluators.SegmentEvaluator;
import com.wingify.ServiceContainer;
import com.wingify.services.LoggerService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.wingify.utils.GatewayServiceUtil.getFromGatewayService;
import static com.wingify.utils.GatewayServiceUtil.getQueryParams;

public class SegmentationManager {
  private SegmentEvaluator evaluator;
  private LoggerService loggerService;

  public SegmentationManager(LoggerService loggerService) {
    this.loggerService = loggerService;
  }

  public SegmentationManager(LoggerService loggerService, boolean shouldInitializeEvaluator) {
    this.loggerService = loggerService;
    if (shouldInitializeEvaluator) {
      this.evaluator = new SegmentEvaluator();
    }
  }

  /**
   * This method sets the contextual data required for segmentation.
   * @param serviceContainer  ServiceContainer object containing the settings manager.
   * @param feature   FeatureModel object containing the feature settings.
   * @param context   WingifyUserContext object containing the user context.
   */
  public void setContextualData(ServiceContainer serviceContainer, Feature feature, WingifyUserContext context) {
    this.evaluator = new SegmentEvaluator();
    this.evaluator.segmentOperandEvaluator = new SegmentOperandEvaluator(serviceContainer);
    this.evaluator.context = context;
    this.evaluator.serviceContainer = serviceContainer;
    this.evaluator.feature = feature;

    // if user agent and ipAddress both are null or empty, return
    if ((context.getUserAgent() == null || context.getUserAgent().isEmpty()) && (context.getIpAddress() == null || context.getIpAddress().isEmpty())) {
      return;
    }

    // Check if any holdout requires gateway service
    List<Holdout> holdouts = serviceContainer.getSettings().getHoldouts();
    boolean isGatewayServiceRequiredForHoldouts = false;
    if (holdouts != null) {
      isGatewayServiceRequiredForHoldouts = holdouts.stream()
              .anyMatch(holdout -> holdout.getIsGatewayServiceRequired() != null && holdout.getIsGatewayServiceRequired());
    }

    // If gateway service is required (by feature OR holdouts) and the base URL is not the default one, fetch the data from the gateway service
    if ((feature.getIsGatewayServiceRequired() || isGatewayServiceRequiredForHoldouts) && serviceContainer.getSettingsManager().isGatewayServiceProvided && (context.getWingify() == null)) {
      Map<String, String> queryParams = new HashMap<>();
      if ( (context.getUserAgent() == null || context.getUserAgent().isEmpty() ) && (context.getIpAddress() == null || context.getIpAddress().isEmpty())) {
        return;
      }
      if (context.getUserAgent() != null) {
        queryParams.put("userAgent", context.getUserAgent());
      }

      if (context.getIpAddress() != null) {
        queryParams.put("ipAddress", context.getIpAddress());
      }

      try {
        Map<String, String> params = getQueryParams(queryParams);
        String wingifyGatewayResponse = getFromGatewayService(serviceContainer, params, UrlEnum.GET_USER_DATA.getUrl());
        GatewayService gatewayServiceModel = WingifyClient.objectMapper.readValue(wingifyGatewayResponse, GatewayService.class);
        context.setWingify(gatewayServiceModel);
      } catch (Exception err) {
        loggerService.log(LogLevelEnum.ERROR, "ERROR_SETTING_SEGMENTATION_CONTEXT", new HashMap<String, Object>() {{
          put("err", err.getMessage());
          putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
        }});
      }
    }
  }

  /**
   * This method validates the segmentation for the given DSL and properties.
   * @param dsl     Object containing the segmentation DSL.
   * @param properties  Map containing the properties required for segmentation.
   * @return  Boolean value indicating whether the segmentation is valid or not.
   */
  public boolean validateSegmentation(Object dsl, Map<String, Object> properties) {
    try {
      JsonNode dslNodes = dsl instanceof String ? WingifyClient.objectMapper.readValue(dsl.toString(), JsonNode.class) : WingifyClient.objectMapper.valueToTree(dsl);
      // If the segment uses campaignVariation but the caller provided no webTestingCampaigns, fail the
      // whole rule immediately. Without this guard, NOT(campaignVariation) would flip to true when the
      // inner operand evaluates false due to a missing map — giving a wrong "user passes" result.
      if (containsCampaignVariationOperand(dslNodes) && !isWebTestingCampaignsProvided()) {
        return false;
      }

      return evaluator.isSegmentationValid(dslNodes, properties);
    } catch (Exception exception) {
      loggerService.log(LogLevelEnum.ERROR, "ERROR_VALIDATING_SEGMENTATION", new HashMap<String, Object>() {{
        put("err", exception.getMessage());
        putAll(evaluator.serviceContainer.getDebuggerService().getStandardDebugProps());
      }});
      return false;
    }
  }
  /** Returns true if the caller passed webTestingCampaigns in context.platformVariables. */
  private boolean isWebTestingCampaignsProvided() {
    WingifyUserContext context = evaluator.context;
    if (context == null || context.getPlatformVariables() == null) {
      return false;
    }
    return context.getPlatformVariables().get("webTestingCampaigns") != null;
  }

  /** Recursively walks the DSL tree and returns true if any node is a campaignVariation operand. */
  private boolean containsCampaignVariationOperand(JsonNode dsl) {
    if (dsl == null || dsl.isNull()) {
      return false;
    }
    if (dsl.isObject()) {
      Iterator<String> fieldNames = dsl.fieldNames();
      while (fieldNames.hasNext()) {
        String fieldName = fieldNames.next();
        if (SegmentOperatorValueEnum.WEB_CAMPAIGN_VARIATION.getValue().equals(fieldName)) {
          return true;
        }
        if (containsCampaignVariationOperand(dsl.get(fieldName))) {
          return true;
        }
      }
    } else if (dsl.isArray()) {
      for (JsonNode element : dsl) {
        if (containsCampaignVariationOperand(element)) {
          return true;
        }
      }
    }
    return false;
  }
}
