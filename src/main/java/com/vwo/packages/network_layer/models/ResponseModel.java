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
package com.vwo.packages.network_layer.models;

import java.util.Map;

public class ResponseModel {

  private int statusCode;
  private Object error;
  private Map<String, String> headers;
  private String data;

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public void setData(String data) {
    this.data = data;
  }

  public void setError(Object error) {
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

  public Object getError() {
    return error;
  }
}
