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

import com.vwo.interfaces.logger.ILogManager;
import com.vwo.interfaces.logger.LogTransport;
import com.vwo.packages.logger.Logger;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.logger.transports.ConsoleTransport;

import java.text.SimpleDateFormat;
import java.util.*;

public class LogManager extends Logger implements ILogManager {
  private static LogManager instance;
  private LogTransportManager transportManager;
  private Map<String, Object> config;
  private String name;
  private String requestId;
  private LogLevelEnum level;
  private String prefix;
  private SimpleDateFormat dateTimeFormat;
  private List<Map<String, Object>> transports = new ArrayList<>();

  public LogManager(Map<String, Object> config) {
    this.config = config;
    this.name = (String) config.getOrDefault("name", "VWO Logger");
    this.requestId = UUID.randomUUID().toString();
    this.level = LogLevelEnum.valueOf(config.getOrDefault("level", LogLevelEnum.ERROR.name()).toString().toUpperCase());
    this.prefix = (String) config.getOrDefault("prefix", "VWO-SDK");
    this.dateTimeFormat = new SimpleDateFormat((String) config.getOrDefault("dateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

    this.transportManager = new LogTransportManager(config);
    handleTransports();
    LogManager.instance = this;
  }

  public static LogManager getInstance() {
    return instance;
  }

  private void handleTransports() {
    List<Map<String, Object>> transportList = (List<Map<String, Object>>) config.get("transports");
    Map<String, Object> transport = (Map<String, Object>) config.get("transport");
    if (transportList != null && !transportList.isEmpty()) {
      addTransports(transportList);
    } else if (transport != null && !transport.isEmpty()) {
      addTransport(transport);
    } else {
      ConsoleTransport defaultTransport = new ConsoleTransport(level);
      Map<String, Object> defaultTransportMap = new HashMap<>();
        defaultTransportMap.put("defaultTransport", defaultTransport);
      addTransport(defaultTransportMap);
    }
  }

  public void addTransport(Map<String, Object> transport) {
    transportManager.addTransport(transport);
  }

  public void addTransports(List<Map<String, Object>> transportList) {
    for (Map<String, Object> transport : transportList) {
      addTransport(transport);
    }
  }

  @Override
  public LogTransportManager getTransportManager() {
    return transportManager;
  }

  @Override
  public Map<String, Object> getConfig() {
    return config;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getRequestId() {
    return requestId;
  }

  @Override
  public LogLevelEnum getLevel() {
    return level;
  }

  @Override
  public String getPrefix() {
    return prefix;
  }

  @Override
  public String getDateTimeFormat() {
    return dateTimeFormat.toPattern();
  }

  @Override
  public Map<String, Object> getTransport() {
    // This method needs more context, currently returning null.
    return null;
  }

  @Override
  public List<Map<String, Object>> getTransports() {
    return transports;
  }

  @Override
  public void trace(String message) {
    transportManager.trace(message);
  }

  @Override
  public void debug(String message) {
    transportManager.debug(message);
  }

  @Override
  public void info(String message) {
    transportManager.info(message);
  }

  @Override
  public void warn(String message) {
    transportManager.warn(message);
  }

  @Override
  public void error(String message) {
    transportManager.error(message);
  }
}
