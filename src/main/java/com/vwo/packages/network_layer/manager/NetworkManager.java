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
package com.vwo.packages.network_layer.manager;


import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.network_layer.client.NetworkClient;
import com.vwo.interfaces.networking.NetworkClientInterface;
import com.vwo.packages.network_layer.handlers.RequestHandler;
import com.vwo.packages.network_layer.models.GlobalRequestModel;
import com.vwo.packages.network_layer.models.RequestModel;
import com.vwo.packages.network_layer.models.ResponseModel;
import com.vwo.services.LoggerService;
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
  public ResponseModel post(RequestModel request) {
    try {
      RequestModel networkOptions = createRequest(request);
      if (networkOptions == null) {
        return null;
      } else {
        return client.POST(request);
      }
    } catch (Exception error) {
      LoggerService.log(LogLevelEnum.ERROR,  "Error when creating post request, error: " + error);
      return null;
    }
  }

  /**
   * Asynchronously sends a POST request to the server.
   * @param request - The RequestModel containing the URL, headers, and body of the POST request.
   */
  public void postAsync(RequestModel request) {
    executorService.submit(() -> {
      post(request);
    });
  }
}
