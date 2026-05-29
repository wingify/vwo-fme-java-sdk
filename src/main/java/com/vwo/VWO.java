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
package com.vwo;

import com.vwo.models.user.GetFlag;
import com.vwo.models.user.VWOInitOptions;
import com.wingify.Wingify;
import com.wingify.WingifyBuilder;
import com.wingify.models.user.WingifyUserContext;

/**
 * Backward-compatible entry point for the SDK.
 * Existing integrations using {@code com.vwo.VWO} continue to work without code changes.
 *
 * @deprecated Use {@link com.wingify.Wingify} instead.
 */
@Deprecated
public class VWO extends Wingify {

    VWO(String settings, WingifyBuilder wingifyBuilder) {
        super(settings, wingifyBuilder);
    }

    /**
     * Initializes the SDK using {@link VWOInitOptions}.
     *
     * @deprecated Use {@link com.wingify.Wingify#init(com.wingify.models.user.WingifyInitOptions)} instead.
     */
    @Deprecated
    public static VWO init(VWOInitOptions options) {
        options.setIsViaVWO(true);
        return (VWO) Wingify.init(options, VWO::new);
    }

    /**
     * Returns the feature flag result as {@link GetFlag} for backward compatibility.
     *
     * @deprecated Use {@link com.wingify.WingifyClient#getFlag(String, com.wingify.models.user.WingifyUserContext)} instead.
     */
    @Deprecated
    @Override
    public GetFlag getFlag(String featureKey, WingifyUserContext context) {
        return new GetFlag(super.getFlag(featureKey, context));
    }
}
