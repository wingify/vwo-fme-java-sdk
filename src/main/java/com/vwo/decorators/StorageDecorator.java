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
package com.vwo.decorators;

import com.vwo.interfaces.storage.IStorageDecorator;
import com.vwo.models.Variation;
import com.vwo.models.user.VWOContext;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.services.LoggerService;
import com.vwo.services.StorageService;

import java.util.HashMap;
import java.util.Map;

public class StorageDecorator implements IStorageDecorator {

    @Override
    public Map<String, Object> getFeatureFromStorage(String featureKey, VWOContext context, StorageService storageService) {
        return storageService.getDataInStorage(featureKey, context);
    }

    @Override
    public Variation setDataInStorage(Map<String, Object> data, StorageService storageService) {
        String featureKey = (String) data.get("featureKey");
        String userId = data.get("userId").toString();

        if (featureKey == null || featureKey.isEmpty()) {
            LoggerService.log(LogLevelEnum.ERROR, "STORING_DATA_ERROR", new HashMap<String, String>(){
                {
                    put("key", "featureKey");
                }
            });
            return null;
        }

        if (userId == null || userId.isEmpty()) {
            LoggerService.log(LogLevelEnum.ERROR, "STORING_DATA_ERROR", new HashMap<String, String>(){
                {
                    put("key", "Context or Context.id");
                }
            });
            return null;
        }

        String rolloutKey = (String) data.get("rolloutKey");
        String experimentKey = (String) data.get("experimentKey");
        Integer rolloutVariationId = (Integer) data.get("rolloutVariationId");
        Integer experimentVariationId = (Integer) data.get("experimentVariationId");

        if (rolloutKey != null && !rolloutKey.isEmpty() && experimentKey == null && rolloutVariationId == null) {
            LoggerService.log(LogLevelEnum.ERROR, "STORING_DATA_ERROR", new HashMap<String, String>(){
                {
                    put("key", "Variation:(rolloutKey, experimentKey or rolloutVariationId)");
                }
            });
            return null;
        }

        if (experimentKey != null && !experimentKey.isEmpty() && experimentVariationId == null) {
            LoggerService.log(LogLevelEnum.ERROR, "STORING_DATA_ERROR", new HashMap<String, String>(){
                {
                    put("key", "Variation:(experimentKey or rolloutVariationId)");
                }
            });
            return null;
        }

        storageService.setDataInStorage(data);

        return new Variation(); // Assuming you need to return a new VariationModel instance.
    }
}
