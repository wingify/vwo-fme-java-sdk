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
package unit.packages.settings;

import com.vwo.VWOClient;
import com.vwo.models.Settings;
import com.vwo.models.schemas.SettingsSchema;
import data.DummySettingsReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsSchemaValidationTest {
    
    private SettingsSchema settingsSchemaValidation;
    private Map<String, String> settingsMap;
    
    @BeforeEach
    public void setUp() {
        settingsMap = new DummySettingsReader().settingsMap;
        
        settingsSchemaValidation = new SettingsSchema();
    }
    
    @Test
    @DisplayName("Settings with wrong type for values should fail validation")
    public void testSettingsWithWrongTypeForValues() {
        try {
            String settingsJson = settingsMap.get("SETTINGS_WITH_WRONG_TYPE_FOR_VALUES");
            Settings settings = VWOClient.objectMapper.readValue(settingsJson, Settings.class);
            
            boolean result = settingsSchemaValidation.isSettingsValid(settings);
            assertFalse(result, "Settings with wrong type for values should fail validation");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Cannot deserialize value"), 
                "Expected deserialization error for wrong type, but got: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Settings with extra key at root level should not fail validation")
    public void testSettingsWithExtraKeyAtRootLevel() {
        try {
            String settingsJson = settingsMap.get("SETTINGS_WITH_EXTRA_KEYS_AT_ROOT_LEVEL");
            Settings settings = VWOClient.objectMapper.readValue(settingsJson, Settings.class);
            
            boolean result = settingsSchemaValidation.isSettingsValid(settings);
            assertTrue(result, "Settings with extra key at root level should not fail validation");
        } catch (Exception e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Settings with extra key inside objects should not fail validation")
    public void testSettingsWithExtraKeyInsideObjects() {
        try {
            String settingsJson = settingsMap.get("SETTINGS_WITH_EXTRA_KEYS_INSIDE_OBJECTS");
            Settings settings = VWOClient.objectMapper.readValue(settingsJson, Settings.class);
            
            boolean result = settingsSchemaValidation.isSettingsValid(settings);
            assertTrue(result, "Settings with extra key inside objects should not fail validation");
        } catch (Exception e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Settings with no features and campaigns should not fail validation")
    public void testSettingsWithNoFeaturesAndCampaigns() {
        try {
            String settingsJson = settingsMap.get("SETTINGS_WITH_NO_FEATURES_AND_CAMPAIGNS");
            Settings settings = VWOClient.objectMapper.readValue(settingsJson, Settings.class);
            
            boolean result = settingsSchemaValidation.isSettingsValid(settings);
            assertTrue(result, "Settings with no features and campaigns should not fail validation");
        } catch (Exception e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }
}
