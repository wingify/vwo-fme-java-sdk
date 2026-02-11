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
package com.vwo.packages.network_layer.client;

import com.vwo.VWOClient;
import com.vwo.interfaces.networking.NetworkClientInterface;
import com.vwo.constants.Constants;
import com.vwo.packages.network_layer.models.RequestModel;
import com.vwo.packages.network_layer.models.ResponseModel;
import com.vwo.packages.network_layer.manager.NetworkManager;
import com.vwo.services.LoggerService;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.enums.EventEnum;
import com.vwo.models.user.RetryConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class NetworkClient implements NetworkClientInterface {

  public static String constructUrl(Map<String, Object> networkOptions) {
    String hostname = (String) networkOptions.get("hostname");
    String path = (String) networkOptions.get("path");
    if (networkOptions.get("port") != null && Integer.parseInt(networkOptions.get("port").toString()) != 0) {
        hostname += ":" + networkOptions.get("port");
    }
    return networkOptions.get("scheme").toString().toLowerCase() + "://" + hostname + path;
  }

  /**
   * Performs a GET request using the provided RequestModel with retry support.
   * Uses synchronous retry with Thread.sleep for exponential backoff.
   * @param requestModel The model containing request options.
   * @return A ResponseModel with the response data.
   */
  @Override
  public ResponseModel GET(RequestModel requestModel) {
    RetryConfig retryConfig = requestModel.getRetryConfig();
    Map<String, Object> networkOptions = requestModel.getOptions();
    String path = networkOptions.get("path") != null ? networkOptions.get("path").toString() : "";
    String endpoint = path.split("\\?")[0];
    
    int attempt = 0;
    String lastError = null;
    ResponseModel responseModel = new ResponseModel();

    while (attempt <= retryConfig.getMaxRetries()) {
      try {
        URL url = new URL(constructUrl(networkOptions));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (requestModel.getTimeout() > 0) {
          connection.setConnectTimeout(requestModel.getTimeout());
          connection.setReadTimeout(requestModel.getTimeout());
        }

        connection.connect();

        int statusCode = connection.getResponseCode();
        responseModel.setStatusCode(statusCode);

        String contentType = connection.getHeaderField("Content-Type");

        // Check for 400 Bad Request - reject immediately without retry
        if (statusCode == Constants.HTTP_BAD_REQUEST) {
          String error = "Bad Request. Status Code: 400";
          responseModel.setError(new Exception(error));
          responseModel.setTotalAttempts(attempt);
          return responseModel;
        }

        // Check for success
        if (statusCode == Constants.HTTP_OK && (contentType == null || contentType.contains("application/json"))) {
          BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          String inputLine;
          StringBuilder response = new StringBuilder();

          while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
          }
          in.close();

          responseModel.setData(response.toString());
          responseModel.setTotalAttempts(attempt);
          
          // If there were retries, set the last error
          if (attempt > 0 && lastError != null) {
            responseModel.setError(new Exception(lastError));
          }
          
          return responseModel;
        }

        // Handle error response
        BufferedReader errorReader = null;
        try {
          errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
          String errorLine = errorReader.readLine();
          lastError = "Invalid response " + errorLine + ", Status Code: " + statusCode + ", Response : " + connection.getResponseMessage();
        } finally {
          if (errorReader != null) errorReader.close();
        }

      } catch (Exception exception) {
        lastError = exception.getMessage();
      }

      // Check if we should retry
      if (!retryConfig.getShouldRetry() || attempt >= retryConfig.getMaxRetries()) {
        // Log failure (skip for VWO_DEBUGGER_EVENT to avoid infinite loop)
        if (!path.contains(EventEnum.VWO_DEBUGGER_EVENT.getValue())) {
          logFailure(endpoint, attempt, lastError);
        }
        responseModel.setTotalAttempts(attempt);
        responseModel.setError(new Exception(lastError));
        return responseModel;
      }

      // Calculate delay and sleep before retry
      int delay = retryConfig.getInitialDelay() * (int) Math.pow(retryConfig.getBackoffMultiplier(), attempt) * 1000;
      logRetryAttempt(endpoint, delay / 1000, attempt + 1, retryConfig.getMaxRetries(), lastError);
      
      try {
        Thread.sleep(delay);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        responseModel.setError(new Exception("Retry interrupted: " + ie.getMessage()));
        responseModel.setTotalAttempts(attempt);
        return responseModel;
      }   
   
      attempt++;
    }
    return responseModel;
  }

  /**
   * Performs a POST request using the provided RequestModel with retry support.
   * Uses synchronous retry with Thread.sleep for exponential backoff.
   * @param request The model containing request options.
   * @return A ResponseModel with the response data.
   */
  @Override
  public ResponseModel POST(RequestModel request) {
    RetryConfig retryConfig = request.getRetryConfig();
    Map<String, Object> networkOptions = request.getOptions();
    String path = networkOptions.get("path") != null ? networkOptions.get("path").toString() : "";
    String endpoint = path.split("\\?")[0];
    
    int attempt = 0;
    String lastError = null;
    ResponseModel responseModel = new ResponseModel();

    while (attempt <= retryConfig.getMaxRetries()) {
      try {
        URL url = new URL(constructUrl(networkOptions));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        if (request.getTimeout() > 0) {
          connection.setConnectTimeout(request.getTimeout());
          connection.setReadTimeout(request.getTimeout());
        }

        // Set headers
        for (Map.Entry<String, Object> entry : networkOptions.entrySet()) {
          if (entry.getKey().equals("headers")) {
            Map<String, String> headers = (Map<String, String>) entry.getValue();
            for (Map.Entry<String, String> header : headers.entrySet()) {
              connection.setRequestProperty(header.getKey(), header.getValue());
            }
          }
        }

        // Write body
        try (OutputStream os = connection.getOutputStream()) {
          Object body = networkOptions.get("body");

          if (body instanceof LinkedHashMap || body instanceof HashMap) {
            String jsonBody = VWOClient.objectMapper.writeValueAsString(body);
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
          } else if (body instanceof String) {
            byte[] input = ((String) body).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
          } else {
            throw new IllegalArgumentException("Unsupported body type: " + body.getClass().getName());
          }
        }

        int statusCode = connection.getResponseCode();
        responseModel.setStatusCode(statusCode);

        // Check for 400 Bad Request - reject immediately without retry
        if (statusCode == Constants.HTTP_BAD_REQUEST) {
          String error = "Bad Request. Status Code: 400";
          responseModel.setError(new Exception(error));
          responseModel.setTotalAttempts(attempt);
          return responseModel;
        }

        // Check for success
        if (statusCode == Constants.HTTP_OK) {
          BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
          StringBuilder response = new StringBuilder();
          String inputLine;

          while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
          }
          in.close();

          responseModel.setData(response.toString());
          responseModel.setTotalAttempts(attempt);

          // If there were retries, set the last error
          if (attempt > 0 && lastError != null) {
            responseModel.setError(new Exception(lastError));
          }

          return responseModel;
        }

        // Handle error response
        BufferedReader errorReader = null;
        String rawData = "";
        try {
          if (connection.getErrorStream() != null) {
            errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
              errorResponse.append(line);
            }
            rawData = errorResponse.toString();
          }
        } finally {
          if (errorReader != null) errorReader.close();
        }
        lastError = "Raw Data: " + rawData + ", Status Code: " + statusCode;

      } catch (Exception exception) {
        lastError = exception.getMessage();
      }

      // Check if we should retry
      if (!retryConfig.getShouldRetry() || attempt >= retryConfig.getMaxRetries()) {
        // Log failure (skip for VWO_DEBUGGER_EVENT to avoid infinite loop)
        if (!path.contains(EventEnum.VWO_DEBUGGER_EVENT.getValue())) {
          logFailure(endpoint, attempt, lastError);
        }
        responseModel.setTotalAttempts(attempt);
        responseModel.setError(new Exception(lastError));
        return responseModel;
      }

      // Calculate delay and sleep before retry
      int delay = retryConfig.getInitialDelay() * (int) Math.pow(retryConfig.getBackoffMultiplier(), attempt) * 1000;
      logRetryAttempt(endpoint, delay / 1000, attempt + 1, retryConfig.getMaxRetries(), lastError);
      
      try {
        Thread.sleep(delay);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        responseModel.setError(new Exception("Retry interrupted: " + ie.getMessage()));
        responseModel.setTotalAttempts(attempt);
        return responseModel;
      }     
      
      attempt++;
    }
    return responseModel;
  }

  /**
   * Logs a retry attempt.
   */
  private void logRetryAttempt(String endpoint, int delaySeconds, int attemptNum, int maxRetries, String errorMsg) {
    LoggerService loggerService = NetworkManager.getInstance().getLoggerService();
    if (loggerService != null) {
      loggerService.log(LogLevelEnum.ERROR, "ATTEMPTING_RETRY_FOR_FAILED_NETWORK_CALL", new HashMap<String, Object>() {{
        put("endPoint", endpoint);
        put("delay", String.valueOf(delaySeconds));
        put("attempt", String.valueOf(attemptNum));
        put("maxRetries", String.valueOf(maxRetries));
        put("err", errorMsg);
      }}, false);
    }
  }

  /**
   * Logs a failure after max retries.
   */
  private void logFailure(String endpoint, int attempts, String errorMsg) {
    LoggerService loggerService = NetworkManager.getInstance().getLoggerService();
    if (loggerService != null) {
      loggerService.log(LogLevelEnum.ERROR, "NETWORK_CALL_FAILURE_AFTER_MAX_RETRIES", new HashMap<String, Object>() {{
        put("extraData", endpoint);
        put("attempts", String.valueOf(attempts));
        put("err", errorMsg);
      }}, false);
    }
  }
}
