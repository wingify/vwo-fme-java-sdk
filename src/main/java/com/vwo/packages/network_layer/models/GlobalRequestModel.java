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

import java.util.Map;

public class GlobalRequestModel {

  private String url;
  private int timeout = 3000;
  private Map<String, Object> query;
  private Map<String, Object> body;
  private Map<String, String> headers;
  private boolean isDevelopmentMode;

  public GlobalRequestModel(String url, Map<String, Object> query, Map<String, Object> body, Map<String, String> headers) {
    this.url = url;
    this.query = query;
    this.body = body;
    this.headers = headers;
  }

  public void setQuery(Map<String, Object> query) {
    this.query = query;
  }

  public Map<String, Object> getQuery() {
    return query;
  }

  public void setBody(Map<String, Object> body) {
    this.body = body;
  }

  public Map<String, Object> getBody() {
    return body;
  }

  public void setBaseUrl(String url) {
    this.url = url;
  }

  public String getBaseUrl() {
    return url;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setDevelopmentMode(boolean isDevelopmentMode) {
    this.isDevelopmentMode = isDevelopmentMode;
  }

  public boolean getDevelopmentMode() {
    return isDevelopmentMode;
  }
}
