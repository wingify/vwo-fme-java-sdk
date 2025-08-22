# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.11.1] - 2025-08-22

### Changed
- Improved error logging during settings polling for better clarity.

## [1.11.0] - 2025-08-16

### Added
- Added support for sending error logs to VWO server for better debugging.

### Fixed
- Fixed an issue where SDK version was not being read correctly from pom.xml file.

## [1.10.0] - 2025-08-04

### Added

- Added support for sending a one-time initialization event to the server to verify correct SDK setup.

## [1.9.2] - 2025-07-24

### Added

- Added the SDK name and version in the settings call to VWO as query parameters.


## [1.9.1] - 2025-07-07

### Fixed

- Fixed an issue where network calls for event batching were being sent synchronously; they are now sent asynchronously to improve performance and prevent blocking.

## [1.9.0] - 2025-07-02

### Added

- Added support for polling intervals to periodically fetch and update settings:
  - If `pollInterval` is set in options (must be >= 1000 milliseconds), that interval will be used
  - If `pollInterval` is configured in VWO application settings, that will be used
  - If neither is set, defaults to 10 minute polling interval

  Example usage:

  ```java
  VWOInitOptions vwoInitOptions = new VWOInitOptions();
  vwoInitOptions.setPollInterval(60000); // Set the poll interval to 60 seconds

  VWO vwoInstance = VWO.init(vwoInitOptions);
  ```

## [1.8.1] - 2025-05-21

### Added

- Added a feature to track and collect usage statistics related to various SDK features and configurations which can be useful for analytics, and gathering insights into how different features are being utilized by end users.

[1.8.0] - 2025-05-20

### Added
- added new method `updateSettings` to update settings on the vwo client instance.

## [1.7.0] - 2025-05-12

### Added

- Added support for `batchEventData` configuration to optimize network requests by batching multiple events together. This allows you to:

  - Configure `requestTimeInterval` to flush events after a specified time interval
  - Set `eventsPerRequest` to control maximum events per batch
  - Implement `flushCallback` to handle batch processing results
  - Manually trigger event flushing via `flushEvents()` method

  - You can also manually flush events using the `flushEvents()` method:

  ```java
   import com.vwo.VWO;
   import com.vwo.models.user.VWOContext;
   import com.vwo.models.user.GetFlag;
   import com.vwo.models.user.VWOInitOptions;

  FlushInterface flushCallback = new FlushInterface() {
    @Override
    public void onFlush(String error, String events) {
        System.out.println("Flush callback executed");
        // custom implementation here
    }
  };

   // Initialize VWO SDK
   VWOInitOptions vwoInitOptions = new VWOInitOptions();

   // Set SDK Key and Account ID
   vwoInitOptions.setSdkKey("sdk-key");
   vwoInitOptions.setAccountId(123);
   BatchEventData batchEventData = new BatchEventData();
   batchEventData.setEventsPerRequest(100);   // Send up to 100 events per request
   batchEventData.setRequestTimeInterval(60); // Flush events every 60 seconds
   batchEventData.setFlushCallback(flushCallback);

   // create VWO instance with the vwoInitOptions
   VWO vwoInstance = VWO.init(vwoInitOptions);

  ```
- You can also manually flush events using the `flushEvents()` method:
```java
  vwoInstance.flushEvents();
```

[1.6.0] - 2025-05-07

### Added

- Support for `Map` in `setAttribute` method to send multiple attributes data.
- Added support to add multiple `transports` in logger.

## [1.5.0] - 2025-03-19

### Changes

- changed uuid generation logic in `UUIDUtils` to generate similar uuid for a user across all vwo sdks.

[1.4.0] - 2024-12-20

### Added

- added support to use salt for bucketing if provided in the rule.

[1.3.0] - 2024-11-22

### Added

- Added support for Personalise rules within `Mutually Exclusive Groups`.

[1.2.0] - 2024-06-17

### Fixed

- Fixed segmentation evaluator issues where `contains` and `Greater than equal to` operator were not working as expected.
- Added unit test cases for the segmentation evaluator and decision maker

`[1.1.0] - 2024-06-07

### Fixed

Send event properties as third param in trackEvent() call`

[1.0.0] - 2024-05-31

### Added

- First release of VWO Feature Management and Experimentation capabilities.

  ```java
  import com.vwo.VWO;
  import com.vwo.models.user.VWOContext;
  import com.vwo.models.user.GetFlag;
  import com.vwo.models.user.VWOInitOptions;

  // Initialize VWO SDK
  VWOInitOptions vwoInitOptions = new VWOInitOptions();

  // Set SDK Key and Account ID
  vwoInitOptions.setSdkKey("sdk-key");
  vwoInitOptions.setAccountId(123);

  // create VWO instance with the vwoInitOptions
  VWO vwoInstance = VWO.init(vwoInitOptions);

  // Create VWOContext object
  VWOContext userContext = new VWOContext();
  // Set User ID
  userContext.setId("user-id");

  // Get the GetFlag object for the feature key and context
  GetFlag getFlag = vwoInstance.getFlag("feature-key", userContext);
  // Get the flag value
  Boolean isFlagEnabled = getFlag.isEnabled();

  // Get the variable value for the given variable key and default value
  Object variableValue = getFlag.getVariable("stringVar", "default-value");

  // Track the event for the given event name and context
  Map<String, Boolean> trackResponse = vwoInstance.trackEvent("event-name", userContext);

  // send attributes data
  vwoInstance.setAttribute("key", "value", userContext);
  ```

- **Storage**

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
        value.put("rolloutVariationId", data.get("rolloutVariationId"));
        value.put("experimentKey", data.get("experimentKey"));
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

- **Log messages**

  ```java
  Map<String, Object> logger = new HashMap<>();
  logger.put("level", "INFO");
  logger.put("prefix", "VWO");

  VWOInitOptions vwoInitOptions = new VWOInitOptions();

  vwInitOptions.setSdkKey("sdk-key");
  vwInitOptions.setAccountId(1234);
  vwoInitOptions.setLogger(logger);
  ```

- **Error handling**

  - Gracefully handle any kind of error - TypeError, NetworkError, etc.

- **Polling support**

  - Provide a way to fetch settings periodically and update the instance to use the latest settings

  ```java
  VWOInitOptions vwoInitOptions = new VWOInitOptions();

  vwInitOptions.setSdkKey("sdk-key");
  vwInitOptions.setAccountId(1234);
  vwInitOptions.setPollInterval(60);
  ```
