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

import com.vwo.models.user.VWOContext;

import java.util.ArrayList;

public class TestData {
    private String description;
    private String settings;
    private VWOContext context;
    private ArrayList<String> userIds;
    private Expectation expectation;
    private String featureKey;
    private String featureKey2;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public VWOContext getContext() {
        return context;
    }

    public void setContext(VWOContext context) {
        this.context = context;
    }

    public Expectation getExpectation() {
        return expectation;
    }

    public void setExpectation(Expectation expectation) {
        this.expectation = expectation;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }

    public String getFeatureKey2() {
        return featureKey2;
    }

    public void setFeatureKey2(String featureKey2) {
        this.featureKey2 = featureKey2;
    }

    public ArrayList<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(ArrayList<String> userIds) {
        this.userIds = userIds;
    }
}
