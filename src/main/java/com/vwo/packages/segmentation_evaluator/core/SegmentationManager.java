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
package com.vwo.packages.segmentation_evaluator.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.vwo.VWOClient;
import com.vwo.constants.Constants;
import com.vwo.enums.UrlEnum;
import com.vwo.models.Feature;
import com.vwo.models.Settings;
import com.vwo.models.user.GatewayService;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.segmentation_evaluator.evaluators.SegmentEvaluator;
import com.vwo.services.LoggerService;
import com.vwo.services.UrlService;

import java.util.HashMap;
import java.util.Map;

import static com.vwo.utils.GatewayServiceUtil.getFromGatewayService;
import static com.vwo.utils.GatewayServiceUtil.getQueryParams;

public class SegmentationManager {
  private static SegmentationManager instance;
  private SegmentEvaluator evaluator;

  public static SegmentationManager getInstance() {
    if (instance == null) {
      instance = new SegmentationManager();
    }
    return instance;
  }

  public void attachEvaluator(SegmentEvaluator segmentEvaluator) {
    this.evaluator = segmentEvaluator;
  }

  public void attachEvaluator() {
    this.evaluator = new SegmentEvaluator();
  }

  /**
   * This method sets the contextual data required for segmentation.
   * @param settings  SettingsModel object containing the account settings.
   * @param feature   FeatureModel object containing the feature settings.
   * @param context   VWOContext object containing the user context.
   */
  public void setContextualData(Settings settings, Feature feature, VWOContext context) {
    this.attachEvaluator();
    this.evaluator.context = context;
    this.evaluator.settings = settings;
    this.evaluator.feature = feature;

    // if user agent and ipAddress both are null or empty, return
    if ((context.getUserAgent() == null || context.getUserAgent().isEmpty()) && (context.getIpAddress() == null || context.getIpAddress().isEmpty())) {
      return;
    }

    // If gateway service is required and the base URL is not the default one, fetch the data from the gateway service
    if (feature.getIsGatewayServiceRequired() && !UrlService.getBaseUrl().contains(Constants.HOST_NAME) && (context.getVwo() == null)) {
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
        String _vwo = getFromGatewayService(params, UrlEnum.GET_USER_DATA.getUrl());
        GatewayService gatewayServiceModel = VWOClient.objectMapper.readValue(_vwo, GatewayService.class);
        context.setVwo(gatewayServiceModel);
      } catch (Exception err) {
        LoggerService.log(LogLevelEnum.ERROR, "Error in setting contextual data for segmentation. Got error: " + err);
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
      JsonNode dslNodes = dsl instanceof String ? VWOClient.objectMapper.readValue(dsl.toString(), JsonNode.class) : VWOClient.objectMapper.valueToTree(dsl);
      return evaluator.isSegmentationValid(dslNodes, properties);
    } catch (Exception exception) {
      LoggerService.log(LogLevelEnum.ERROR, "Exception occurred validate segmentation " + exception.getMessage());
      return false;
    }
  }
}
