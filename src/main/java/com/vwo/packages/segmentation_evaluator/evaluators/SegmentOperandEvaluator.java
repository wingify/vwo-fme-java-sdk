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
package com.vwo.packages.segmentation_evaluator.evaluators;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.vwo.enums.UrlEnum;
// import com.vwo.modules.logger.core.LogManager;
import com.vwo.models.user.VWOUserContext;
import com.vwo.packages.logger.enums.LogLevelEnum;
import com.vwo.packages.segmentation_evaluator.enums.SegmentOperandRegexEnum;
import com.vwo.packages.segmentation_evaluator.enums.SegmentOperandValueEnum;
import com.vwo.services.LoggerService;
import com.vwo.utils.GatewayServiceUtil;

import static com.vwo.packages.segmentation_evaluator.utils.SegmentUtil.getKeyValue;
import static com.vwo.packages.segmentation_evaluator.utils.SegmentUtil.matchWithRegex;
import static com.vwo.utils.DataTypeUtil.isBoolean;

public class SegmentOperandEvaluator {
    public Boolean evaluateCustomVariableDSL(JsonNode dslOperandValue, Map<String, Object> properties) {
        Map.Entry<String, JsonNode> entry = getKeyValue(dslOperandValue);
        String operandKey = entry.getKey();
        JsonNode operandValueNode = entry.getValue();
        String operandValue = operandValueNode.asText();

        // Check if the property exists
        if (!properties.containsKey(operandKey)) {
            return false;
        }

        // Handle 'inlist' operand
        if (operandValue.contains("inlist")) {
            Pattern listIdPattern = Pattern.compile("inlist\\(([^)]+)\\)");
            Matcher matcher = listIdPattern.matcher(operandValue);
            if (!matcher.find()) {
                LoggerService.log(LogLevelEnum.ERROR, "Invalid 'inList' operand format");
                return false;
            }
            String listId = matcher.group(1);
            // Process the tag value and prepare query parameters
            Object tagValue = properties.get(operandKey);
            String attributeValue = preProcessTagValue(tagValue.toString());
            Map<String, String> queryParamsObj = new HashMap<>();
            queryParamsObj.put("attribute", attributeValue);
            queryParamsObj.put("listId", listId);

            // Make a web service call to check the attribute against the list
            String gatewayServiceResponse = GatewayServiceUtil.getFromGatewayService(queryParamsObj, UrlEnum.ATTRIBUTE_CHECK.getUrl());
            if (gatewayServiceResponse == null) {
                return false;
            }
            return Boolean.parseBoolean(gatewayServiceResponse);
        } else {
            // Process other types of operands
            Object tagValue = properties.get(operandKey);
            if (tagValue == null) {
                tagValue = "";
            }
            tagValue = preProcessTagValue(tagValue.toString());
            Map<String, Object> preProcessOperandValue = preProcessOperandValue(operandValue);
            Map<String, Object> processedValues = processValues(preProcessOperandValue.get("operandValue"), tagValue);

            // Convert numeric values to strings if processing wildcard pattern
            SegmentOperandValueEnum operandType = (SegmentOperandValueEnum) preProcessOperandValue.get("operandType");
            if (operandType == SegmentOperandValueEnum.STARTING_ENDING_STAR_VALUE ||
                    operandType == SegmentOperandValueEnum.STARTING_STAR_VALUE ||
                    operandType == SegmentOperandValueEnum.ENDING_STAR_VALUE ||
                    operandType == SegmentOperandValueEnum.REGEX_VALUE) {
                processedValues.put("tagValue", processedValues.get("tagValue").toString());
            }

            tagValue = processedValues.get("tagValue");
            return extractResult(operandType, processedValues.get("operandValue").toString().trim().replace("\"", ""), tagValue.toString());
        }
    }

