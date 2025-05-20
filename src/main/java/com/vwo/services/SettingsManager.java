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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.vwo.VWOClient;
import com.vwo.models.Settings;
import com.vwo.models.schemas.SettingsSchema;
import com.vwo.models.user.VWOInitOptions;
import com.vwo.constants.Constants;
// import com.vwo.modules.logger.core.LogManager;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.network_layer.manager.NetworkManager;
import com.vwo.packages.network_layer.models.RequestModel;
import com.vwo.packages.network_layer.models.ResponseModel;
import com.vwo.utils.NetworkUtil;

// public class SettingsManager implements ISettingsManager {
public class SettingsManager {
    private String sdkKey;
    private Integer accountId;
    private int expiry;
    private int networkTimeout;
    public String hostname;
    public int port;
    public String protocol = "https";
    public boolean isGatewayServiceProvided = false;
    private static SettingsManager instance;

    public SettingsManager(VWOInitOptions options) {
        this.sdkKey = options.getSdkKey();
        this.accountId = options.getAccountId();
        // TODO -- check expiry logic
        this.expiry = (int) Constants.SETTINGS_EXPIRY;
        this.networkTimeout = (int) Constants.SETTINGS_TIMEOUT;

        if (options.getGatewayService() != null && !options.getGatewayService().isEmpty()) {
            isGatewayServiceProvided = true;
            try {
                URL parsedUrl;
                String gatewayServiceUrl = options.getGatewayService().get("url").toString();
                Object gatewayServiceProtocol = options.getGatewayService().get("protocol");
                Object gatewayServicePort = options.getGatewayService().get("port");
                if (gatewayServiceUrl.startsWith("http://") || gatewayServiceUrl.startsWith("https://")) {
                    parsedUrl = new URL(gatewayServiceUrl);
                } else if (gatewayServiceProtocol != null && !gatewayServiceProtocol.toString().isEmpty()) {
                    parsedUrl = new URL(gatewayServiceProtocol + "://" + gatewayServiceUrl);
                } else {
                    parsedUrl = new URL("https://" + gatewayServiceUrl);
                }
                this.hostname = parsedUrl.getHost();
                this.protocol = parsedUrl.getProtocol();
                if (parsedUrl.getPort() != -1){
                    this.port = parsedUrl.getPort();
                } else if (gatewayServicePort != null && !gatewayServicePort.toString().isEmpty()) {
                    this.port = Integer.parseInt(gatewayServicePort.toString());
                }
            } catch (Exception e) {
                LoggerService.log(LogLevelEnum.ERROR, "Error occurred while parsing gateway service URL: " + e.getMessage());
                this.hostname = Constants.HOST_NAME;
            }
        } else {
            this.hostname = Constants.HOST_NAME;
        }
        SettingsManager.instance = this;
    }

    public static SettingsManager getInstance() {
        return instance;
    }

    /**
     * Fetches settings from the server
     */
    private String fetchSettingsAndCacheInStorage() {
        try {
            return fetchSettings(false);
        } catch (Exception e) {
            LoggerService.log(LogLevelEnum.ERROR, "SETTINGS_FETCH_ERROR", new HashMap<String, String>() {
                {
                    put("err", e.toString());
                }
            });
        }
        return null;
    }

    /**
     * Fetches settings from the server
     * @return settings
     */
    public String fetchSettings(Boolean isViaWebhook) {
        if (sdkKey == null || accountId == null) {
            throw new IllegalArgumentException("SDK Key and Account ID are required to fetch settings. Aborting!");
        }

        NetworkManager networkInstance = NetworkManager.getInstance();
        Map<String, String> options = new NetworkUtil().getSettingsPath(sdkKey, accountId);
        options.put("api-version", "3");

        if (!networkInstance.getConfig().getDevelopmentMode()) {
            options.put("s", "prod");
        }

        String endpoint = Constants.SETTINGS_ENDPOINT;
        if (isViaWebhook) {
            endpoint = Constants.WEBHOOK_SETTINGS_ENDPOINT;
        }

        try {
            RequestModel request = new RequestModel(hostname, "GET", endpoint, options, null, null, this.protocol, port);
            request.setTimeout(networkTimeout);

            ResponseModel response = networkInstance.get(request);
            if (response.getStatusCode() != 200){
                LoggerService.log(LogLevelEnum.ERROR, "SETTINGS_FETCH_ERROR", new HashMap<String, String>() {
                    {
                        put("err", response.getError().toString());
                    }
                });
                return null;
            }
            return response.getData();
        } catch (Exception e) {
            LoggerService.log(LogLevelEnum.ERROR, "SETTINGS_FETCH_ERROR", new HashMap<String, String>() {
                {
                    put("err", e.toString());
                }
            });
            return null;
        }
    }

    /**
     * Fetches settings from the server
     * @param forceFetch forceFetch, if pooling - true, else - false
     * @return settings
     */
    public String getSettings(Boolean forceFetch) {
        if (forceFetch) {
            return fetchSettingsAndCacheInStorage();
        } else {
            try {
                String settings = fetchSettingsAndCacheInStorage();
                if (settings == null) {
                    LoggerService.log(LogLevelEnum.ERROR, "SETTINGS_SCHEMA_INVALID", null);
                    return null;
                }
                boolean settingsValid = new SettingsSchema().isSettingsValid(VWOClient.objectMapper.readValue(settings, Settings.class));
                if (settingsValid) {
                    return settings;
                } else {
                    LoggerService.log(LogLevelEnum.ERROR, "SETTINGS_SCHEMA_INVALID", null);
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }
    }
}
