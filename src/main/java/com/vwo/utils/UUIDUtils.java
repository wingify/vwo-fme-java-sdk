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
package com.vwo.utils;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDUtils {

    // Define the DNS and URL namespaces for UUID v5
    private static final UUID DNS_NAMESPACE = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
    private static final UUID URL_NAMESPACE = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");

    // Define the SEED_URL constant
    private static final String SEED_URL = "https://vwo.com";

    /**
     * Generates a random UUID based on an API key.
     *
     * @param sdkKey The API key used to generate a namespace for the UUID.
     * @return A random UUID string.
     */
    public static String getRandomUUID(String sdkKey) {
        // Generate a namespace based on the API key using DNS namespace
        UUID namespace = generateUUID(sdkKey, DNS_NAMESPACE);
        // Generate a random UUID (UUIDv4)
        UUID randomUUID = UUID.randomUUID();
        // Generate a UUIDv5 using the random UUID and the namespace
        UUID uuidv5 = generateUUID(randomUUID.toString(), namespace);

        return uuidv5.toString();
    }

    /**
     * Generates a UUID for a user based on their userId and accountId.
     *
     * @param userId    The user's ID.
     * @param accountId The account ID associated with the user.
     * @return A UUID string formatted without dashes and in uppercase.
     */
    public static String getUUID(String userId, String accountId) {
        // Generate a namespace UUID based on SEED_URL using URL namespace
        UUID VWO_NAMESPACE = generateUUID(SEED_URL, URL_NAMESPACE);
        // Ensure userId and accountId are strings
        String userIdStr = (userId != null) ? userId : "";
        String accountIdStr = (accountId != null) ? accountId : "";
        // Generate a namespace UUID based on the accountId
        UUID userIdNamespace = generateUUID(accountIdStr, VWO_NAMESPACE);
        // Generate a UUID based on the userId and the previously generated namespace
        UUID uuidForUserIdAccountId = generateUUID(userIdStr, userIdNamespace);

        // Remove all dashes from the UUID and convert it to uppercase
        String desiredUuid = uuidForUserIdAccountId.toString().replaceAll("-", "").toUpperCase();
        return desiredUuid;
    }

    /**
     * Helper function to generate a UUID v5 based on a name and a namespace.
     *
     * @param name      The name from which to generate the UUID.
     * @param namespace The namespace used to generate the UUID.
     * @return A UUID.
     */
    public static UUID generateUUID(String name, UUID namespace) {
        if (name == null || namespace == null) {
            return null;
        }

        byte[] namespaceBytes = toBytes(namespace);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[namespaceBytes.length + nameBytes.length];
        System.arraycopy(namespaceBytes, 0, combined, 0, namespaceBytes.length);
        System.arraycopy(nameBytes, 0, combined, namespaceBytes.length, nameBytes.length);

        byte[] hash = Hashing.sha1().hashBytes(combined).asBytes();

        // Set version to 5 (name-based using SHA-1)
        hash[6] = (byte) (hash[6] & 0x0f); // Clear version
        hash[6] = (byte) (hash[6] | 0x50); // Set to version 5
        hash[8] = (byte) (hash[8] & 0x3f); // Clear variant
        hash[8] = (byte) (hash[8] | 0x80); // Set to IETF variant

        return fromBytes(hash);
    }

    /**
     * Helper function to convert a UUID to a byte array.
     *
     * @param uuid The UUID to convert.
     * @return A byte array.
     */
    private static byte[] toBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();

        // Convert the most significant bits and least significant bits to bytes
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (msb >>> (8 * (7 - i)));
            bytes[8 + i] = (byte) (lsb >>> (8 * (7 - i)));
        }

        return bytes;
    }

    /**
     * Helper function to convert a byte array to a UUID.
     *
     * @param bytes The byte array to convert.
     * @return A UUID.
     */
    private static UUID fromBytes(byte[] bytes) {
        long msb = 0;
        long lsb = 0;

        // Convert the most significant bits and least significant bits to longs
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
        }

        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (bytes[i] & 0xff);
        }

        return new UUID(msb, lsb);
    }
}