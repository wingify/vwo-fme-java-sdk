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
package com.vwo.models.user;

import com.vwo.models.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetFlag {
    private Boolean isEnabled = false;
    private List<Variable> variables = new ArrayList<>();

    public Boolean isEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public List<Variable> getVariablesValue() {
        return variables;
    }

    // get specific value from variables given key
    public Object getVariable(String key, Object defaultValue) {
        for (Variable variable : getVariablesValue()) {
            if (variable.getKey().equals(key)) {
                return variable.getValue();
            }
        }
        return defaultValue;
    }

    public List<Map<String, Object>> getVariables() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Variable variable : getVariablesValue()) {
            result.add(convertVariableModelToMap(variable));
        }
        return result;
    }

    private Map<String, Object> convertVariableModelToMap(Variable variableModel) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", variableModel.getKey());
        map.put("value", variableModel.getValue());
        map.put("type", variableModel.getType());
        map.put("id", variableModel.getId());
        return map;
    }
}
