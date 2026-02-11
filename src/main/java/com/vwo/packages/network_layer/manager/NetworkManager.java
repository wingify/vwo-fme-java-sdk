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


import com.vwo.packages.network_layer.client.NetworkClient;
import com.vwo.interfaces.networking.NetworkClientInterface;
import com.vwo.models.FlushInterface;
import com.vwo.packages.network_layer.handlers.RequestHandler;
import com.vwo.packages.network_layer.models.GlobalRequestModel;
import com.vwo.packages.network_layer.models.RequestModel;
import com.vwo.packages.network_layer.models.ResponseModel;
import com.vwo.constants.Constants;
import com.vwo.services.LoggerService;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.models.user.RetryConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NetworkManager {

  private static NetworkManager instance;

  private GlobalRequestModel config;
  private NetworkClientInterface client;
  private ExecutorService executorService;
  private RetryConfig retryConfig;
  private LoggerService loggerService;
  
  // Thread pool configuration (configurable by user)
  private int maxPoolSize = Constants.THREAD_POOL_MAX_SIZE;
  private int queueSize = Constants.THREAD_POOL_QUEUE_SIZE;

  public NetworkManager() {
  }

  /**
   * Creates a bounded thread pool executor.
   * - Core pool size: Minimum threads always alive
   * - Max pool size: Maximum threads under load (configurable)
   * - Queue size: Tasks waiting in queue (configurable)
   * - Keep alive: Idle threads above core are terminated after timeout
   * - CallerRunsPolicy: If queue is full, caller thread executes the task
   * @return The configured ExecutorService.
   */
  private ExecutorService createThreadPool() {
    return new ThreadPoolExecutor(
        Constants.THREAD_POOL_MIN_SIZE,
        this.maxPoolSize,
        Constants.THREAD_POOL_KEEP_ALIVE_SECONDS,
        TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(this.queueSize),
        new ThreadPoolExecutor.DiscardPolicy()
    );
  }

  public static NetworkManager getInstance() {
    if (instance == null) {
      instance = new NetworkManager();
      return instance;
    }
    return instance;
  }

  /**
   * Returns the executor service.
   * @return The executor service.
   */
  public ExecutorService getExecutorService() {
    return this.executorService;
  }

  /**
   * Configures the thread pool with user-provided settings.
   * Supported keys: "maxPoolSize" (Integer), "queueSize" (Integer)
   * If values are invalid or not provided, defaults from Constants are used.
   * @param threadPoolConfig The thread pool configuration map.
   */
  public void configureThreadPool(Map<String, Object> threadPoolConfig) {
    if (threadPoolConfig != null && !threadPoolConfig.isEmpty()) {
      // Read maxPoolSize if provided and valid
      if (threadPoolConfig.get("maxPoolSize") != null) {
        try {
          int userMaxPoolSize = ((Number) threadPoolConfig.get("maxPoolSize")).intValue();
          if (userMaxPoolSize >= Constants.THREAD_POOL_MIN_SIZE) {
            this.maxPoolSize = userMaxPoolSize;
          } else if (this.loggerService != null) {
            this.loggerService.log(LogLevelEnum.WARN, "Invalid maxPoolSize value: " + userMaxPoolSize + 
                ". Must be >= " + Constants.THREAD_POOL_MIN_SIZE + ". Using default: " + Constants.THREAD_POOL_MAX_SIZE);
          }
        } catch (Exception e) {
          if (this.loggerService != null) {
            this.loggerService.log(LogLevelEnum.WARN, "Invalid maxPoolSize type. Using default: " + Constants.THREAD_POOL_MAX_SIZE);
          }
        }
      }

      // Read queueSize if provided and valid
      if (threadPoolConfig.get("queueSize") != null) {
        try {
          int userQueueSize = ((Number) threadPoolConfig.get("queueSize")).intValue();
          if (userQueueSize > 0) {
            this.queueSize = userQueueSize;
          } else if (this.loggerService != null) {
            this.loggerService.log(LogLevelEnum.WARN, "Invalid queueSize value: " + userQueueSize + 
                ". Must be > 0. Using default: " + Constants.THREAD_POOL_QUEUE_SIZE);
          }
        } catch (Exception e) {
          if (this.loggerService != null) {
            this.loggerService.log(LogLevelEnum.WARN, "Invalid queueSize type. Using default: " + Constants.THREAD_POOL_QUEUE_SIZE);
          }
        }
      }
    }

    // Always create thread pool (with defaults or user config)
    this.executorService = createThreadPool();
  }

  /**
   * Validates the retry configuration parameters.
   * @param retryConfig The retry configuration to validate.
   * @return The validated retry configuration with corrected values.
   */
  private RetryConfig validateRetryConfig(RetryConfig retryConfig) {
    if (retryConfig == null) {
      return new RetryConfig(
        Constants.DEFAULT_SHOULD_RETRY,
        Constants.DEFAULT_MAX_RETRIES,
        Constants.DEFAULT_INITIAL_DELAY,
        Constants.DEFAULT_BACKOFF_MULTIPLIER
      );
    }

    RetryConfig validatedConfig = new RetryConfig(retryConfig);
    boolean isInvalidConfig = false;

    // Validate shouldRetry: should be a boolean value
    if (validatedConfig.getShouldRetry() == null) {
      validatedConfig.setShouldRetry(Constants.DEFAULT_SHOULD_RETRY);
      isInvalidConfig = true;
    }

    // Validate maxRetries: should be >= 1
    if (validatedConfig.getMaxRetries() == null || validatedConfig.getMaxRetries() < 1) {
      validatedConfig.setMaxRetries(Constants.DEFAULT_MAX_RETRIES);
      isInvalidConfig = true;
    }

    // Validate initialDelay: should be >= 1
    if (validatedConfig.getInitialDelay() == null || validatedConfig.getInitialDelay() < 1) {
      validatedConfig.setInitialDelay(Constants.DEFAULT_INITIAL_DELAY);
      isInvalidConfig = true;
    }

    // Validate backoffMultiplier: should be >= MIN_BACKOFF_MULTIPLIER
    if (validatedConfig.getBackoffMultiplier() == null || validatedConfig.getBackoffMultiplier() < Constants.MIN_BACKOFF_MULTIPLIER) {
      validatedConfig.setBackoffMultiplier(Constants.DEFAULT_BACKOFF_MULTIPLIER);
      isInvalidConfig = true;
    }

    if (isInvalidConfig) {
      // Log error about invalid config
      if (this.loggerService != null) {
        this.loggerService.log(LogLevelEnum.ERROR, "INVALID_RETRY_CONFIG", new HashMap<String, Object>() {{
          put("retryConfig", retryConfig != null ? retryConfig.toString() : "null");
        }}, false);
      }
      return new RetryConfig(
        Constants.DEFAULT_SHOULD_RETRY,
        Constants.DEFAULT_MAX_RETRIES,
        Constants.DEFAULT_INITIAL_DELAY,
        Constants.DEFAULT_BACKOFF_MULTIPLIER
      );
    }

    return validatedConfig;
  }

  public void attachClient(NetworkClientInterface client) {
    this.client = client;
    this.config = new GlobalRequestModel(null, null, null, null);
  }

  public void attachClient() {
    this.client = new NetworkClient();
    this.config = new GlobalRequestModel(null, null, null, null);
  }

  /**
   * Sets the retry configuration for network requests.
   * Merges provided config with defaults and validates it.
   * @param retryConfig The retry configuration to set (can be null for defaults)
   */
  public void setRetryConfig(RetryConfig retryConfig) {
    RetryConfig defaultConfig = new RetryConfig(
      Constants.DEFAULT_SHOULD_RETRY,
      Constants.DEFAULT_MAX_RETRIES,
      Constants.DEFAULT_INITIAL_DELAY,
      Constants.DEFAULT_BACKOFF_MULTIPLIER
    );
    
    // Merge provided retryConfig with defaults
    if (retryConfig != null) {
      RetryConfig mergedConfig = new RetryConfig(
        retryConfig.getShouldRetry() != null ? retryConfig.getShouldRetry() : defaultConfig.getShouldRetry(),
        retryConfig.getMaxRetries() != null ? retryConfig.getMaxRetries() : defaultConfig.getMaxRetries(),
        retryConfig.getInitialDelay() != null ? retryConfig.getInitialDelay() : defaultConfig.getInitialDelay(),
        retryConfig.getBackoffMultiplier() != null ? retryConfig.getBackoffMultiplier() : defaultConfig.getBackoffMultiplier()
      );
      this.retryConfig = validateRetryConfig(mergedConfig);
    } else {
      this.retryConfig = defaultConfig;
    }
  }

  /**
   * Retrieves the current retry configuration.
   * @return A copy of the current retry configuration.
   */
  public RetryConfig getRetryConfig() {
    if (this.retryConfig == null) {
      return new RetryConfig(
        Constants.DEFAULT_SHOULD_RETRY,
        Constants.DEFAULT_MAX_RETRIES,
        Constants.DEFAULT_INITIAL_DELAY,
        Constants.DEFAULT_BACKOFF_MULTIPLIER
      );
    }
    return new RetryConfig(this.retryConfig);
  }

  public void setConfig(GlobalRequestModel config) {
    this.config = config;
  }

  public GlobalRequestModel getConfig() {
    return this.config;
  }

  /**
   * Creates a RequestModel from the given request.
   * @param request - The RequestModel containing the URL, headers, and body of the request.
   * @return The RequestModel containing the merged URL, headers, and body of the request.
   */
  public RequestModel createRequest(RequestModel request) {
    RequestHandler handler = new RequestHandler();
    return handler.createRequest(request, this.config); // Merge and create request
  }

  /**
   * Synchronously sends a GET request to the server.
   * @param request - The RequestModel containing the URL, headers, and body of the GET request.
   * @return The ResponseModel containing the status code and data of the GET request.
   */
  public ResponseModel get(RequestModel request) {
    RequestModel networkOptions = createRequest(request);
    if (networkOptions == null) {
      return null;
    } else {
      return client.GET(request);
    }
  }

  /**
   * Synchronously sends a POST request to the server.
   * @param request - The RequestModel containing the URL, headers, and body of the POST request.
   * @return
   */
  public ResponseModel post(RequestModel request, FlushInterface flushCallback) {
    ResponseModel response = null;
    RequestModel networkOptions = createRequest(request);
    if (networkOptions == null) {
      return null;
    } 
    // Perform the actual POST request
    response = client.POST(request);

    // Handle the response and trigger callback based on success or failure
    if (response != null && response.getStatusCode() >= Constants.HTTP_OK && response.getStatusCode() < Constants.HTTP_MULTIPLE_CHOICES) {
        if (flushCallback != null) {
            flushCallback.onFlush(null, request.getBody().toString());  // Success, pass response body to callback
        }
    } else {
        if (flushCallback != null) {
            flushCallback.onFlush("Failed with status code: " + response.getStatusCode(), null);  // Failure, pass error message
        }
    }
    return response;
  }

  /**
   * Sets the LoggerService instance.
   * @param loggerService The LoggerService to set.
   */
  public void setLoggerService(LoggerService loggerService) {
    this.loggerService = loggerService;
  }

  /**
   * Gets the LoggerService instance.
   * @return The LoggerService instance.
   */
  public LoggerService getLoggerService() {
    return this.loggerService;
  }
}
