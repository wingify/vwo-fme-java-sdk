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
package com.vwo.packages.network_layer.manager;


import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.network_layer.client.NetworkClient;
import com.vwo.interfaces.networking.NetworkClientInterface;
import com.vwo.models.FlushInterface;
import com.vwo.packages.network_layer.handlers.RequestHandler;
import com.vwo.packages.network_layer.models.GlobalRequestModel;
import com.vwo.packages.network_layer.models.RequestModel;
import com.vwo.packages.network_layer.models.ResponseModel;
import com.vwo.services.LoggerService;
import com.vwo.utils.UsageStatsUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager {

  private static NetworkManager instance;

  private GlobalRequestModel config;
  private NetworkClientInterface client;
  private final ExecutorService executorService;

  public NetworkManager() {
    // Executors.newCachedThreadPool() is a factory method in the Java Executors class
    // that creates a thread pool that can dynamically adjust the number of threads it uses.
    this.executorService = Executors.newCachedThreadPool();
  }

  public static NetworkManager getInstance() {
    if (instance == null) {
      instance = new NetworkManager();
      return instance;
    }
    return instance;
  }

  public void attachClient(NetworkClientInterface client) {
    this.client = client;
    this.config = new GlobalRequestModel(null, null, null, null); // Initialize with default config
  }

  public void attachClient() {
    this.client =  new NetworkClient();
    this.config = new GlobalRequestModel(null, null, null, null); // Initialize with default config
  }

  public void setConfig(GlobalRequestModel config) {
    this.config = config;
  }

  public GlobalRequestModel getConfig() {
    return this.config;
  }

  public RequestModel createRequest(RequestModel request) {
    RequestHandler handler = new RequestHandler();
    return handler.createRequest(request, this.config); // Merge and create request
  }

  public ResponseModel get(RequestModel request) {
    try {
      RequestModel networkOptions = createRequest(request);
      if (networkOptions == null) {
        return null;
      } else {
        return client.GET(request);
      }
    } catch(Exception error) {
      LoggerService.log(LogLevelEnum.ERROR,  "Error when creating get request, error: " + error);
      return null;
    }
  }

  /**
   * Synchronously sends a POST request to the server.
   * @param request - The RequestModel containing the URL, headers, and body of the POST request.
   * @return
   */
  public ResponseModel post(RequestModel request, FlushInterface flushCallback) {
    ResponseModel response = null;
    try {
      RequestModel networkOptions = createRequest(request);
      if (networkOptions == null) {
        return null;
      } 
      // Perform the actual POST request
      response = client.POST(request);

      // Handle the response and trigger callback based on success or failure
      if (response != null && response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
          if (flushCallback != null) {
              flushCallback.onFlush(null, request.getBody().toString());  // Success, pass response body to callback
          }
      } else {
          if (flushCallback != null) {
              flushCallback.onFlush("Failed with status code: " + response.getStatusCode(), null);  // Failure, pass error message
          }
      }
    } catch (Exception error) {
      LoggerService.log(LogLevelEnum.ERROR,  "Error when creating post request, error: " + error);
      if (flushCallback != null) {
        flushCallback.onFlush("Error: " + error.getMessage(), null);  // Pass error message to callback
      }
      return null;
    }
    return response;
  }

  /**
 * Sends a POST request to the server either asynchronously or synchronously based on the postBatchData flag.
 *
 * @param request        The RequestModel containing the URL, headers, and body of the POST request.
 * @param flushCallback  The callback to be triggered after the request is processed.
 * @param postBatchData  If true, the POST request is sent synchronously in the current thread (used for batching);
 *                       if false, the POST request is sent asynchronously in a new thread.
 */

public boolean postAsync(RequestModel request, FlushInterface flushCallback, boolean postBatchData) {
  if(postBatchData) {
    ResponseModel response = post(request, flushCallback);
    //return true if response is success or false if response is not success
    if(response != null && response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
      return true;
    }
    return false;
  } else {
    executorService.submit(() -> {
      try {
          // Perform the actual POST request and handle response asynchronously
          ResponseModel response = post(request, flushCallback);
          if (response != null && response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            UsageStatsUtil.getInstance().clearUsageStats();
            return true;
          }
          return false;
      } catch (Exception ex) {
          LoggerService.log(LogLevelEnum.ERROR, "Error occurred during post request: " + ex.getMessage());
          return false;
      }
    });
    return true;
  }
}

}