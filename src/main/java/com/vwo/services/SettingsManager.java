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
import com.vwo.enums.DebuggerCategoryEnum;
import com.vwo.utils.DebuggerServiceUtil;
import com.vwo.utils.NetworkUtil;
import static com.vwo.utils.LogMessageUtil.buildMessage;

// public class SettingsManager implements ISettingsManager {
public class SettingsManager {
    public String sdkKey;
    public Integer accountId;
    public int expiry;
    public int networkTimeout;
    public String hostname;
    public int port;
    public String protocol = "https";
    public boolean isGatewayServiceProvided = false;
    public boolean isSettingsValidOnInit = false;
    public Long settingsFetchTime;
    public LoggerService loggerService;
    public String collectionPrefix = "";

    public SettingsManager(VWOInitOptions options, LoggerService loggerService) {
        this.loggerService = loggerService;
        this.sdkKey = options.getSdkKey();
        this.accountId = options.getAccountId();
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
                loggerService.log(LogLevelEnum.ERROR, "ERROR_PARSING_GATEWAY_URL", new HashMap<String, Object>() {{
                    put("err", e.getMessage());
                    put("accountId", accountId.toString());
                    put("sdkKey", sdkKey);
                    put("an", ApiEnum.INIT.getValue());
                }});
                this.hostname = Constants.HOST_NAME;
            }
        } else {
            this.hostname = Constants.HOST_NAME;
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

        String endpoint = isViaWebhook ? Constants.WEBHOOK_SETTINGS_ENDPOINT : Constants.SETTINGS_ENDPOINT;
        try {
            // Set fetch time
            long startTime = System.currentTimeMillis();

            RequestModel request = new RequestModel(hostname, "GET", endpoint, options, null, null, this.protocol, port);
            request.setTimeout(networkTimeout);

            ResponseModel response = networkInstance.get(request);
            if (response.getStatusCode() != 200){
                // create debug event props
                Map<String, Object> debugEventProps = new HashMap<String, Object>() {
                    {
                        put("cg", DebuggerCategoryEnum.NETWORK.getValue());
                        put("tRa", 0);
                        put("err", response.getError().getMessage());
                        put("an", isViaWebhook ? ApiEnum.UPDATE_SETTINGS.getValue() : ApiEnum.INIT.getValue());
                        put("msg_t", Constants.NETWORK_CALL_EXCEPTION);
                        put("sc", response.getStatusCode());
                        put("lt", LogLevelEnum.ERROR.toString());
                        put("msg", buildMessage(LoggerService.errorMessages.get("NETWORK_CALL_EXCEPTION"), new HashMap<String, Object>() {{
                            put("extraData", endpoint);
                            put("accountId", accountId.toString());
                            put("err", response.getError().getMessage());
                        }}));
                    }
                };
                // send debug event to VWO
                DebuggerServiceUtil.sendDebugEventToVWO(this, debugEventProps);
                loggerService.log(LogLevelEnum.ERROR, "ERROR_FETCHING_SETTINGS", new HashMap<String, Object>() {
                    {
                        put("err", response.getError().getMessage());
                        put("accountId", accountId.toString());
                        put("sdkKey", sdkKey);
                    }
                }, false);
                return null;
            }
            this.settingsFetchTime = System.currentTimeMillis() - startTime;
            return response.getData();
        } catch (Exception e) {
            // create debug event props
            Map<String, Object> debugEventProps = new HashMap<String, Object>() {
                {
                    put("cg", DebuggerCategoryEnum.NETWORK.getValue());
                    put("err", e.getMessage());
                    put("tRa", 0);
                    put("msg_t", Constants.NETWORK_CALL_EXCEPTION);
                    put("an", isViaWebhook ? ApiEnum.UPDATE_SETTINGS.getValue() : ApiEnum.INIT.getValue());
                    put("lt", LogLevelEnum.ERROR.toString());
                    put("msg", buildMessage(LoggerService.errorMessages.get("NETWORK_CALL_EXCEPTION"), new HashMap<String, Object>() {{
                        put("extraData", endpoint);
                        put("accountId", accountId.toString());
                        put("err", e.toString());
                    }}));
                }
            };
            // send debug event to VWO
            DebuggerServiceUtil.sendDebugEventToVWO(this, debugEventProps);
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
