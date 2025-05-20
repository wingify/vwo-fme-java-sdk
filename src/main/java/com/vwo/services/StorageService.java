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
package com.vwo.services;

import com.vwo.models.user.VWOContext;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.storage.Connector;
import com.vwo.packages.storage.Storage;

import java.util.HashMap;
import java.util.Map;

public class StorageService {

    /**
     * Retrieves data from storage based on the feature key and user ID.
     * @param featureKey The key to identify the feature data.
     * @param context The context model containing at least an ID.
     * @return The data retrieved or an error/storage status enum.
     */
    public Map<String, Object> getDataInStorage(String featureKey, VWOContext context) {
        Object storageInstance = Storage.getInstance().getConnector();
        if (storageInstance == null) {
            return null;
        }
        try {
            return  (Map<String, Object>) ((Connector) storageInstance).get(featureKey, context.getId());
        } catch (Exception e) {
            LoggerService.log(LogLevelEnum.ERROR, "STORED_DATA_ERROR", new HashMap<String, String>() {{
                put("err", e.toString());
            }});
            return null;
        }
    }

    /**
     * Stores data in the storage.
     * @param data The data to be stored as a map.
     * @return true if data is successfully stored, otherwise false.
     */
    public boolean setDataInStorage(Map<String, Object> data) {
        Object storageInstance = Storage.getInstance().getConnector();

        if (storageInstance == null) {
            return false;
        }
        try {
            ((Connector) storageInstance).set(data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
