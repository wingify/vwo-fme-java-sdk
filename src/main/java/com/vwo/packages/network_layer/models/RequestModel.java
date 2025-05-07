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
package com.vwo.packages.network_layer.models;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class RequestModel {

  private String url;
  private String method;
  private String scheme;
  private int port;
  private String path;
  private Map<String, String> query;
  private int timeout;
  private Map<String, Object> body;
  private Map<String, String> headers;

  public RequestModel(String url, String method, String path, Map<String, String> query, Map<String, Object> body, Map<String, String> headers, String scheme, int port) {
    this.url = url;
    this.method = method != null ? method : "GET";
    this.path = path;
    this.query = query;
    this.body = body;
    this.headers = headers;
    this.scheme = scheme != null ? scheme : "http";
    if (port != 0) {
        this.port = port;
    }
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Map<String, Object> getBody() {
    return body;
  }

  public void setBody(Map<String, Object> body) {
    this.body = body;
  }

  public Map<String, String> getQuery() {
    return query;
  }

  public void setQuery(Map<String, String> query) {
    this.query = query;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getScheme() {
    return scheme;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Map<String, Object> getOptions() {
    StringBuilder queryParams = new StringBuilder();
    for (String key : query.keySet()) {
      queryParams.append(key).append('=').append(query.get(key)).append('&');
    }

    Map<String, Object> options = new HashMap<>();
    options.put("hostname", url);
    options.put("agent", false);

    if (scheme != null) {
      options.put("scheme", scheme);
    }
    if (port != 80) {
      options.put("port", port);
    }
    if (headers != null) {
      options.put("headers", headers);
    }

    if (method != null) {
      options.put("method", method);
    }

    if (body != null) {
      String postBody = new Gson().toJson(body);
      headers.put("Content-Type", "application/json");
      headers.put("Content-Length", String.valueOf(postBody.getBytes().length));
      options.put("headers", headers);
      options.put("body", body);
    }

    if (path != null) {
      String combinedPath = path;
      if (queryParams.length() > 0) {
        combinedPath += "?" + queryParams.substring(0, queryParams.length() - 1);
      }
      options.put("path", combinedPath);
    }
    if (timeout > 0) {
      options.put("timeout", timeout);
    }

    return options;
  }
}