    public Map<String, Object> preProcessOperandValue(String operand) {
        SegmentOperandValueEnum operandType;
        String operandValue = null;

        if (matchWithRegex(operand, SegmentOperandRegexEnum.LOWER_MATCH.getRegex())) {
            operandType = SegmentOperandValueEnum.LOWER_VALUE;
            operandValue = extractOperandValue(operand, SegmentOperandRegexEnum.LOWER_MATCH.getRegex());
        } else if (matchWithRegex(operand, SegmentOperandRegexEnum.WILDCARD_MATCH.getRegex())) {
            operandValue = extractOperandValue(operand, SegmentOperandRegexEnum.WILDCARD_MATCH.getRegex());
            boolean startingStar = matchWithRegex(operandValue, SegmentOperandRegexEnum.STARTING_STAR.getRegex());
            boolean endingStar = matchWithRegex(operandValue, SegmentOperandRegexEnum.ENDING_STAR.getRegex());
            if (startingStar && endingStar) {
                operandType = SegmentOperandValueEnum.STARTING_ENDING_STAR_VALUE;
            } else if (startingStar) {
                operandType = SegmentOperandValueEnum.STARTING_STAR_VALUE;
            } else if (endingStar) {
                operandType = SegmentOperandValueEnum.ENDING_STAR_VALUE;
            } else {
                operandType = SegmentOperandValueEnum.REGEX_VALUE;
            }
            operandValue = operandValue.replaceAll(SegmentOperandRegexEnum.STARTING_STAR.getRegex(), "")
                    .replaceAll(SegmentOperandRegexEnum.ENDING_STAR.getRegex(), "");
        } else if (matchWithRegex(operand, SegmentOperandRegexEnum.REGEX_MATCH.getRegex())) {
            operandType = SegmentOperandValueEnum.REGEX_VALUE;
            operandValue = extractOperandValue(operand, SegmentOperandRegexEnum.REGEX_MATCH.getRegex());
        } else if (matchWithRegex(operand, SegmentOperandRegexEnum.GREATER_THAN_MATCH.getRegex())) {
            operandType = SegmentOperandValueEnum.GREATER_THAN_VALUE;
            operandValue = extractOperandValue(operand, SegmentOperandRegexEnum.GREATER_THAN_MATCH.getRegex());
        } else if (matchWithRegex(operand, SegmentOperandRegexEnum.GREATER_THAN_EQUAL_TO_MATCH.getRegex())) {
            operandType = SegmentOperandValueEnum.GREATER_THAN_EQUAL_TO_VALUE;
            operandValue = extractOperandValue(operand, SegmentOperandRegexEnum.GREATER_THAN_EQUAL_TO_MATCH.getRegex());
        } else if (matchWithRegex(operand, SegmentOperandRegexEnum.LESS_THAN_MATCH.getRegex())) {
            operandType = SegmentOperandValueEnum.LESS_THAN_VALUE;
            operandValue = extractOperandValue(operand, SegmentOperandRegexEnum.LESS_THAN_MATCH.getRegex());
        } else if (matchWithRegex(operand, SegmentOperandRegexEnum.LESS_THAN_EQUAL_TO_MATCH.getRegex())) {
            operandType = SegmentOperandValueEnum.LESS_THAN_EQUAL_TO_VALUE;
            operandValue = extractOperandValue(operand, SegmentOperandRegexEnum.LESS_THAN_EQUAL_TO_MATCH.getRegex());
        } else {
            operandType = SegmentOperandValueEnum.EQUAL_VALUE;
            operandValue = operand;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("operandType", operandType);
        result.put("operandValue", operandValue);
        return result;
    }

    public boolean evaluateUserDSL(String dslOperandValue, Map<String, Object> properties) {
        String[] users = dslOperandValue.split(",");
        for (String user : users) {
            if (user.trim().replace("\"", "").equals(properties.get("_vwoUserId"))) {
                return true;
            }
        }
        return false;
    }

    public boolean evaluateUserAgentDSL(String dslOperandValue, VWOUserContext context) {
        if (context == null || context.getUserAgent() == null) {
            //LogManager.getInstance().info("To Evaluate UserAgent segmentation, please provide userAgent in context");
            return false;
        }
        String tagValue = java.net.URLDecoder.decode(context.getUserAgent());
        Map<String, Object> preProcessOperandValue = preProcessOperandValue(dslOperandValue);
        Map<String, Object> processedValues = processValues(preProcessOperandValue.get("operandValue"), tagValue);
        tagValue = (String) processedValues.get("tagValue");
        SegmentOperandValueEnum operandType = (SegmentOperandValueEnum) preProcessOperandValue.get("operandType");
        return extractResult(operandType, processedValues.get("operandValue").toString().trim().replace("\"", ""), tagValue);
    }

    public String preProcessTagValue(String tagValue) {
        if (tagValue == null) {
            return "";
        }
        if (isBoolean(tagValue)) {
            return Boolean.toString(Boolean.parseBoolean(tagValue));
        }
        return tagValue.trim();
    }

    private Map<String, Object> processValues(Object operandValue, Object tagValue) {
        Map<String, Object> result = new HashMap<>();

        // Process operandValue
        result.put("operandValue", convertValue(operandValue));

        // Process tagValue
        result.put("tagValue", convertValue(tagValue));

        return result;
    }

    private String convertValue(Object value) {
        // Check if the value is a boolean
        if (value instanceof Boolean) {
            return value.toString(); // Convert boolean to "true" or "false"
        }

        try {
            // Attempt to convert to a numeric value
            double numericValue = Double.parseDouble(value.toString());
            // Check if the numeric value is actually an integer
            if (numericValue == (int) numericValue) {
                return String.valueOf((int) numericValue); // Remove '.0' by converting to int
            } else {
                // Format float to avoid scientific notation for large numbers
                DecimalFormat df = new DecimalFormat("#.##############"); // Adjust the pattern as needed
                return df.format(numericValue);
            }
        } catch (NumberFormatException e) {
            // Return the value as-is if it's not a number
            return value.toString();
        }
    }

    /**
     * Extracts the result of the evaluation based on the operand type and values.
     * @param operandType The type of the operand.
     * @param operandValue The value of the operand.
     * @param tagValue The value of the tag to compare against.
     * @return A boolean indicating the result of the evaluation.
     */
    public boolean extractResult(SegmentOperandValueEnum operandType, Object operandValue, String tagValue) {
        boolean result = false;

        switch (operandType) {
            case LOWER_VALUE:
                result = operandValue.toString().equalsIgnoreCase(tagValue);
                break;
            case STARTING_ENDING_STAR_VALUE:
                result = tagValue.contains(operandValue.toString());
                break;
            case STARTING_STAR_VALUE:
                result = tagValue.endsWith(operandValue.toString());
                break;
            case ENDING_STAR_VALUE:
                result = tagValue.startsWith(operandValue.toString());
                break;
            case REGEX_VALUE:
                try {
                    Pattern pattern = Pattern.compile(operandValue.toString());
                    Matcher matcher = pattern.matcher(tagValue);
                    result = matcher.matches();
                } catch (Exception e) {
                    result = false;
                }
                break;
            case GREATER_THAN_VALUE:
                result = Float.parseFloat(tagValue) > Float.parseFloat(operandValue.toString());
                break;
            case GREATER_THAN_EQUAL_TO_VALUE:
                result = Float.parseFloat(tagValue) >= Float.parseFloat(operandValue.toString());
                break;
            case LESS_THAN_VALUE:
                result = Float.parseFloat(tagValue) < Float.parseFloat(operandValue.toString());
                break;
            case LESS_THAN_EQUAL_TO_VALUE:
                result = Float.parseFloat(tagValue) <= Float.parseFloat(operandValue.toString());
                break;
            default:
                result = tagValue.equals(operandValue.toString());
        }

        return result;
    }

    /**
     * Extracts the operand value based on the provided regex pattern.
     *
     * @param operand The operand to be matched.
     * @param regex The regex pattern to match the operand against.
     * @return The extracted operand value or the original operand if no match is found.
     */
    public String extractOperandValue(String operand, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(operand);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return operand;
    }
}
