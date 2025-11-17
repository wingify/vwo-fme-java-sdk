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
package com.vwo.constants;

public class Constants {

  public static final String PLATFORM = "server";

  public static final int MAX_TRAFFIC_PERCENT = 100;
  public static final int MAX_TRAFFIC_VALUE = 10000;
  public static final String STATUS_RUNNING = "RUNNING";

  public static final int SEED_VALUE = 1;
  public static final int MAX_EVENTS_PER_REQUEST = 5000;
  public static final int DEFAULT_REQUEST_TIME_INTERVAL = 600; // 10 * 60(secs) = 600 secs i.e. 10 minutes
  public static final int DEFAULT_EVENTS_PER_REQUEST = 100;
  public static final String SDK_NAME = "vwo-fme-java-sdk";
  public static final String SDK_VERSION = "1.13.0";
  public static final long SETTINGS_EXPIRY = 10000000;
  public static final long SETTINGS_TIMEOUT = 50000;

  public static final String HOST_NAME = "dev.visualwebsiteoptimizer.com";
  public static final String SETTINGS_ENDPOINT = "/server-side/v2-settings";
  public static final String WEBHOOK_SETTINGS_ENDPOINT = "/server-side/v2-pull";

  public static final String VWO_FS_ENVIRONMENT = "vwo_fs_environment";
  public static final String HTTPS_PROTOCOL = "https";

  public static final int RANDOM_ALGO = 1;
  public static final String VWO_META_MEG_KEY = "_vwo_meta_meg_";

  public static final int DEFAULT_POLL_INTERVAL = 600000; // 10 minutes
  public static final String FME = "fme";

  public static final String POLLING = "polling";
  public static final String FLAG_DECISION = "FLAG_DECISION";
  public static final String NETWORK_CALL_EXCEPTION = "NETWORK_CALL_EXCEPTION";

  public static final String IMPACT_ANALYSIS = "IMPACT_ANALYSIS";
}
