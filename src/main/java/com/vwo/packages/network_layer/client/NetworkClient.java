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
package com.vwo.packages.network_layer.client;

import com.vwo.VWOClient;
import com.vwo.interfaces.networking.NetworkClientInterface;
import com.vwo.packages.network_layer.models.RequestModel;
import com.vwo.packages.network_layer.models.ResponseModel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
   * Performs a GET request using the provided RequestModel.
   * @param requestModel The model containing request options.
   * @return A ResponseModel with the response data.
   */
  @Override
  public ResponseModel GET(RequestModel requestModel){
    ResponseModel responseModel = new ResponseModel();
    try {
      Map<String, Object> networkOptions = requestModel.getOptions();
      URL url = new URL(constructUrl(networkOptions));

      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      //connection.setConnectTimeout(5000);

      connection.connect();

      int statusCode = connection.getResponseCode();
      responseModel.setStatusCode(statusCode);

      String contentType = connection.getHeaderField("Content-Type");

      if (statusCode != 200 || !contentType.contains("application/json")) {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        String error = "Invalid response " + in.readLine()+ ", Status Code: " + statusCode + ", Response : " + connection.getResponseMessage();
        responseModel.setError(new Exception(error));
        return responseModel;
      }

      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuilder response = new StringBuilder();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      String responseData = response.toString();
      responseModel.setData(responseData);

      return responseModel;
    } catch (Exception exception) {
      responseModel.setError(exception);
      return responseModel;
    }
  }

  /**
   * Performs a POST request using the provided RequestModel.
   * @param request The model containing request options.
   * @return A ResponseModel with the response data.
   */
  @Override
  public ResponseModel POST(RequestModel request) {
    ResponseModel responseModel = new ResponseModel();
    try {
      Map<String, Object> networkOptions = request.getOptions();
      URL url = new URL(constructUrl(networkOptions));

      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      // set headers
      for (Map.Entry<String, Object> entry : networkOptions.entrySet()) {
        if (entry.getKey().equals("headers")) {
          Map<String, String> headers = (Map<String, String>) entry.getValue();
          for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
          }
        }
      }
      // connection.setConnectTimeout(5000);

      try (OutputStream os = connection.getOutputStream()) {
        Object body = networkOptions.get("body");

        if (body instanceof LinkedHashMap) {
          // Convert LinkedHashMap to JSON string
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

      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
      StringBuilder response = new StringBuilder();
      String inputLine;

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      String responseData = response.toString();
      responseModel.setData(responseData);

      if (statusCode != 200) {
        String error = "Request failed. Status Code: " + statusCode + ", Response: " + responseData;
        responseModel.setError(new Exception(error));
      }

      return responseModel;
    } catch (Exception exception) {
      responseModel.setError(exception);
      return responseModel;
    }
  }
}
