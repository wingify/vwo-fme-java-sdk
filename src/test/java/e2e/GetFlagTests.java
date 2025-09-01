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
package e2e;

import com.vwo.VWO;
import com.vwo.VWOBuilder;
import com.vwo.VWOClient;
import com.vwo.models.Storage;
import com.vwo.models.user.GetFlag;
import com.vwo.models.user.VWOContext;
import com.vwo.models.user.VWOInitOptions;
import data.StorageTest;
import data.DummySettingsReader;
import data.testCases.TestCases;
import data.testCases.TestData;
import data.TestDataReader;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetFlagTests {
    private static final String SDK_KEY = "abcd";
    private static final int ACCOUNT_ID = 12345;

    private final Map<String, String> settingsMap = new DummySettingsReader().settingsMap;

    private final TestCases testCases = new TestDataReader().testCases;

    @Test
    @Order(1)
    public void testGetFlagWithoutStorage() {
        runTests(testCases.getGETFLAG_WITHOUT_STORAGE(), false);
    }

    @Test
    @Order(2)
    public void testGetFlagWithSalt() { runSaltTest(testCases.getGETFLAG_WITH_SALT());}

    @Test
    @Order(3)
    public void testGetFlagWithMegRandom() {
        runTests(testCases.getGETFLAG_MEG_RANDOM(), false);
    }

    @Test
    @Order(4)
    public void testGetFlagWithMegAdvance() {
        runTests(testCases.getGETFLAG_MEG_ADVANCE(), false);
    }

    @Test
    @Order(5)
    public void testGetFlagWithStorage() {
        runTests(testCases.getGETFLAG_WITH_STORAGE(), true);
    }

    private void runTests(List<TestData> tests, Boolean storageMap){
        for (TestData testData : tests) {
            StorageTest storage = new StorageTest();
            VWOInitOptions vwoInitOptions = new VWOInitOptions();
            vwoInitOptions.setSdkKey(SDK_KEY);
            vwoInitOptions.setAccountId(ACCOUNT_ID);
            if (storageMap) {
                vwoInitOptions.setStorage(storage);
            }

            VWOBuilder vwoBuilder = new VWOBuilder(vwoInitOptions);
            VWOBuilder vwoBuilderSpy = spy(vwoBuilder);

            when(vwoBuilderSpy.getSettings(false)).thenReturn(settingsMap.get(testData.getSettings()));

            vwoInitOptions.setVwoBuilder(vwoBuilderSpy);
            VWO vwoClient = VWO.init(vwoInitOptions);

            if (storageMap) {
                Map<String, Object> storageData = (Map<String, Object>) storage.get(testData.getFeatureKey(), testData.getContext().getId());
                assertNull(storageData);
            }

            GetFlag featureFlag = vwoClient.getFlag(testData.getFeatureKey(), testData.getContext());
            assertEquals(testData.getExpectation().getIsEnabled(), featureFlag.isEnabled());
            assertEquals((double) testData.getExpectation().getIntVariable(), Double.parseDouble(featureFlag.getVariable("int", 1).toString()));
            assertEquals(testData.getExpectation().getStringVariable(), featureFlag.getVariable("string", "VWO"));
            assertEquals(testData.getExpectation().getFloatVariable(), featureFlag.getVariable("float", 1.1));
            assertEquals(testData.getExpectation().getBooleanVariable(), featureFlag.getVariable("boolean", false));
            assertEquals(testData.getExpectation().getJsonVariable(), featureFlag.getVariable("json", new HashMap<>()));

            if (storageMap) {
                try {
                    if (testData.getExpectation().getIsEnabled()) {
                        Map<String, Object> updatedStorageData = (Map<String, Object>) storage.get(testData.getFeatureKey(), testData.getContext().getId());
                        String storageMapAsString = VWOClient.objectMapper.writeValueAsString(updatedStorageData);
                        Storage storedData = VWOClient.objectMapper.readValue(storageMapAsString, com.vwo.models.Storage.class);assertEquals(testData.getExpectation().getStorageData().getRolloutKey(), storedData.getRolloutKey());
                        assertEquals(testData.getExpectation().getStorageData().getRolloutVariationId(), storedData.getRolloutVariationId());
                        assertEquals(testData.getExpectation().getStorageData().getExperimentKey(), storedData.getExperimentKey());
                        assertEquals(testData.getExpectation().getStorageData().getExperimentVariationId(), storedData.getExperimentVariationId());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void runSaltTest(List<TestData> tests){
        for (TestData testData : tests) {
            VWOInitOptions vwoInitOptions = new VWOInitOptions();
            vwoInitOptions.setSdkKey(SDK_KEY);
            vwoInitOptions.setAccountId(ACCOUNT_ID);

            VWOBuilder vwoBuilder = new VWOBuilder(vwoInitOptions);
            VWOBuilder vwoBuilderSpy = spy(vwoBuilder);

            when(vwoBuilderSpy.getSettings(false)).thenReturn(settingsMap.get(testData.getSettings()));

            vwoInitOptions.setVwoBuilder(vwoBuilderSpy);
            VWO vwoClient = VWO.init(vwoInitOptions);

            ArrayList<String> userIds = testData.getUserIds();

            for (String userId : userIds) {
                VWOContext VWOContext = new VWOContext();
                VWOContext.setId(userId);
                GetFlag featureFlag = vwoClient.getFlag(testData.getFeatureKey(), VWOContext);
                GetFlag featureFlag2 = vwoClient.getFlag(testData.getFeatureKey2(), VWOContext);

                List<Map<String, Object>> featureFlagVariables = featureFlag.getVariables();
                List<Map<String, Object>> featureFlag2Variables = featureFlag2.getVariables();
                if (testData.getExpectation().getShouldReturnSameVariation()) {
                    assertEquals(featureFlagVariables, featureFlag2Variables, "The feature flag variables are not equal!");
                } else {
                    boolean areEqual = featureFlagVariables.equals(featureFlag2Variables);
                    assertFalse(areEqual, "The feature flag variables are equal!");
                }
            }
        }
    }
}
