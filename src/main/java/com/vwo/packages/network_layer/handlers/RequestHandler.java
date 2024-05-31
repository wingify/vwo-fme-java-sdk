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
package com.vwo.packages.network_layer.handlers;
import com.vwo.packages.network_layer.models.GlobalRequestModel;
import com.vwo.packages.network_layer.models.RequestModel;

import java.util.HashMap;
import java.util.Map;

public class RequestHandler {

  /**
   * Creates a new request by merging properties from a base request and a configuration model.
   * If both the request URL and the base URL from the configuration are missing, it returns null.
   * Otherwise, it merges the properties from the configuration into the request if they are not already set.
   *
   * @param request The initial request model.
   * @param config The global request configuration model.
   * @return The merged request model or null if both URLs are missing.
   */
  public RequestModel createRequest(RequestModel request, GlobalRequestModel config) {
    // Check if both the request URL and the configuration base URL are missing
    if ((config.getBaseUrl() == null || config.getBaseUrl().isEmpty())
            && (request.getUrl() == null || request.getUrl().isEmpty())) {
      return null; // Return null if no URL is specified
    }

    // Set the request URL, defaulting to the configuration base URL if not set
    if (request.getUrl() == null || request.getUrl().isEmpty()) {
      request.setUrl(config.getBaseUrl());
    }

    // Set the request timeout, defaulting to the configuration timeout if not set
    if (request.getTimeout() == -1) {
      request.setTimeout(config.getTimeout());
    }

    // Set the request body, defaulting to the configuration body if not set
    if (request.getBody() == null) {
      request.setBody(config.getBody());
    }

    // Set the request headers, defaulting to the configuration headers if not set
    if (request.getHeaders() == null) {
      request.setHeaders(config.getHeaders());
    }

    // Initialize request query parameters, defaulting to an empty map if not set
    Map<String, String> requestQueryParams = request.getQuery();
    if (requestQueryParams == null) {
      requestQueryParams = new HashMap<>();
    }

    // Initialize configuration query parameters, defaulting to an empty map if not set
    Map<String, Object> configQueryParams = config.getQuery();
    if (configQueryParams == null) {
      configQueryParams = new HashMap<>();
    }

    // Merge configuration query parameters into the request query parameters if they don't exist
    for (Map.Entry<String, Object> entry : configQueryParams.entrySet()) {
      requestQueryParams.putIfAbsent(entry.getKey(), (String) entry.getValue());
    }

    // Set the merged query parameters back to the request
    request.setQuery(requestQueryParams);

    return request; // Return the modified request
  }
}
