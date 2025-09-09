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

public class ResponseModel {

  private int statusCode;
  private Exception error;
  private Map<String, String> headers;
  private String data;
  private boolean isGzipped;

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public void setData(String data) {
    this.data = data;
  }

  public void setError(Exception error) {
    this.error = error;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public String getData() {
    return data;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public Exception getError() {
    return error;
  }

  public void setIsGzipped(boolean isGzipped) {
    this.isGzipped = isGzipped;
  }

  public boolean getIsGzipped() {
    return isGzipped;
  }
}
