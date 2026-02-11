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
import com.vwo.enums.ApiEnum;
import com.vwo.models.Settings;
import com.vwo.models.schemas.SettingsSchema;
import com.vwo.models.user.VWOInitOptions;
import com.vwo.constants.Constants;
// import com.vwo.modules.logger.core.LogManager;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.network_layer.manager.NetworkManager;
import com.vwo.packages.network_layer.models.RequestModel;
import com.vwo.packages.network_layer.models.ResponseModel;
import com.vwo.utils.DebuggerServiceUtil;
import com.vwo.utils.NetworkUtil;

// public class SettingsManager implements ISettingsManager {
public class SettingsManager {
    public String sdkKey;
    public Integer accountId;
    public int expiry;
    public int networkTimeout;
    public String hostname = Constants.HOST_NAME;
    public int port;
    public String protocol = Constants.HTTPS_PROTOCOL;
    public boolean isGatewayServiceProvided = false;
    public boolean isSettingsValidOnInit = false;
    public Long settingsFetchTime;
    public LoggerService loggerService;
    public String collectionPrefix = "";
    public Boolean isProxyUrlProvided = false;
    public String proxyUrl = "";

    public SettingsManager(VWOInitOptions options, LoggerService loggerService) {
        this.loggerService = loggerService;
        this.sdkKey = options.getSdkKey();
        this.accountId = options.getAccountId();
        this.expiry = (int) Constants.SETTINGS_EXPIRY;
        this.networkTimeout = (int) Constants.SETTINGS_TIMEOUT;

        // check if proxy url is provided and gateway service is also provided
        if ((options.getProxyUrl() != null && !options.getProxyUrl().isEmpty()) && (options.getGatewayService() != null && !options.getGatewayService().isEmpty())) {
            loggerService.log(LogLevelEnum.INFO, "PROXY_AND_GATEWAY_SERVICE_PROVIDED", new HashMap<String, Object>() {{
                put("accountId", accountId.toString());
                put("sdkKey", sdkKey);
                put("an", ApiEnum.INIT.getValue());
            }});
            this.isGatewayServiceProvided = true;
        }

        // check if proxy url is provided and gateway service is not provided
        if (options.getProxyUrl() != null && !options.getProxyUrl().isEmpty() && !this.isGatewayServiceProvided) {
            this.isProxyUrlProvided = true;
            try {
                URL parsedUrl = new URL(options.getProxyUrl());
                this.hostname = parsedUrl.getHost();
                this.protocol = parsedUrl.getProtocol();
                if (parsedUrl.getPort() != -1){
                    this.port = parsedUrl.getPort();
                }
            } catch (Exception e) {
                loggerService.log(LogLevelEnum.ERROR, "ERROR_PARSING_PROXY_URL", new HashMap<String, Object>() {{
                    put("err", e.getMessage());
                    put("accountId", accountId.toString());
                    put("sdkKey", sdkKey);
                    put("an", ApiEnum.INIT.getValue());
                }});
                this.hostname = Constants.HOST_NAME;
            }
        }

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
                loggerService.log(LogLevelEnum.ERROR, "ERROR_PARSING_GATEWAY_URL", new HashMap<String, Object>() {{
                    put("err", e.getMessage());
                    put("accountId", accountId.toString());
                    put("sdkKey", sdkKey);
                    put("an", ApiEnum.INIT.getValue());
                }});
                this.hostname = Constants.HOST_NAME;
            }
        }
    }

    /**
     * Gets the settings fetch time
     * @return The settings fetch time in milliseconds
     */
    public Long getSettingsFetchTime() {
        return this.settingsFetchTime;
    }

    /**
     * Fetches settings from the server
     */
    private String fetchSettingsAndCacheInStorage() {
        try {
            return fetchSettings(false);
        } catch (Exception e) {
            loggerService.log(LogLevelEnum.ERROR, "ERROR_FETCHING_SETTINGS", new HashMap<String, Object>() {
                {
                    put("err", e.toString());
                    put("accountId", accountId.toString());
                    put("sdkKey", sdkKey);
                    put("an", ApiEnum.INIT.getValue());
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
        options.put("sn", Constants.SDK_NAME);
        options.put("sv", Constants.SDK_VERSION);

        if (!networkInstance.getConfig().getDevelopmentMode()) {
            options.put("s", "prod");
        }
        
        // if the webhook is triggered and the gateway service is not provided, use the v2-pull endpoint
        String endpoint = isViaWebhook && !isGatewayServiceProvided ? Constants.WEBHOOK_SETTINGS_ENDPOINT : Constants.SETTINGS_ENDPOINT;
        try {
            // Set fetch time
            long startTime = System.currentTimeMillis();

            RequestModel request = new RequestModel(hostname, "GET", endpoint, options, null, null, this.protocol, port);
            request.setTimeout(networkTimeout);
            request.setRetryConfig(networkInstance.getRetryConfig());

            ResponseModel response = networkInstance.get(request);
            String apiName = isViaWebhook ? ApiEnum.UPDATE_SETTINGS.getValue() : ApiEnum.INIT.getValue();
            
            // If attempt is more than 0, send debug event
            if (response.getTotalAttempts() > 0) {
                Map<String, Object> debugEventProps = DebuggerServiceUtil.createNetWorkAndRetryDebugEvent(
                    response,
                    null,
                    apiName,
                    endpoint
                );
                DebuggerServiceUtil.sendDebugEventToVWO(this, debugEventProps);
            }
            
            if (response.getStatusCode() != Constants.HTTP_OK){
                loggerService.log(LogLevelEnum.ERROR, "ERROR_FETCHING_SETTINGS", new HashMap<String, Object>() {
                    {
                        put("err", response.getError() != null ? response.getError().getMessage() : "Unknown error");
                        put("accountId", accountId.toString());
                        put("sdkKey", sdkKey);
                    }
                }, false);
                return null;
            }
            this.settingsFetchTime = System.currentTimeMillis() - startTime;
            return response.getData();
        } catch (Exception e) {
            loggerService.log(LogLevelEnum.ERROR, "ERROR_FETCHING_SETTINGS", new HashMap<String, Object>() {
                {
                    put("err", e.toString());
                    put("accountId", accountId.toString());
                    put("sdkKey", sdkKey);
                }
            }, false);
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
                    loggerService.log(LogLevelEnum.ERROR, "INVALID_SETTINGS_SCHEMA", new HashMap<String, Object>() {{
                        put("errors", "Settings is null");
                        put("accountId", accountId.toString());
                        put("sdkKey", sdkKey);
                        put("settings", "null");
                        put("an", forceFetch ? Constants.POLLING : ApiEnum.INIT.getValue());
                    }}, false);
                    return null;
                }
                SettingsSchema validationResult = new SettingsSchema().validateSettings(VWOClient.objectMapper.readValue(settings, Settings.class));
                if (validationResult.isValid()) {
                    this.isSettingsValidOnInit = true;
                    return settings;
                } else {
                    loggerService.log(LogLevelEnum.ERROR, "INVALID_SETTINGS_SCHEMA", new HashMap<String, Object>() {{
                        put("errors", validationResult.getErrorsAsString());
                        put("accountId", accountId.toString());
                        put("sdkKey", sdkKey);
                        put("settings", settings);
                        put("an", forceFetch ? Constants.POLLING : ApiEnum.INIT.getValue());
                    }});
                    return settings;
                }
            } catch (Exception e) {
                loggerService.log(LogLevelEnum.ERROR, "INVALID_SETTINGS_SCHEMA", new HashMap<String, Object>() {{
                    put("errors", "Exception during validation: " + e.getMessage());
                    put("accountId", accountId.toString());
                    put("sdkKey", sdkKey);
                    put("settings", "null");
                    put("an", forceFetch ? Constants.POLLING : ApiEnum.INIT.getValue());
                }});
                return null;
            }
        }
    }
}
