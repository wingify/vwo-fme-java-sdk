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
package data.testCases;

import com.vwo.models.Storage;

import java.util.Map;

public class Expectation {
    private Boolean isEnabled;
    private Integer intVariable;
    private String stringVariable;
    private Double floatVariable;
    private Boolean booleanVariable;
    private Map<String, Object> jsonVariable;
    private Storage storageData;

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Integer getIntVariable() {
        return intVariable;
    }

    public void setIntVariable(Integer intVariable) {
        this.intVariable = intVariable;
    }

    public String getStringVariable() {
        return stringVariable;
    }

    public void setStringVariable(String stringVariable) {
        this.stringVariable = stringVariable;
    }

    public Double getFloatVariable() {
        return floatVariable;
    }

    public void setFloatVariable(Double floatVariable) {
        this.floatVariable = floatVariable;
    }

    public Boolean getBooleanVariable() {
        return booleanVariable;
    }

    public void setBooleanVariable(Boolean booleanVariable) {
        this.booleanVariable = booleanVariable;
    }

    public Map<String, Object> getJsonVariable() {
        return jsonVariable;
    }

    public void setJsonVariable(Map<String, Object> jsonVariable) {
        this.jsonVariable = jsonVariable;
    }

    public Storage getStorageData() {
        return storageData;
    }

    public void setStorageData(Storage storageData) {
        this.storageData = storageData;
    }
}
