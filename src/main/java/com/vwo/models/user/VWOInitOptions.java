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
package com.vwo.models.user;

import com.wingify.WingifyBuilder;
import com.wingify.models.user.WingifyInitOptions;

import java.util.Map;

/**
 * Backward-compatible SDK initialization options for existing VWO integrations.
 *
 * @deprecated Use {@link com.wingify.models.user.WingifyInitOptions} instead.
 */
@Deprecated
public class VWOInitOptions extends WingifyInitOptions {

    /**
     * Sets the SDK builder instance.
     *
     * @deprecated Use {@link #setWingifyBuilder(WingifyBuilder)} instead.
     */
    @Deprecated
    public void setVwoBuilder(WingifyBuilder wingifyBuilder) {
        setWingifyBuilder(wingifyBuilder);
    }

    /**
     * Returns the SDK builder instance.
     *
     * @deprecated Use {@link #getWingifyBuilder()} instead.
     */
    @Deprecated
    public WingifyBuilder getVwoBuilder() {
        return getWingifyBuilder();
    }

    /**
     * Returns SDK metadata configured during initialization.
     *
     * @deprecated Use {@link #getWingifyMetaData()} instead.
     */
    @Deprecated
    public Map<String, Object> getVwoMetaData() {
        return getWingifyMetaData();
    }

    /**
     * Sets SDK metadata for initialization.
     *
     * @deprecated Use {@link #setWingifyMetaData(Map)} instead.
     */
    @Deprecated
    public void setVwoMetaData(Map<String, Object> wingifyMeta) {
        setWingifyMetaData(wingifyMeta);
    }
}
