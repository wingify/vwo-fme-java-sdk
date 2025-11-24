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

import com.vwo.ServiceContainer;
import com.vwo.services.LoggerService;

public class UserIdUtil {

    /**
     * Get the user id from the gateway service
     * @param userId The user id to get
     * @param serviceContainer The service container
     * @return The user id
     * @throws Exception If the gateway service is not provided when aliasing is enabled
     */
    public static String getUserId(String userId, ServiceContainer serviceContainer) throws Exception {
        if (serviceContainer.getSettingsManager().isGatewayServiceProvided) {
            String aliasId = AliasingUtil.getAlias(userId, serviceContainer);
            return aliasId;
        } else {
            throw new Exception(LogMessageUtil.buildMessage(LoggerService.errorMessages.get("INVALID_GATEWAY_SERVICE_WHEN_ALIASING_ENABLED"), null));
        }
    }
}
