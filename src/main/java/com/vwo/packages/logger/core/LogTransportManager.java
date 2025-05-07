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
package com.vwo.packages.logger.core;

import com.vwo.interfaces.logger.LogTransport;
import com.vwo.packages.logger.LogMessageBuilder;
import com.vwo.packages.logger.Logger;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.logger.enums.LogLevelNumberEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogTransportManager extends Logger implements LogTransport {
    private List<Map<String, Object>> transports = new ArrayList<>();
    private Map<String, Object> config;

    public LogTransportManager(Map<String, Object> config) {
        this.config = config;
    }

    public void addTransport(Map<String, Object> transport) {
        transports.add(transport);
    }

    public boolean shouldLog(String transportLevel, Object configLevel) {
        String level;
        if (configLevel == null || configLevel.toString().isEmpty()) {
            level = this.config.getOrDefault("level", LogLevelEnum.ERROR.name()).toString();
        } else {
            level = configLevel.toString();
        }
        if (level == null || level.isEmpty()) {
            level = this.config.getOrDefault("level", LogLevelEnum.ERROR.name()).toString();
        }
        int targetLevel = LogLevelNumberEnum.valueOf(transportLevel.toUpperCase()).getLevel();
        int desiredLevel = LogLevelNumberEnum.valueOf(level.toUpperCase()).getLevel();
        return targetLevel >= desiredLevel;
    }

    @Override
    public void trace(String message) {
        log(LogLevelEnum.TRACE, message);
    }

    @Override
    public void debug(String message) {
        log(LogLevelEnum.DEBUG, message);
    }

    @Override
    public void info(String message) {
        log(LogLevelEnum.INFO, message);
    }

    @Override
    public void warn(String message) {
        log(LogLevelEnum.WARN, message);
    }

    @Override
    public void error(String message) {
        log(LogLevelEnum.ERROR, message);
    }

    @Override
    public void log(LogLevelEnum level, String message) {
        for (Map<String, Object> transport : transports) {
            LogMessageBuilder logMessageBuilder = new LogMessageBuilder(config, transport);
            String formattedMessage = logMessageBuilder.formatMessage(level, message);
            if (shouldLog(level.name(), transport.get("level"))) {
                Object logHandler = transport.get("log");
                // Check if log method is implemented or not
                if (logHandler instanceof LogTransport) {
                    ((LogTransport) logHandler).log(level, message);
                } else {
                    Object defaultLogHandler = transport.get("defaultTransport");
                    ((LogTransport) defaultLogHandler).log(level, formattedMessage);
                }
            }
        }
    }
}
