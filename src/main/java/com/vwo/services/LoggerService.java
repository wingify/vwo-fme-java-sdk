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

import com.vwo.VWOClient;
import com.vwo.packages.logger.core.LogManager;
import com.vwo.packages.logger.enums.LogLevelEnum;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import static com.vwo.utils.LogMessageUtil.*;

public class LoggerService {
    public static Map<String, String> debugMessages;
    public static Map<String, String> errorMessages;
    public static Map<String, String> infoMessages;
    public static Map<String, String> warningMessages;
    public static Map<String, String> traceMessages;

    public static void log(LogLevelEnum level, String key, Map<String, String> map) {
        LogManager logManager = LogManager.getInstance();
        switch (level) {
            case DEBUG:
                logManager.debug(buildMessage(debugMessages.get(key), map));
                break;
            case INFO:
                logManager.info(buildMessage(infoMessages.get(key), map));
                break;
            case TRACE:
                logManager.trace(buildMessage(traceMessages.get(key), map));
                break;
            case WARN:
                logManager.warn(buildMessage(warningMessages.get(key), map));
                break;
            default:
                logManager.error(buildMessage(errorMessages.get(key), map));
        }
    }

    public static void log(LogLevelEnum level, String message) {
        LogManager logManager = LogManager.getInstance();
        switch (level) {
            case DEBUG:
                logManager.debug(message);
                break;
            case INFO:
                logManager.info(message);
                break;
            case TRACE:
                logManager.trace(message);
                break;
            case WARN:
                logManager.warn(message);
                break;
            default:
                logManager.error(message);
        }
    }

    public LoggerService(Map<String, Object> config) {
        // initialize the LogManager
        new LogManager(config);

        // read the log files
        debugMessages = readLogFiles("debug-messages.json");
        infoMessages = readLogFiles("info-messages.json");
        errorMessages = readLogFiles("error-messages.json");
        warningMessages = readLogFiles("warn-messages.json");
        traceMessages = readLogFiles("trace-messages.json");
    }

    /**
     * Reads the log files and returns the messages in a map.
     */
    private Map<String, String> readLogFiles(String fileName) {
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
            return VWOClient.objectMapper.readValue(inputStream, Map.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new HashMap<>();
    }
}
