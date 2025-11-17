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
import com.vwo.enums.DebuggerCategoryEnum;
import com.vwo.services.SettingsManager;
import com.vwo.utils.DebuggerServiceUtil;
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
    private LogManager logManager;
    private SettingsManager settingsManager;

    /**
     * Overloaded method to log a message to the log manager
     * @param level The level of the message
     * @param key The key of the message
     * @param map The map of the message
     */
    public void log(LogLevelEnum level, String key, Map<String, Object> map) {
        log(level, key, map, true);
    }

    /**
     * Logs a message to the log manager
     * @param level The level of the message
     * @param key The key of the message
     * @param map The map of the message
     * @param shouldLogToVWO Whether to log to VWO
     */
    public void log(LogLevelEnum level, String key, Map<String, Object> map, Boolean shouldLogToVWO) {
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
                String message = buildMessage(errorMessages.get(key), map);
                logManager.error(message);
                if (shouldLogToVWO) {
                    errorLogToVWO(key, message, map);
                }
        }
    }

    /**
     * Logs a message to the log manager
     * @param level The level of the message
     * @param message The message to log
     */
    public void log(LogLevelEnum level, String message) {
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

    /**
     * Sets the settings manager
     * @param settingsManager The settings manager
     */
    public void setSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    /**
     * Constructor for the LoggerService
     * @param config The configuration for the logger
     */
    public LoggerService(Map<String, Object> config) {
        // initialize the LogManager
        logManager = new LogManager(config);

        // read the log files
        debugMessages = readLogFiles("debug-messages.json");
        infoMessages = readLogFiles("info-messages.json");
        errorMessages = readLogFiles("error-messages.json");
        warningMessages = readLogFiles("warn-messages.json");
        traceMessages = readLogFiles("trace-messages.json");
    }

    /**
     * Gets the log manager
     * @return The log manager
     */
    public LogManager getLogManager() {
        return logManager;
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

    /**
     * This method is used to send and error event to VWO.
     * @param template The template of the message.
     * @param debugProps The map of the debug props.
     */
    private void errorLogToVWO(String template, String message, Map<String, Object> debugProps) {
        debugProps.put("msg_t", template);
        debugProps.put("msg", message);
        debugProps.put("lt", LogLevelEnum.ERROR.toString());
        debugProps.put("cg", DebuggerCategoryEnum.ERROR.getValue());

        // send debug event to VWO
        DebuggerServiceUtil.sendDebugEventToVWO(settingsManager, debugProps);
    }
}