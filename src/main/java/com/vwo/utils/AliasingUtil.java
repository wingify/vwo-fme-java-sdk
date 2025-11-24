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

package com.vwo.utils;

import com.vwo.enums.UrlEnum;
import com.vwo.ServiceContainer;
import com.vwo.VWOClient;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.vwo.models.user.Alias;
import com.vwo.models.user.AliasSetResponse;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.services.LoggerService;

import static com.vwo.utils.LogMessageUtil.*;

public class AliasingUtil {
    private static String KEY_USER_ID = "userId";
    private static String KEY_ALIAS_ID = "aliasId";

    private static String GET_ALIAS_URL = UrlEnum.GET_ALIAS.getUrl();
    private static String SET_ALIAS_URL = UrlEnum.SET_ALIAS.getUrl();

    /**
     * Get the alias for a given userId
     * @param userId The userId to get the alias for
     * @param serviceContainer The service container
     * @return The userId if alias was not found, the alias for the given userId otherwise
     */
    public static String getAlias(String userId, ServiceContainer serviceContainer) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("accountId", serviceContainer.getSettingsManager().accountId.toString());
        params.put("sdkKey", serviceContainer.getSettingsManager().sdkKey);

        // gateway service expects userId as JSON array
        String userIdJSON = VWOClient.objectMapper.writeValueAsString(Collections.singletonList(userId));
        params.put(KEY_USER_ID, userIdJSON);

        String response = GatewayServiceUtil.getFromGatewayService(serviceContainer, params, GET_ALIAS_URL);
        if (response == null) {
            throw new Exception(buildMessage(LoggerService.errorMessages.get("ERROR_GETTING_ALIAS"), new HashMap<String, Object>() {{
                put("userId", userId);
                put("err", "Response is null");
            }}));
        }

        List<Alias> aliasList = VWOClient.objectMapper.readValue(response, new TypeReference<List<Alias>>() {});
            Optional<Alias> result = aliasList.stream()
            .filter(item -> item.getAliasId().equals(userId))
            .findFirst();
    
        if (result.isPresent()) {
            return result.get().getUserId();
        } else {
            return userId;
        }
    }

    /**
     * Set the alias for a given userId
     * @param userId The userId to set the alias for
     * @param aliasId The alias to set for the given userId
     * @param serviceContainer The service container
     * @return True if alias was set successfully, false otherwise
     */
    public static Boolean setAlias(String userId, String aliasId, ServiceContainer serviceContainer) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("accountId", serviceContainer.getSettingsManager().accountId.toString());
        params.put("sdkKey", serviceContainer.getSettingsManager().sdkKey);
        params.put(KEY_USER_ID, userId);
        params.put(KEY_ALIAS_ID, aliasId);

        Map<String, Object> payload = new HashMap<>();
        payload.put(KEY_USER_ID, userId);
        payload.put(KEY_ALIAS_ID, aliasId);

        String response = GatewayServiceUtil.postToGatewayService(serviceContainer, params, payload, SET_ALIAS_URL);
        if (response == null) {
            throw new Exception(buildMessage(LoggerService.errorMessages.get("ERROR_SETTING_ALIAS"), new HashMap<String, Object>() {{
                put("userId", userId);
            }}));
        }

        // Parse the JSON response
        AliasSetResponse aliasSetResponse = VWOClient.objectMapper.readValue(response, AliasSetResponse.class);
        
        if (aliasSetResponse.isAliasSet()) {
            return true;
        } else {
            throw new Exception(buildMessage(LoggerService.errorMessages.get("ERROR_SETTING_ALIAS"), new HashMap<String, Object>() {{
                put("userId", userId);
            }}));
        }
    }
}