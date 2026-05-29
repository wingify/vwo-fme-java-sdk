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
package com.vwo.packages.logger.enums;

/**
 * Backward-compatible log levels for existing VWO logger integrations.
 *
 * @deprecated Use {@link com.wingify.packages.logger.enums.LogLevelEnum} instead.
 */
@Deprecated
public enum LogLevelEnum {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    /**
     * Converts this VWO log level to the Wingify equivalent.
     *
     * @deprecated Use {@link com.wingify.packages.logger.enums.LogLevelEnum#valueOf(String)} instead.
     */
    @Deprecated
    public com.wingify.packages.logger.enums.LogLevelEnum toWingify() {
        return com.wingify.packages.logger.enums.LogLevelEnum.valueOf(this.name());
    }

    /**
     * Converts a Wingify log level to the VWO equivalent.
     *
     * @deprecated Use {@link com.wingify.packages.logger.enums.LogLevelEnum} directly instead.
     */
    @Deprecated
    public static LogLevelEnum fromWingify(com.wingify.packages.logger.enums.LogLevelEnum level) {
        return LogLevelEnum.valueOf(level.name());
    }
}
