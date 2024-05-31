# VWO FME JAVA SDK

[![CI](https://github.com/wingify/vwo-fme-java-sdk/workflows/CI/badge.svg?branch=master)](https://github.com/wingify/vwo-fme-java-sdk/actions?query=workflow%3ACI)
[![codecov](https://codecov.io/gh/wingify/vwo-fme-java-sdk/branch/master/graph/badge.svg?token=WZ9LNISPPJ)](https://codecov.io/gh/wingify/vwo-fme-java-sdk)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

## Requirements

The Java SDK supports:

* Open JDK - 8 onwards
* Oracle JDK - 8 onwards

Our [Build](https://github.com/wingify/vwo-fme-java-sdk/actions) is successful on these Java Versions -

## SDK Installation

Install dependencies using `mvn install`

Add below Maven dependency in your project.


```java
<dependency>
    <groupId>com.vwo.sdk</groupId>
    <artifactId>vwo-fme-java-sdk</artifactId>
    <version>LATEST</version>
</dependency>
```

## Basic Usage
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

For more appenders, refer [this](https://logback.qos.ch/manual/appenders.html).

## Authors

* [Abhishek Joshi](https://github.com/Abhi591)
* [Rohitesh Dutta](https://github.com/rohitesh-wingify)

## Changelog

Refer [CHANGELOG.md](https://github.com/wingify/vwo-fme-java-sdk/blob/master/CHANGELOG.md)

## Contributing

Please go through our [contributing guidelines](https://github.com/wingify/vwo-fme-java-sdk/blob/master/CONTRIBUTING.md)

## Code of Conduct

[Code of Conduct](https://github.com/wingify/vwo-fme-java-sdk/blob/master/CODE_OF_CONDUCT.md)

## License

[Apache License, Version 2.0](https://github.com/wingify/vwo-fme-java-sdk/blob/master/LICENSE)

Copyright 2024 Wingify Software Pvt. Ltd.
