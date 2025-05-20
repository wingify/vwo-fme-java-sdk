# VWO Feature Management and Experimentation SDK for Java

[![CI](https://github.com/wingify/vwo-fme-java-sdk/workflows/CI/badge.svg?branch=master)](https://github.com/wingify/vwo-fme-java-sdk/actions?query=workflow%3ACI)
[![codecov](https://codecov.io/gh/wingify/vwo-fme-java-sdk/branch/master/graph/badge.svg?token=WZ9LNISPPJ)](https://codecov.io/gh/wingify/vwo-fme-java-sdk)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

## Overview

The **VWO Feature Management and Experimentation SDK** (VWO FME Java SDK) enables java developers to integrate feature flagging and experimentation into their applications. This SDK provides full control over feature rollout, A/B testing, and event tracking, allowing teams to manage features dynamically and gain insights into user behavior.

## Requirements

The Java SDK supports:

* Open JDK - 8 onwards
* Oracle JDK - 8 onwards

Our [Build](https://github.com/wingify/vwo-fme-java-sdk/actions) is successful on these Java Versions -

## Installation

Install dependencies using `mvn install`

Add below Maven dependency in your project.


```java
<dependency>
    <groupId>com.vwo.sdk</groupId>
    <artifactId>vwo-fme-java-sdk</artifactId>
    <version>LATEST</version>
</dependency>
```

## Basic Usage Example

The following example demonstrates initializing the SDK with a VWO account ID and SDK key, setting a user context, checking if a feature flag is enabled, and tracking a custom event.
 ```java
 import com.vwo.VWO;
import com.vwo.models.user.VWOContext;
import com.vwo.models.user.GetFlag;
import com.vwo.models.user.VWOInitOptions;
import java.util.Map;

public class VWOExample {
    public static void main(String[] args) {
        // Initialize VWO SDK with your account details
        VWOInitOptions vwoInitOptions = new VWOInitOptions();
        vwoInitOptions.setSdkKey("32-alpha-numeric-sdk-key"); // Replace with your SDK key
        vwoInitOptions.setAccountId(123456); // Replace with your account ID

        // Initialize VWO instance
        VWO vwoInstance = VWO.init(vwoInitOptions);

        // Create user context
        VWOContext context = new VWOContext();
        context.setId("unique_user_id"); // Set a unique user identifier

        // Check if a feature flag is enabled
        GetFlag getFlag = vwoInstance.getFlag("feature_key", context);
        Boolean isFeatureEnabled = getFlag.isEnabled();
        System.out.println("Is feature enabled? " + isFeatureEnabled);

        // Get a variable value with a default fallback
        String variableValue = (String) getFlag.getVariable("feature_variable", "default_value");
        System.out.println("Variable value: " + variableValue);

        // Track a custom event
        Map<String, Boolean> trackResponse = vwoInstance.trackEvent("event_name", context);
        System.out.println("Event tracked: " + trackResponse);

        // Set multiple custom attributes 
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put("attribute-name", "attribute-value");
        vwoInstance.setAttribute(attributeMap, context);
    }
}
 ```

## Advanced Configuration Options

To customize the SDK further, additional parameters can be passed to the `init()` API using the `VWOInitOptions` object. Here’s a table describing each option:

| **Parameter**                | **Description**                                                                                                                                             | **Required** | **Type** | **Example**                     |
| ---------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------ | -------- | ------------------------------- |
| `setAccountId`                  | VWO Account ID for authentication.                                                                                                                          | Yes          | String   | `'123456'`                      |
| `setSdkKey`                     | SDK key corresponding to the specific environment to initialize the VWO SDK Client. You can get this key from VWO Application.                              | Yes          | String   | `'32-alpha-numeric-sdk-key'`    |
| `setPollInterval`               | Time interval for fetching updates from VWO servers (in milliseconds).                                                                                      | No           | Number   | `60000`                         |
| `setGatewayService`             | Configuration for integrating VWO Gateway Service. Service.                                                                                   | No           | Object   | see [Gateway](#gateway) section |
| `setStorage`                    | Custom storage connector for persisting user decisions and campaign data. data.                                                                                   | No           | Object   | See [Storage](#storage) section |
| `setLogger`                     | Toggle log levels for more insights or for debugging purposes. You can also customize your own transport in order to have better control over log messages. | No           | Object   | See [Logger](#logger) section   |
| `setIntegrations`               | Callback function for integrating with third-party analytics services.                                                                                      | No           | Function | See [Integrations](#integrations) section |

Refer to the [official VWO documentation](https://developers.vwo.com/v2/docs/fme-java-install) for additional parameter details.

### User Context

The `VWOContext` object uniquely identifies users and is crucial for consistent feature rollouts. A typical `VWOContext` includes an `id` for identifying the user, set via `setId()`. It can also include other attributes that can be used for targeting and segmentation, such as custom variables (set via `setCustomVariables()`), user agent (set via `setUserAgent()`) and IP address (set via `setIpAddress()`).

#### Parameters Table

The following table explains all the parameters in the `VWOContext` object:

| **Parameter**     | **Description**                                                            | **Required** | **Type** |
| ----------------- | -------------------------------------------------------------------------- | ------------ | -------- |
| `setId`              | Unique identifier for the user.                                            | Yes          | String   |
| `setCustomVariables` | Custom attributes for targeting.                                           | No           | Map<String, Object> |
| `setUserAgent`       | User agent string for identifying the user's browser and operating system. | No           | String   |
| `setIpAddress`       | IP address of the user.                                                    | No           | String   |

#### Example

```java
VWOContext context = new VWOContext();
context.setId("unique_user_id"); // Set a unique user identifier

// Create the map using HashMap in Java 8 and below
Map<String, Object> customVariables = new HashMap<>();
customVariables.put("age", 25);
customVariables.put("location", "US");
context.setCustomVariables(customVariables);

context.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
context.setIpAddress("1.1.1.1");
```

### Basic Feature Flagging

Feature Flags serve as the foundation for all testing, personalization, and rollout rules within FME.
To implement a feature flag, first use the `getFlag()` method to retrieve the flag configuration.
The `getFlag()` method provides a simple way to check if a feature is enabled for a specific user and access its variables. It returns a `GetFlag` object that contains methods like `isEnabled()` for checking the feature's status and `getVariable()` for retrieving any associated variables.

| Parameter    | Description                                                      | Required | Type        |
| ------------ | ---------------------------------------------------------------- | -------- | ----------- |
| `featureKey` | Unique identifier of the feature flag                            | Yes      | String      |
| `context`    | Object containing user identification and contextual information | Yes      | VWOContext  |

Example usage:

```java
GetFlag featureFlag = vwoInstance.getFlag("feature_key", context);
Boolean isEnabled = featureFlag.isEnabled();

if (isEnabled) {
  System.out.println("Feature is enabled!");

  // Get and use feature variable with type safety
  String variableValue = (String) featureFlag.getVariable('feature_variable', 'default_value');
  System.out.println("Variable value: " + variableValue);
} else {
  System.out.println("Feature is not enabled!");
}
```

### Custom Event Tracking

Feature flags can be enhanced with connected metrics to track key performance indicators (KPIs) for your features. These metrics help measure the effectiveness of your testing rules by comparing control versus variation performance, and evaluate the impact of personalization and rollout campaigns. Use the `trackEvent()` method to track custom events like conversions, user interactions, and other important metrics:

| Parameter         | Description                                                            | Required | Type        |
| ----------------- | ---------------------------------------------------------------------- | -------- | ----------- |
| `eventName`       | Name of the event you want to track                                    | Yes      | String      |
| `context`         | Object containing user identification and contextual information       | Yes      | VWOContext  |
| `eventProperties` | Additional properties/metadata associated with the event               | No       | Map<String, Object> |

Example usage:

```java
vwoInstance.trackEvent('event_name', context, eventProperties);
```

See [Tracking Conversions](https://developers.vwo.com/v2/docs/fme-java-metrics#usage) documentation for more information.

### Pushing Attributes

User attributes provide rich contextual information about users, enabling powerful personalization. The `setAttribute()` method in VWOClient provides a simple way to associate these attributes with users in VWO for advanced segmentation. The method accepts an attribute key, value, and VWOContext object containing the user information. Here's what you need to know about the method parameters:

| Parameter      | Description                                                            | Required | Type        |
|----------------|------------------------------------------------------------------------| -------- | ----------- |
| `attributeMap` | Multiple attributes you want to set for a user.                        | Yes      | String      |
| `context`      | Object containing user identification and other contextual information | Yes      | VWOContext  |

Example usage:

```java
Map<String, Object> attributeMap = new HashMap<>();
attributeMap.put("attribute-name", "attribute-value");
vwoInstance.setAttribute(attributeMap, context);
```

See [Pushing Attributes](https://developers.vwo.com/v2/docs/fme-java-attributes#usage) documentation for additional information.

### Polling Interval Adjustment

The `setPollInterval()` is an optional parameter that allows the SDK to automatically fetch and update settings from the VWO server at specified intervals. Setting this parameter ensures your application always uses the latest configuration.

```java
VWOInitOptions vwoInitOptions = new VWOInitOptions();
vwoInitOptions.setPollInterval(60000); // Set the poll interval to 60 seconds

VWO vwoInstance = VWO.init(vwoInitOptions);
```

### Gateway

The VWO FME Gateway Service is an optional but powerful component that enhances VWO's Feature Management and Experimentation (FME) SDKs. It acts as a critical intermediary for pre-segmentation capabilities based on user location and user agent (UA). By deploying this service within your infrastructure, you benefit from minimal latency and strengthened security for all FME operations.

#### Why Use a Gateway?

The Gateway Service is required in the following scenarios:

- When using pre-segmentation features based on user location or user agent.
- For applications requiring advanced targeting capabilities.
- It's mandatory when using any thin-client SDK (e.g., Go).

#### How to Use the Gateway

The gateway can be customized by passing the `setGatewayService()` parameter in the `init` configuration.

```java
VWOInitOptions vwoInitOptions = new VWOInitOptions();
vwoInitOptions.setAccountId(123456);
vwoInitOptions.setSdkKey("32-alpha-numeric-sdk-key");

Map<String, Object> gatewayService = new HashMap<>();
gatewayService.put("url", "http://custom.gateway.com");
vwoInitOptions.setGatewayService(gatewayService);
VWO vwoInstance = VWO.init(vwoInitOptions);
```

Refer to the [Gateway Documentation](https://developers.vwo.com/v2/docs/gateway-service) for further details.

### Storage

The SDK operates in a stateless mode by default, meaning each `getFlag` call triggers a fresh evaluation of the flag against the current user context.

To optimize performance and maintain consistency, you can implement a custom storage mechanism by passing a `setStorage()` parameter during initialization. This allows you to persist feature flag decisions in your preferred database system (like Redis, MongoDB, or any other data store).

Key benefits of implementing storage:

- Improved performance by caching decisions
- Consistent user experience across sessions
- Reduced load on your application

The storage mechanism ensures that once a decision is made for a user, it remains consistent even if campaign settings are modified in the VWO Application. This is particularly useful for maintaining a stable user experience during A/B tests and feature rollouts.

```java
  import com.vwo.packages.storage.Connector;
  import java.util.HashMap;
  import java.util.Map;
  public class StorageTest extends Connector {
  
  private final Map<String, Map<String, Object>> storage = new HashMap<>();

    @Override
    public void set(Map<String, Object> data) throws Exception {
        String key = data.get("featureKey") + "_" + data.get("userId");

        // Create a map to store the data
        Map<String, Object> value = new HashMap<>();
        value.put("rolloutKey", data.get("rolloutKey"));
        value.put("rolloutId", data.get("rolloutId"));
        value.put("rolloutVariationId", data.get("rolloutVariationId"));
        value.put("experimentKey", data.get("experimentKey"));
        value.put("experimentId", data.get("experimentId"));
        value.put("experimentVariationId", data.get("experimentVariationId"));

        // Store the value in the storage
        storage.put(key, value);
    }

    @Override
    public Object get(String featureKey, String userId) throws Exception {
        String key = featureKey + "_" + userId;

        // Check if the key exists in the storage
        if (storage.containsKey(key)) {
            return storage.get(key);
        }
        return null;
    }
  }
  ```

### Logger

VWO by default logs all `ERROR` level messages to your server console.
To gain more control over VWO's logging behaviour, you can use the `setLogger()` parameter in the `init` configuration.

| **Parameter** | **Description**                                      | **Required** | **Type** | **Default Value** |
| ------------- |------------------------------------------------------| ------------ |---------| ----------------- |
| `level`       | Log level to control verbosity of logs               | Yes          | String  | `ERROR`           |
| `prefix`      | Custom prefix for log messages                       | No           | String  | `VWO-SDK`             |
| `transport`   | Custom logger implementation for single transport    | No           | Map<String, Object>        | `null`            |
| `transports`  | Custom logger implementation for multiple transports | No           | List<Map<String, Object>>       | `null`            |
#### Example 1: Set log level to control verbosity of logs

```java
VWOInitOptions vwoInitOptions = new VWOInitOptions();
vwoInitOptions.setAccountId(123456);
vwoInitOptions.setSdkKey("32-alpha-numeric-sdk-key");

Map<String, Object> logger = new HashMap<>();
logger.put("level", "DEBUG");
vwoInitOptions.setLogger(logger);
VWO vwoInstance = VWO.init(vwoInitOptions);
```

#### Example 2: Add custom prefix to log messages for easier identification

```java
VWOInitOptions vwoInitOptions = new VWOInitOptions();
vwoInitOptions.setAccountId(123456);
vwoInitOptions.setSdkKey("32-alpha-numeric-sdk-key");

Map<String, Object> logger = new HashMap<>();
logger.put("level", "DEBUG");
logger.put("prefix", "CUSTOM LOG PREFIX");
vwoInitOptions.setLogger(logger);
VWO vwoInstance = VWO.init(vwoInitOptions);
```

#### Example 3: Implement custom transport to handle logs your way

The `transport` parameter allows you to implement custom logging behavior by providing your own logging functions. You can define handlers for different log levels (`debug`, `info`, `warn`, `error`, `trace`) to process log messages according to your needs.

For example, you could:

- Send logs to a third-party logging service
- Write logs to a file
- Format log messages differently
- Filter or transform log messages
- Route different log levels to different destinations

The transport object should implement handlers for the log levels you want to customize. Each handler receives the log message as a parameter.

For single transport you can use the `transport` parameter. For example:

```java
import com.vwo.interfaces.logger.LogTransport;
import com.vwo.packages.logger.enums.LogLevelEnum;

VWOInitOptions vwoInitOptions = new VWOInitOptions();
vwoInitOptions.setAccountId(123456);
vwoInitOptions.setSdkKey("32-alpha-numeric-sdk-key");

Map<String, Object> logger = new HashMap<>();
LogTransport logTransport = (level, message) -> {
    // your custom logging logic here
};
logger.put("transport", new HashMap<String, Object>() {{
    put("level", LogLevelEnum.DEBUG);
    put("log", logTransport);
}});

vwoInitOptions.setLogger(logger);
VWO vwoInstance = VWO.init(vwoInitOptions);
```

For multiple transports you can use the `transports` parameter. For example:
```java
import com.vwo.interfaces.logger.LogTransport;
import com.vwo.packages.logger.enums.LogLevelEnum;

VWOInitOptions vwoInitOptions = new VWOInitOptions();
vwoInitOptions.setAccountId(123456);
vwoInitOptions.setSdkKey("32-alpha-numeric-sdk-key");

Map<String, Object> logger = new HashMap<>();
List<Map<String, Object>> transports = new ArrayList<>();
LogTransport errorTransport = (level, message) -> {
    // your custom logging logic here
};
LogTransport infoTransport = (level, message) -> {
    // your custom logging logic here
};

transports.add(new HashMap<String, Object>() {{
    put("level", LogLevelEnum.INFO);
    put("log", infoTransport);
}});
transports.add(new HashMap<String, Object>() {{
    put("level", LogLevelEnum.ERROR);
    put("log", errorTransport);
}});
logger.put("transports", transports);

vwoInitOptions.setLogger(logger);
VWO vwoInstance = VWO.init(vwoInitOptions);
```
### Integrations
VWO FME SDKs provide seamless integration with third-party tools like analytics platforms, monitoring services, customer data platforms (CDPs), and messaging systems. This is achieved through a simple yet powerful callback mechanism that receives VWO-specific properties and can forward them to any third-party tool of your choice.

```java
import com.vwo.interfaces.integration.IntegrationCallback;

IntegrationCallback integrations = properties -> {
    // your function definition 
};

VWOInitOptions vwoInitOptions = new VWOInitOptions();
vwoInitOptions.setSdkKey("32-alpha-numeric-sdk-key");
vwoInitOptions.setAccountId(12345);
vwoInitOptions.setIntegrations(integrations);

VWO vwoInstance = VWO.init(vwoInitOptions);
```

Refer to the [Integrations](https://developers.vwo.com/v2/docs/fme-java-integrations) documentation for more information.

### Version History

The version history tracks changes, improvements, and bug fixes in each version. For a full history, see the [CHANGELOG.md](https://github.com/wingify/vwo-fme-java-sdk/blob/master/CHANGELOG.md).

## Contributing

We welcome contributions to improve this SDK! Please read our [contributing guidelines](https://github.com/wingify/vwo-fme-java-sdk/blob/master/CONTRIBUTING.md) before submitting a PR.

## Code of Conduct

Our [Code of Conduct](https://github.com/wingify/vwo-fme-java-sdk/blob/master/CODE_OF_CONDUCT.md) outlines expectations for all contributors and maintainers.

## License

[Apache License, Version 2.0](https://github.com/wingify/vwo-fme-java-sdk/blob/master/LICENSE)

Copyright 2024-2025 Wingify Software Pvt. Ltd.