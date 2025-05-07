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
package com.vwo.packages.logger.transports;

import com.vwo.packages.logger.Logger;
import com.vwo.interfaces.logger.LogTransport;
import com.vwo.packages.logger.enums.LogLevelEnum;

public class ConsoleTransport extends Logger implements LogTransport {
  private LogLevelEnum level;

  public ConsoleTransport(LogLevelEnum level) {
    this.level = level;
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
    if (this.level.ordinal() <= level.ordinal()) {
      System.out.println(message);
    }
  }
}
