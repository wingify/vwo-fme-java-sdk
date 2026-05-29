/**
 * Copyright 2024-2026 Wingify Software Pvt. Ltd.
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
package com.wingify.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;

import com.wingify.constants.Constants;
import com.wingify.enums.EventEnum;
import com.wingify.services.SettingsManager;

public class LogMessageUtil {

    private static final Pattern NARGS = Pattern.compile("\\{([0-9a-zA-Z_]+)\\}");
    private static final String VWO_BRAND = "VWO";
    private static final String WINGIFY_BRAND = "Wingify";

    // create a map to store unique log messages
    private static Set<String> errorLogMessages = new HashSet<>();

    /**
     * Resolves the brand name based on whether the SDK is initialized via VWO.
     *
     * @param isViaVWO Whether the SDK is initialized via VWO.
     * @return "VWO" when isViaVWO is true, otherwise "Wingify".
     */
    public static String getBrand(Boolean isViaVWO) {
        return Boolean.TRUE.equals(isViaVWO) ? VWO_BRAND : WINGIFY_BRAND;
    }

    /**
     * Constructs a message by replacing placeholders in a template with corresponding values from a data object.
     *
     * @param template The message template containing placeholders in the format {key}.
     * @param data     An object containing keys and values used to replace the placeholders in the template.
     * @return The constructed message with all placeholders replaced by their corresponding values from the data object.
     */
    public static String buildMessage(String template, Map<String, Object> data) {
        if (template == null || data == null) {
            return template;
        }
        try {
            StringBuffer result = new StringBuffer();
            Matcher matcher = NARGS.matcher(template);
            while (matcher.find()) {
                String key = matcher.group(1);
                Object value = data.get(key);
                if (value != null) {
                    // If the value is not null, replace the placeholder with its value
                    matcher.appendReplacement(result, Matcher.quoteReplacement(value.toString()));
                }
            }
            matcher.appendTail(result);
            return result.toString();
        } catch (Exception e) {
            // Return the original template in case of an error
            return template;
        }
    }
}
