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
package com.vwo.packages.segmentation_evaluator.enums;

public enum SegmentOperatorValueEnum {
  AND("and"),
  NOT("not"),
  OR("or"),
  CUSTOM_VARIABLE("custom_variable"),
  USER("user"),
  COUNTRY("country"),
  REGION("region"),
  CITY("city"),
  OPERATING_SYSTEM("os"),
  DEVICE_TYPE("device_type"),
  BROWSER_AGENT("browser_string"),
  UA("ua"),
  DEVICE("device"),
  FEATURE_ID("featureId");

  private final String value;

  SegmentOperatorValueEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static SegmentOperatorValueEnum fromValue(String value) {
    for (SegmentOperatorValueEnum operator : SegmentOperatorValueEnum.values()) {
      if (operator.getValue().equals(value)) {
        return operator;
      }
    }
    throw new IllegalArgumentException("No enum constant with value " + value);
  }
}