# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

[1.1.0] - 2024-06-07

### Fixed
Send event properties as third param in trackEvent() call

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
   VWOContext context = new VWOContext();
   // Set User ID
   context.setId("user-id");
   
   // Get the GetFlag object for the feature key and context
   GetFlag getFlag = vwoInstance.getFlag("feature-key", context);
   // Get the flag value
   Boolean isFlagEnabled = getFlag.isEnabled();
   
   // Get the variable value for the given variable key and default value
   Object variableValue = getFlag.getVariable("stringVar", "default-value");
   
   // Track the event for the given event name and context
   Map<String, Boolean> trackResponse = vwoInstance.trackEvent("event-name", context);
   
   // send attributes data
   vwoInstance.setAttribute("key", "value", context);
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
        String key = data.get("featureKey") + "_" + data.get("user");

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
 