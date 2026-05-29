/**
 * Copyright 2024-2026 Wingify Software Pvt. Ltd.
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
package com.wingify;

import com.wingify.packages.logger.enums.LogLevelEnum;
import com.wingify.models.user.WingifyInitOptions;
import com.wingify.utils.UUIDUtils;

import java.util.HashMap;
import java.util.function.BiFunction;

import static com.wingify.utils.LogMessageUtil.*;


public class Wingify extends WingifyClient {

    /**
     * Constructor for the Wingify class.
     * Initializes a new instance of Wingify with the provided options.
     * @param settings - Settings string for the Wingify instance.
     * @param wingifyBuilder - Wingify builder instance containing configuration options.
     */
    public Wingify(String settings, WingifyBuilder wingifyBuilder) {
        super(settings, wingifyBuilder);
    }

    /**
     * Sets the singleton instance of Wingify.
     * Configures and builds the Wingify instance using the provided options.
     * @param options - Configuration options for setting up Wingify.
     * @return A CompletableFuture resolving to the configured Wingify instance.
     */
    private static Wingify setInstance(WingifyInitOptions options, BiFunction<String, WingifyBuilder, Wingify> instanceCreator) {
        WingifyBuilder wingifyBuilder;
        if (options.getWingifyBuilder() != null) {
            wingifyBuilder = options.getWingifyBuilder();
        } else {
            wingifyBuilder = new WingifyBuilder(options);
        }
        wingifyBuilder
                .setLogger()           // Sets up logging for debugging and monitoring.
                .setSettingsManager()  // Sets the settings manager for configuration management.
                .setStorage()          // Configures storage for data persistence.
                .setNetworkManager()   // Configures network management for API communication.
                .initPolling()        // Initializes the polling mechanism for fetching settings.
                .initUsageStats();

        String settings =  wingifyBuilder.getSettings(false);
        wingifyBuilder.initBatching();
        Wingify wingifyInstance = instanceCreator.apply(settings, wingifyBuilder);
        // Set WingifyClient instance in WingifyBuilder
        wingifyBuilder.setWingifyClient(wingifyInstance);

        wingifyBuilder.getLoggerService().log(LogLevelEnum.INFO, "CLIENT_INITIALIZED", new HashMap<String, Object>() {
            {
                put("brand", getBrand(options.getIsViaVWO()));
            }
        });
        return wingifyInstance;
    }

    public static Wingify init(WingifyInitOptions options, BiFunction<String, WingifyBuilder, Wingify> instanceCreator) {
        validateInitOptions(options);
        long initStartTime = System.currentTimeMillis();
        Wingify instance = setInstance(options, instanceCreator);
        long initTime = System.currentTimeMillis() - initStartTime;
        instance.sendSdkInitAndUsageStatsEvent(initTime);
        return instance;
    }

    private static void validateInitOptions(WingifyInitOptions options) {
        Boolean isViaVWO = options != null ? options.getIsViaVWO() : false;

        if (options == null) {
            String message = buildMessage("[ERROR]: {brand}-SDK Options should be of type object", new HashMap<String, Object>() {
                {
                    put("brand", getBrand(isViaVWO));
                }
            });
            System.err.println(message);
        }

        if (options == null || options.getSdkKey() == null || options.getSdkKey().isEmpty()) {
            String message = buildMessage("[ERROR]: {brand}-SDK Please provide the sdkKey in the options and should be a of type string", new HashMap<String, Object>() {
                {
                    put("brand", getBrand(isViaVWO));
                }
            });
            System.err.println(message);
        }

        if (options == null || options.getAccountId() == null || options.getAccountId().toString().isEmpty()) {
            String message = buildMessage("[ERROR]: {brand}-SDK Please provide {brand} account ID in the options and should be a of type string|number", new HashMap<String, Object>() {
                {
                    put("brand", getBrand(isViaVWO));
                }
            });
            System.err.println(message);
        }

        if (options == null || options.getIsAliasingEnabled() && (options.getGatewayService() == null || options.getGatewayService().isEmpty())) {
            String message = buildMessage("[ERROR]: {brand}-SDK Please provide a valid gateway service url in the options when aliasing is enabled", new HashMap<String, Object>() {
                {
                    put("brand", getBrand(isViaVWO));
                }
            });
            System.err.println(message);
        }
    }

    public static Wingify init(WingifyInitOptions options) {
        return init(options, Wingify::new);
    }

    /**
     * Generate a deterministic UUID for a given user and account combination.
     *
     * @param userId    The user's ID (must be a non-empty string).
     * @param accountId The account ID (must be a non-empty string).
     * @return UUID without dashes in uppercase, or null on invalid input or error.
     */
    public static String getUUID(String userId, String accountId) {
        String apiName = "getUUID";
        try {
            // Validate userId
            if (userId == null || userId.isEmpty()) {
                System.out.println("userId passed to " + apiName + " API is not of valid type.");
                return null;
            }

            // Validate accountId
            if (accountId == null || accountId.isEmpty()) {
                System.out.println("accountId passed to " + apiName + " API is not of valid type.");
                return null;
            }

            // Call the UUID utility function
            return UUIDUtils.getUUID(userId, accountId);

        } catch (Exception error) {
            System.out.println("API - " + apiName + " failed to execute. Trace: " + error);
            return null;
        }
    }
}
