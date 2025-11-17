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
package com.vwo.decorators;

import com.vwo.models.Variation;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.services.StorageService;
import com.vwo.ServiceContainer;

import java.util.HashMap;
import java.util.Map;

public class StorageDecorator {

    /**
     * Gets the feature from storage
     * @param featureKey The feature key
     * @param context The context
     * @param storageService The storage service
     * @param serviceContainer The service container
     * @return The feature
     */
    public Map<String, Object> getFeatureFromStorage(String featureKey, VWOContext context, StorageService storageService, ServiceContainer serviceContainer) {
        try {
            return storageService.getDataInStorage(featureKey, context);
        } catch (Exception e) {
            serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "ERROR_READING_DATA_FROM_STORAGE", new HashMap<String, Object>() {{
                put("err", e.toString());
                putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
            }});
            return null;
        }
    }

    /**
     * Sets the data in storage
     * @param data The data to be stored
     * @param storageService The storage service
     * @param serviceContainer The service container
     * @return The variation
     */
    public Variation setDataInStorage(Map<String, Object> data, StorageService storageService, ServiceContainer serviceContainer) {
        String featureKey = (String) data.get("featureKey");
        String userId = data.get("userId").toString();

        if (featureKey == null || featureKey.isEmpty()) {
            serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "STORING_DATA_ERROR", new HashMap<String, Object>(){
                {
                    put("key", "featureKey");
                    putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                }
            });
            return null;
        }

        if (userId == null || userId.isEmpty()) {
            serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "STORING_DATA_ERROR", new HashMap<String, Object>(){
                {
                    put("key", "Context or Context.id");
                    putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                }
            });
            return null;
        }

        String rolloutKey = (String) data.get("rolloutKey");
        String experimentKey = (String) data.get("experimentKey");
        Integer rolloutVariationId = (Integer) data.get("rolloutVariationId");
        Integer experimentVariationId = (Integer) data.get("experimentVariationId");

        if (rolloutKey != null && !rolloutKey.isEmpty() && experimentKey == null && rolloutVariationId == null) {
            serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "STORING_DATA_ERROR", new HashMap<String, Object>(){
                {
                    put("key", "Variation:(rolloutKey, experimentKey or rolloutVariationId)");
                    putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                }
            });
            return null;
        }

        if (experimentKey != null && !experimentKey.isEmpty() && experimentVariationId == null) {
            serviceContainer.getLoggerService().log(LogLevelEnum.ERROR, "STORING_DATA_ERROR", new HashMap<String, Object>(){
                {
                    put("key", "Variation:(experimentKey or rolloutVariationId)");
                    putAll(serviceContainer.getDebuggerService().getStandardDebugProps());
                }
            });
            return null;
        }

        storageService.setDataInStorage(data);

        return new Variation(); // Assuming you need to return a new VariationModel instance.
    }
}
