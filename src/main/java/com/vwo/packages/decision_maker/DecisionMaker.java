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
package com.vwo.packages.decision_maker;

import com.github.eprst.murmur3.MurmurHash3;

public class DecisionMaker {

  private static final int SEED_VALUE = 1; // Seed value for the hash function
  public static final int MAX_TRAFFIC_VALUE = 10000; // Maximum traffic value used as a default scale
  public static final int MAX_CAMPAIGN_VALUE = 100;

  /**
   * Generates a bucket value for a user by hashing the user ID with murmurHash
   * and scaling it down to a specified maximum value.
   *
   * @param hashValue The hash value generated after hashing
   * @param maxValue The maximum value up to which the hash value needs to be scaled
   * @param multiplier Multiplier to adjust the scale in case the traffic allocation is less than 100
   * @return The bucket value of the user
   */
  public int generateBucketValue(long hashValue, int maxValue, int multiplier) {
    double ratio = (double) hashValue / Math.pow(2, 32); // Calculate the ratio of the hash value to the maximum hash value
    double multipliedValue = (maxValue * ratio + 1) * multiplier; // Apply the multiplier after scaling the hash value
    return (int) Math.floor(multipliedValue); // Floor the value to get an integer bucket value
  }

  public int generateBucketValue(long hashValue, int maxValue) {
    int multiplier = 1;
    double ratio = (double) hashValue / Math.pow(2, 32); // Calculate the ratio of the hash value to the maximum hash value
    double multipliedValue = (maxValue * ratio + 1) * multiplier; // Apply the multiplier after scaling the hash value
    return (int) Math.floor(multipliedValue); // Floor the value to get an integer bucket value
  }

  /**
   * Validates the user ID and generates a bucket value for the user by hashing the user ID with murmurHash
   * and scaling it down.
   *
   * @param userId The unique ID assigned to the user
   * @param maxValue The maximum value for bucket scaling (default is 100)
   * @return The bucket value allotted to the user (between 1 and maxValue)
   */
  public int getBucketValueForUser(String userId, int maxValue) {
    if (userId == null || userId.isEmpty()) {
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }
    long hashValue = generateHashValue(userId); // Generate the hash value using murmurHash
    return generateBucketValue(hashValue, maxValue, 1); // Generate the bucket value using the hash value (default multiplier)
  }

  public int getBucketValueForUser(String userId) {
    int maxValue = 100;
    if (userId == null || userId.isEmpty()) {
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }
    long hashValue = generateHashValue(userId); // Generate the hash value using murmurHash
    return generateBucketValue(hashValue, maxValue, 1); // Generate the bucket value using the hash value (default multiplier)
  }

  /**
   * Calculates the bucket value for a given string and optional multiplier and maximum value.
   *
   * @param str The string to hash
   * @param multiplier Multiplier to adjust the scale (default is 1)
   * @param maxValue Maximum value for bucket scaling (default is 10000)
   * @return The calculated bucket value
   */
  public int calculateBucketValue(String str, int multiplier, int maxValue) {
    long hashValue = generateHashValue(str); // Generate the hash value for the string

    return generateBucketValue(hashValue, maxValue, multiplier); // Generate and return the bucket value
  }

  public int calculateBucketValue(String str) {
    int multiplier = 1;
    int maxValue = 10000;
    long hashValue = generateHashValue(str); // Generate the hash value for the string

    return generateBucketValue(hashValue, maxValue, multiplier); // Generate and return the bucket value
  }

  /**
   * Generates a hash value for a given key using murmurHash.
   *
   * @param hashKey The key to hash
   * @return The generated hash value
   */
  public long generateHashValue(String hashKey) {
    /**
     * Took reference from StackOverflow (https://stackoverflow.com/) to:
     * Convert the int to unsigned long value
     * Author - Mysticial (https://stackoverflow.com/users/922184/mysticial)
     * Source - https://stackoverflow.com/questions/9578639/best-way-to-convert-a-signed-integer-to-an-unsigned-long
   */
    int murmurHash = MurmurHash3.murmurhash3_x86_32(hashKey.getBytes(), 0, hashKey.length(), SEED_VALUE);
    long signedMurmurHash = (murmurHash & 0xFFFFFFFFL);
    
    return signedMurmurHash;
  }
}
