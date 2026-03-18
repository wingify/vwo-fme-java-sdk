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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vwo.VWO;
import com.vwo.VWOBuilder;
import com.vwo.VWOClient;
import com.vwo.models.Settings;
import com.vwo.models.user.GetFlag;
import com.vwo.models.user.VWOContext;
import com.vwo.models.user.VWOInitOptions;
import com.vwo.packages.storage.Storage;
import data.DummySettingsReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CustomBucketingSeedTest {
    private static final String SDK_KEY = "abcdef";
    private static final int ACCOUNT_ID = 123456;
    private static final String MOCK_SETTINGS_JSON = "{\n" +
            "    \"version\": 1,\n" +
            "    \"sdkKey\": \"abcdef\",\n" +
            "    \"accountId\": 123456,\n" +
            "    \"campaigns\": [\n" +
            "        {\n" +
            "            \"segments\": {},\n" +
            "            \"status\": \"RUNNING\",\n" +
            "            \"variations\": [\n" +
            "                {\n" +
            "                    \"weight\": 100,\n" +
            "                    \"segments\": {},\n" +
            "                    \"id\": 1,\n" +
            "                    \"variables\": [\n" +
            "                        {\n" +
            "                            \"id\": 1,\n" +
            "                            \"type\": \"string\",\n" +
            "                            \"value\": \"def\",\n" +
            "                            \"key\": \"kaus\"\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"name\": \"Rollout-rule-1\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"type\": \"FLAG_ROLLOUT\",\n" +
            "            \"isAlwaysCheckSegment\": false,\n" +
            "            \"isForcedVariationEnabled\": false,\n" +
            "            \"name\": \"featureOne : Rollout\",\n" +
            "            \"key\": \"featureOne_rolloutRule1\",\n" +
            "            \"id\": 1\n" +
            "        },\n" +
            "        {\n" +
            "            \"segments\": {},\n" +
            "            \"status\": \"RUNNING\",\n" +
            "            \"key\": \"featureOne_testingRule1\",\n" +
            "            \"type\": \"FLAG_TESTING\",\n" +
            "            \"isAlwaysCheckSegment\": false,\n" +
            "            \"name\": \"featureOne : Testing rule 1\",\n" +
            "            \"isForcedVariationEnabled\": true,\n" +
            "            \"variations\": [\n" +
            "                {\n" +
            "                    \"weight\": 50,\n" +
            "                    \"segments\": {},\n" +
            "                    \"id\": 1,\n" +
            "                    \"variables\": [\n" +
            "                        {\n" +
            "                            \"id\": 1,\n" +
            "                            \"type\": \"string\",\n" +
            "                            \"value\": \"def\",\n" +
            "                            \"key\": \"kaus\"\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"name\": \"Default\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"weight\": 50,\n" +
            "                    \"segments\": {},\n" +
            "                    \"id\": 2,\n" +
            "                    \"variables\": [\n" +
            "                        {\n" +
            "                            \"id\": 1,\n" +
            "                            \"type\": \"string\",\n" +
            "                            \"value\": \"var1\",\n" +
            "                            \"key\": \"kaus\"\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"name\": \"Variation-1\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"weight\": 0,\n" +
            "                    \"segments\": {\n" +
            "                        \"or\": [\n" +
            "                            {\n" +
            "                                \"user\": \"forcedWingify\"\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    },\n" +
            "                    \"id\": 3,\n" +
            "                    \"variables\": [\n" +
            "                        {\n" +
            "                            \"id\": 1,\n" +
            "                            \"type\": \"string\",\n" +
            "                            \"value\": \"var2\",\n" +
            "                            \"key\": \"kaus\"\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"name\": \"Variation-2\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"weight\": 0,\n" +
            "                    \"segments\": {},\n" +
            "                    \"id\": 4,\n" +
            "                    \"variables\": [\n" +
            "                        {\n" +
            "                            \"id\": 1,\n" +
            "                            \"type\": \"string\",\n" +
            "                            \"value\": \"var3\",\n" +
            "                            \"key\": \"kaus\"\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"name\": \"Variation-3\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"id\": 2,\n" +
            "            \"percentTraffic\": 100\n" +
            "        }\n" +
            "    ],\n" +
            "    \"features\": [\n" +
            "        {\n" +
            "            \"impactCampaign\": {},\n" +
            "            \"rules\": [\n" +
            "                {\n" +
            "                    \"campaignId\": 1,\n" +
            "                    \"type\": \"FLAG_ROLLOUT\",\n" +
            "                    \"ruleKey\": \"rolloutRule1\",\n" +
            "                    \"variationId\": 1\n" +
            "                },\n" +
            "                {\n" +
            "                    \"type\": \"FLAG_TESTING\",\n" +
            "                    \"ruleKey\": \"testingRule1\",\n" +
            "                    \"campaignId\": 2\n" +
            "                }\n" +
            "            ],\n" +
            "            \"status\": \"ON\",\n" +
            "            \"key\": \"featureOne\",\n" +
            "            \"metrics\": [\n" +
            "                {\n" +
            "                    \"type\": \"CUSTOM_GOAL\",\n" +
            "                    \"identifier\": \"e1\",\n" +
            "                    \"id\": 1\n" +
            "                }\n" +
            "            ],\n" +
            "            \"type\": \"FEATURE_FLAG\",\n" +
            "            \"name\": \"featureOne\",\n" +
            "            \"id\": 1\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    private Settings mockSettings;

    private final Map<String, String> settingsMap = new DummySettingsReader().settingsMap;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        mockSettings = VWOClient.objectMapper.readValue(MOCK_SETTINGS_JSON, Settings.class);
        // Reset the Storage singleton to prevent leakage from other test classes
        // (e.g., GetFlagTests attaches a StorageTest connector that persists on the singleton).
        // Without this, cached results from prior tests bypass seed-based bucketing.
        Storage.getInstance().attachConnector(null);
    }

    private VWO initVWO() {
        VWOInitOptions vwoInitOptions = new VWOInitOptions();
        vwoInitOptions.setSdkKey(SDK_KEY);
        vwoInitOptions.setAccountId(ACCOUNT_ID);
        vwoInitOptions.setLogger(new HashMap<>());

        VWOBuilder vwoBuilder = new VWOBuilder(vwoInitOptions);
        VWOBuilder vwoBuilderSpy = spy(vwoBuilder);

        doReturn(MOCK_SETTINGS_JSON).when(vwoBuilderSpy).getSettings(anyBoolean());

        vwoInitOptions.setVwoBuilder(vwoBuilderSpy);
        return VWO.init(vwoInitOptions);
    }

    private VWO initVWOWithSettings(String settingsJson) {
        VWOInitOptions vwoInitOptions = new VWOInitOptions();
        vwoInitOptions.setSdkKey(SDK_KEY);
        vwoInitOptions.setAccountId(ACCOUNT_ID);
        vwoInitOptions.setLogger(new HashMap<>());

        VWOBuilder vwoBuilder = new VWOBuilder(vwoInitOptions);
        VWOBuilder vwoBuilderSpy = spy(vwoBuilder);

        doReturn(settingsJson).when(vwoBuilderSpy).getSettings(anyBoolean());

        vwoInitOptions.setVwoBuilder(vwoBuilderSpy);
        return VWO.init(vwoInitOptions);
    }

    @Test
    public void testCase1_SeedProvided_SameVariation() {
        /**
         * Case 1: Seed Provided
         * Scenario: Two different users with the SAME bucketingSeed.
         * Expected: Both users get the SAME variation.
         */
        VWO vwoClient = initVWO();
        String sameSeed = "common-seed-123";

        VWOContext context1 = new VWOContext();
        context1.setId("KaustubhVWO");
        context1.setBucketingSeed(sameSeed);

        VWOContext context2 = new VWOContext();
        context2.setId("RandomUserVWO");
        context2.setBucketingSeed(sameSeed);

        GetFlag user1Flag = vwoClient.getFlag("featureOne", context1);
        GetFlag user2Flag = vwoClient.getFlag("featureOne", context2);

        assertEquals(user1Flag.getVariables().toString(), user2Flag.getVariables().toString(),
            "Users with same bucketingSeed should get same variation");
    }

    @Test
    public void testCase2_NoSeedProvided_FallbackToUserId() {
        /**
         * Case 2: No Seed Provided
         * Scenario: Two different users WITHOUT bucketingSeed.
         * Expected: Falls back to UserId, resulting in different variations.
         */
        VWO vwoClient = initVWO();

        VWOContext context1 = new VWOContext();
        context1.setId("KaustubhVWO");

        VWOContext context2 = new VWOContext();
        context2.setId("RandomUserVWO");

        GetFlag user1Flag = vwoClient.getFlag("featureOne", context1);
        GetFlag user2Flag = vwoClient.getFlag("featureOne", context2);

        assertNotEquals(user1Flag.getVariables().toString(), user2Flag.getVariables().toString(),
            "Users without bucketingSeed should fallback to userId and get different variations");
    }

    @Test
    public void testCase3_DifferentSeeds_DifferentVariations() {
        /**
         * Case 3: Different Seeds
         * Scenario: Same user with DIFFERENT bucketingSeeds.
         * Expected: Different variations.
         */
        VWO vwoClient = initVWO();

        VWOContext context1 = new VWOContext();
        context1.setId("sameId");
        context1.setBucketingSeed("KaustubhVWO");

        VWOContext context2 = new VWOContext();
        context2.setId("sameId");
        context2.setBucketingSeed("RandomUserVWO");

        GetFlag flag1 = vwoClient.getFlag("featureOne", context1);
        GetFlag flag2 = vwoClient.getFlag("featureOne", context2);

        assertNotEquals(flag1.getVariables().toString(), flag2.getVariables().toString(),
            "Different seeds should result in different variations");
    }

    @Test
    public void testCase4_EmptyStringSeed_FallbackToUserId() {
        /**
         * Case 4: Empty String Seed
         * Scenario: bucketingSeed is an empty string.
         * Expected: Falls back to UserId.
         */
        VWO vwoClient = initVWO();

        VWOContext context1 = new VWOContext();
        context1.setId("KaustubhVWO");
        context1.setBucketingSeed("");

        VWOContext context2 = new VWOContext();
        context2.setId("RandomUserVWO");
        context2.setBucketingSeed("");

        GetFlag user1Flag = vwoClient.getFlag("featureOne", context1);
        GetFlag user2Flag = vwoClient.getFlag("featureOne", context2);

        assertNotEquals(user1Flag.getVariables().toString(), user2Flag.getVariables().toString(),
            "Empty string seed should fallback to userId");
    }

    @Test
    public void testCase5_NullSeed_FallbackToUserId() {
        /**
         * Case 5: Null Seed
         * Scenario: bucketingSeed is explicitly null.
         * Expected: Falls back to UserId.
         */
        VWO vwoClient = initVWO();

        VWOContext context1 = new VWOContext();
        context1.setId("KaustubhVWO");
        context1.setBucketingSeed(null);

        VWOContext context2 = new VWOContext();
        context2.setId("RandomUserVWO");
        context2.setBucketingSeed(null);

        GetFlag user1Flag = vwoClient.getFlag("featureOne", context1);
        GetFlag user2Flag = vwoClient.getFlag("featureOne", context2);

        assertNotEquals(user1Flag.getVariables().toString(), user2Flag.getVariables().toString(),
            "Null seed should fallback to userId");
    }

    @Test
    public void testCase6_SameSalt_NoBucketingSeed_SameVariationPerUser() {
        /**
         * Case 6: No bucketing seed, custom salt present
         * Scenario: 10 users, both feature1 and feature2 have the SAME salt ("testingSalt").
         * Expected: Each user gets the same variation in both flags due to identical salt.
         */
        String sameSaltSettings = settingsMap.get("SETTINGS_WITH_SAME_SALT");
        VWO vwoClient = initVWOWithSettings(sameSaltSettings);

        for (int i = 1; i <= 10; i++) {
            String userId = "user" + i;
            VWOContext context = new VWOContext();
            context.setId(userId);

            GetFlag flag1 = vwoClient.getFlag("feature1", context);
            GetFlag flag2 = vwoClient.getFlag("feature2", context);

            assertEquals(flag1.getVariables().toString(), flag2.getVariables().toString(),
                "User " + userId + " should get the same variation for both flags when salt is identical");
        }
    }

    @Test
    public void testCase7_SameSalt_WithBucketingSeed_AllUsersSameVariation() {
        /**
         * Case 7: Same bucketing seed for all users, custom salt present
         * Scenario: 10 users all with the SAME bucketingSeed ("same-seed").
         * Expected: All users get the exact same variation (set of unique variations has size 1).
         */
        String sameSaltSettings = settingsMap.get("SETTINGS_WITH_SAME_SALT");
        VWO vwoClient = initVWOWithSettings(sameSaltSettings);

        Set<String> variationSet = new HashSet<>();

        for (int i = 1; i <= 10; i++) {
            String userId = "user" + i;
            VWOContext context = new VWOContext();
            context.setId(userId);
            context.setBucketingSeed("same-seed");

            GetFlag flag = vwoClient.getFlag("feature1", context);
            variationSet.add(flag.getVariables().toString());
        }
        //size of variation set should be 1
        assertEquals(1, variationSet.size(),
            "All users with the same bucketing seed should get the same variation");
    }

    @Test
    public void testCase8_ForcedVariation_WithBucketingSeed_WhitelistingTakesPriority() {
        /**
         * Case 8: Forced variation (whitelisting) with bucketing seed
         * Scenario: User "forcedWingify" is whitelisted to Variation-2 in MOCK_SETTINGS_FILE.
         * Even with a bucketingSeed provided, the forced variation should take priority.
         * Expected: User gets Variation-2 (value "var2") regardless of seed.
         */
        VWO vwoClient = initVWO();

        VWOContext context = new VWOContext();
        context.setId("forcedWingify");
        context.setBucketingSeed("any-seed-value");

        GetFlag flag = vwoClient.getFlag("featureOne", context);

        assertTrue(flag.isEnabled(), "Flag should be enabled for whitelisted user");
        assertEquals("var2", flag.getVariable("kaus", ""),
            "Whitelisted user should get Variation-2 (value 'var2') regardless of bucketing seed");
    }

}