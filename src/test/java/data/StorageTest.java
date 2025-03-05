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
package data;

import com.vwo.packages.storage.Connector;

import java.util.HashMap;
import java.util.Map;

public class StorageTest extends Connector {

    private final Map<String, Map<String, Object>> storage = new HashMap<>();

    @Override
    public void set(Map<String, Object> data) {
        String key = data.get("featureKey") + "_" + data.get("userId");

        // Create a map to store the data
        Map<String, Object> value = new HashMap<>();
        value.put("rolloutKey", data.get("rolloutKey"));
        value.put("rolloutVariationId", data.get("rolloutVariationId"));
        value.put("experimentKey", data.get("experimentKey"));
        value.put("experimentVariationId", data.get("experimentVariationId"));

        // Store the value in the storage
        storage.put(key, value);
    }

    @Override
    public Object get(String featureKey, String userId){
        String key = featureKey + "_" + userId;

        // Check if the key exists in the storage
        if (storage.containsKey(key)) {
            return storage.get(key);
        }
        return null;
    }
}
