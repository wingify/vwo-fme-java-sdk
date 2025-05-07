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
package com.vwo.packages.logger;

import com.vwo.interfaces.logger.LogTransport;
import com.vwo.packages.logger.enums.LogLevelEnum;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class LogMessageBuilder {
    private Map<String, Object> loggerConfig;
    private Map<String, Object> transport;
    private String prefix;
    private SimpleDateFormat dateTimeFormat;

    public LogMessageBuilder(Map<String, Object> loggerConfig, Map<String, Object> transport) {
        this.loggerConfig = loggerConfig;
        this.transport = transport;
        this.prefix = (String) this.transport.get("prefix");
        if (this.prefix == null || this.prefix.isEmpty()) {
            this.prefix = (String) loggerConfig.getOrDefault("prefix", "VWO-SDK");
        }
        this.dateTimeFormat = new SimpleDateFormat((String) loggerConfig.getOrDefault("dateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
    }

    public String formatMessage(LogLevelEnum level, String message) {
        return String.format("[%s]: %s %s %s", getFormattedLevel(level.name()), getFormattedPrefix(prefix), getFormattedDateTime(), message);
    }

    private String getFormattedPrefix(String prefix) {
        return String.format("%s%s%s", AnsiColorEnum.BOLD, AnsiColorEnum.GREEN, prefix + AnsiColorEnum.RESET);
    }

    private String getFormattedLevel(String level) {
        String upperCaseLevel = level.toUpperCase();
        switch (LogLevelEnum.valueOf(level.toUpperCase())) {
            case TRACE:
                return String.format("%s%s%s", AnsiColorEnum.BOLD, AnsiColorEnum.WHITE, upperCaseLevel + AnsiColorEnum.RESET);
            case DEBUG:
                return String.format("%s%s%s", AnsiColorEnum.BOLD, AnsiColorEnum.LIGHTBLUE, upperCaseLevel + AnsiColorEnum.RESET);
            case INFO:
                return String.format("%s%s%s", AnsiColorEnum.BOLD, AnsiColorEnum.CYAN, upperCaseLevel + AnsiColorEnum.RESET);
            case WARN:
                return String.format("%s%s%s", AnsiColorEnum.BOLD, AnsiColorEnum.YELLOW, upperCaseLevel + AnsiColorEnum.RESET);
            case ERROR:
                return String.format("%s%s%s", AnsiColorEnum.BOLD, AnsiColorEnum.RED, upperCaseLevel + AnsiColorEnum.RESET);
            default:
                return level;
        }
    }

    private String getFormattedDateTime() {
        return dateTimeFormat.format(new Date());
    }
}
