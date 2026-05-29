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
package com.wingify.utils;

import com.wingify.constants.Constants;
import com.wingify.packages.logger.enums.LogLevelEnum;
import com.wingify.packages.network_layer.manager.NetworkManager;
import com.wingify.packages.network_layer.models.RequestModel;
import com.wingify.packages.network_layer.models.ResponseModel;
import com.wingify.services.LoggerService;
import com.wingify.services.SettingsManager;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import com.wingify.ServiceContainer;

public class GatewayServiceUtil {

    /**
     * Fetches data from the gateway service
     * @param queryParams The query parameters to send with the request
     * @param endpoint The endpoint to send the request to
     * @return The response data from the gateway service
     */
    public static String getFromGatewayService(ServiceContainer serviceContainer, Map<String, String> queryParams, String endpoint) {
        NetworkManager networkInstance = NetworkManager.getInstance();
        // if the base url contains the host name, this means the gateway service is not configured
        if (!serviceContainer.getSettingsManager().isGatewayServiceProvided) {
            serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_GATEWAY_URL", new HashMap<String, Object>() {
                {
                    put("brand", LogMessageUtil.getBrand(serviceContainer.getWingifyInitOptions().getIsViaVWO()));
                    putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                }
            });
            return null;
        }
        try {
            RequestModel request = new RequestModel(
                    serviceContainer.getSettingsManager().hostname,
                    "GET",
                    serviceContainer.getEndpointWithCollectionPrefix(endpoint),
                    queryParams,
                    null,
                    null,
                    serviceContainer.getSettingsManager().protocol,
                    serviceContainer.getSettingsManager().port
            );
            request.setRetryConfig(networkInstance.getRetryConfig());
            ResponseModel response = networkInstance.get(request);

            return response.getData();
        } catch (Exception e) {
            serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "ERROR_FETCHING_DATA_FROM_GATEWAY", new HashMap<String, Object>() {
                {
                    put("err", e.getMessage());
                    putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                }
            });
            return null;
        }
    }

     /**
     * Sends data to the gateway service
     * @param queryParams The query parameters to send with the request
     * @param payload The payload to send with the request
     * @param endpoint The endpoint to send the request to
     * @return The response data from the gateway service
     */
    public static String postToGatewayService(ServiceContainer serviceContainer, Map<String, String> queryParams, Map<String, Object> payload, String endpoint) {
        // get the network instance
        NetworkManager networkInstance = NetworkManager.getInstance();
        // if the base url contains the host name, this means the gateway service is not configured
        if (!serviceContainer.getSettingsManager().isGatewayServiceProvided) {
            serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "INVALID_GATEWAY_URL", new HashMap<String, Object>() {
                {
                    put("brand", LogMessageUtil.getBrand(serviceContainer.getWingifyInitOptions().getIsViaVWO()));
                    putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                }
            });
            return null;
        }
        try {
            // create the request
            RequestModel request = new RequestModel(
                    serviceContainer.getSettingsManager().hostname,
                    "POST",
                    serviceContainer.getEndpointWithCollectionPrefix(endpoint),
                    queryParams,
                    payload,
                    null,
                    serviceContainer.getSettingsManager().protocol,
                    serviceContainer.getSettingsManager().port
            );
            request.setRetryConfig(networkInstance.getRetryConfig());
            // send the request
            ResponseModel response = networkInstance.post(request, null);
            return response.getData();
        } catch (Exception e) {
            serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "ERROR_SENDING_DATA_TO_GATEWAY", new HashMap<String, Object>() {
                {
                    put("err", e.getMessage());
                    putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                }
            });
            return null;
        }
    }

    /**
     * Encodes the query parameters to ensure they are URL-safe
     * @param queryParams The query parameters to encode
     * @return The encoded query parameters
     */
    public static Map<String, String> getQueryParams(Map<String, String> queryParams) {
        Map<String, String> encodedParams = new HashMap<>();

        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            // Encode the parameter value to ensure it is URL-safe
            String encodedValue = URLEncoder.encode(entry.getValue());
            // Add the encoded parameter to the result map
            encodedParams.put(entry.getKey(), encodedValue);
        }

        return encodedParams;
    }
}