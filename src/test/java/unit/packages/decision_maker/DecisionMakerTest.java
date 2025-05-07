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
package unit.packages.decision_maker;

import com.vwo.packages.decision_maker.DecisionMaker;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.*;
public class DecisionMakerTest {

    private DecisionMaker decisionMaker;

    @BeforeEach
    public void setUp() {
        decisionMaker = new DecisionMaker();
    }

    @Test
    public void testGenerateBucketValue() {
        long hashValue = 2147483647; // Example hash value
        int maxValue = 100;
        int multiplier = 1;
        int expectedBucketValue = (int) Math.floor((maxValue * ((double) hashValue / Math.pow(2, 32)) + 1) * multiplier);
        int bucketValue = decisionMaker.generateBucketValue(hashValue, maxValue, multiplier);
        assertEquals(expectedBucketValue, bucketValue);
    }

    @Test
    public void testGetBucketValueForUser() {
        String userId = "user123";
        int maxValue = 100;
        long mockHashValue = 123456789; // Mocked hash value

        DecisionMaker spyDecisionMaker = spy(decisionMaker);
        when(spyDecisionMaker.generateHashValue(userId)).thenReturn(mockHashValue);

        int expectedBucketValue = spyDecisionMaker.generateBucketValue(mockHashValue, maxValue);
        int bucketValue = spyDecisionMaker.getBucketValueForUser(userId, maxValue);

        assertEquals(expectedBucketValue, bucketValue);
    }

    @Test
    public void testCalculateBucketValue() {
        String str = "testString";
        int multiplier = 1;
        int maxValue = 10000;
        long mockHashValue = 987654321; // Mocked hash value

        DecisionMaker spyDecisionMaker = spy(decisionMaker);
        when(spyDecisionMaker.generateHashValue(str)).thenReturn(mockHashValue);

        int expectedBucketValue = spyDecisionMaker.generateBucketValue(mockHashValue, maxValue, multiplier);
        int bucketValue = spyDecisionMaker.calculateBucketValue(str, multiplier, maxValue);

        assertEquals(expectedBucketValue, bucketValue);
    }

    @Test
    public void testGenerateHashValue() {
        String hashKey = "key123";
        long expectedHashValue = 2360047679L; // Expected hash value (this should be determined by actual hash function behavior)

        // Assuming you have a way to determine the expected hash value
        long hashValue = decisionMaker.generateHashValue(hashKey);
        assertEquals(expectedHashValue, hashValue);
    }
}
