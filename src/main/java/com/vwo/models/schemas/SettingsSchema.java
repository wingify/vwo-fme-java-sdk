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
package com.vwo.models.schemas;

import com.vwo.models.*;
import java.util.ArrayList;
import java.util.List;

public class SettingsSchema {

    private boolean valid;
    private List<String> errors;

    public SettingsSchema() {
        this.valid = true;
        this.errors = new ArrayList<>();
    }

    public SettingsSchema(boolean valid) {
        this.valid = valid;
        this.errors = new ArrayList<>();
    }

    /**
     * Returns if the settings are valid
     * @return boolean value indicating if the settings are valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the validity of the settings
     * @param valid boolean value indicating if the settings are valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Returns the errors in the settings
     * @return list of errors
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Adds an error to the settings
     * @param error error message
     */
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }

    /**
     * Returns the errors as a string
     * @return string of errors
     */
    public String getErrorsAsString() {
        return String.join("; ", errors);
    }

    /**
     * Returns if the settings are valid
     * @param settings settings to be validated
     * @return boolean value indicating if the settings are valid
     */
    public boolean isSettingsValid(Settings settings) {
        return validateSettings(settings).isValid();
    }

    /**
     * Validates the settings
     * @param settings settings to be validated
     * @return SettingsSchema object containing the validation result
     */
    public SettingsSchema validateSettings(Settings settings) {
        SettingsSchema result = new SettingsSchema();
        try {
            if (settings == null) {
                result.addError("Settings object is null");
                return result;
            }

            // Validate SettingsModel fields
            if (settings.getVersion() == null) {
                result.addError("Settings version is null");
            }
            
            if (settings.getAccountId() == null) {
                result.addError("Settings accountId is null");
            }

            if (settings.getCampaigns() == null) {
                result.addError("Settings campaigns list is null");
            } else if (settings.getCampaigns().isEmpty()) {
                result.addError("Settings campaigns list is empty");
            } else {
                for (int i = 0; i < settings.getCampaigns().size(); i++) {
                    Campaign campaign = settings.getCampaigns().get(i);
                    SettingsSchema campaignResult = validateCampaign(campaign, i);
                    if (!campaignResult.isValid()) {
                        result.getErrors().addAll(campaignResult.getErrors());
                        result.setValid(false);
                    }
                }
            }

            if (settings.getFeatures() != null) {
                for (int i = 0; i < settings.getFeatures().size(); i++) {
                    Feature feature = settings.getFeatures().get(i);
                    SettingsSchema featureResult = validateFeature(feature, i);
                    if (!featureResult.isValid()) {
                        result.getErrors().addAll(featureResult.getErrors());
                        result.setValid(false);
                    }
                }
            }
        } catch (Exception e) {
            result.addError("Error validating settings: " + e.getMessage());
            result.setValid(false);
            return result;
        }

        return result;
    }

    /**
     * Validates the campaign
     * @param campaign campaign to be validated
     * @param index index of the campaign
     * @return SettingsSchema object containing the validation result
     */
    private SettingsSchema validateCampaign(Campaign campaign, int index) {
        SettingsSchema result = new SettingsSchema();
        String prefix = "Campaign[" + index + "]: ";
        
        if (campaign == null) {
            result.addError(prefix + "Campaign object is null");
            return result;
        }
        
        if (campaign.getId() == null) {
            result.addError(prefix + "Campaign id is null");
        }
        
        if (campaign.getType() == null) {
            result.addError(prefix + "Campaign type is null");
        }
        
        if (campaign.getKey() == null) {
            result.addError(prefix + "Campaign key is null");
        }
        
        if (campaign.getStatus() == null) {
            result.addError(prefix + "Campaign status is null");
        }
        
        if (campaign.getName() == null) {
            result.addError(prefix + "Campaign name is null");
        }

        if (campaign.getVariations() == null) {
            result.addError(prefix + "Campaign variations list is null");
        } else if (campaign.getVariations().isEmpty()) {
            result.addError(prefix + "Campaign variations list is empty");
        } else {
            for (int i = 0; i < campaign.getVariations().size(); i++) {
                Variation variation = campaign.getVariations().get(i);
                SettingsSchema variationResult = validateCampaignVariation(variation, index, i);
                if (!variationResult.isValid()) {
                    result.getErrors().addAll(variationResult.getErrors());
                    result.setValid(false);
                }
            }
        }

        return result;
    }

    /**
     * Validates the campaign variation
     * @param variation variation to be validated
     * @param campaignIndex index of the campaign
     * @param variationIndex index of the variation
     * @return SettingsSchema object containing the validation result
     */
    private SettingsSchema validateCampaignVariation(Variation variation, int campaignIndex, int variationIndex) {
        SettingsSchema result = new SettingsSchema();
        String prefix = "Campaign[" + campaignIndex + "].Variation[" + variationIndex + "]: ";
        
        if (variation == null) {
            result.addError(prefix + "Variation object is null");
            return result;
        }
        
        if (variation.getId() == null) {
            result.addError(prefix + "Variation id is null");
        }
        
        if (variation.getName() == null) {
            result.addError(prefix + "Variation name is null");
        }
        
        if (String.valueOf(variation.getWeight()).isEmpty()) {
            result.addError(prefix + "Variation weight is empty");
        }

        if (variation.getVariables() != null) {
            for (int i = 0; i < variation.getVariables().size(); i++) {
                Variable variable = variation.getVariables().get(i);
                SettingsSchema variableResult = validateVariableObject(variable, "Campaign[" + campaignIndex + "].Variation[" + variationIndex + "].Variable[" + i + "]");
                if (!variableResult.isValid()) {
                    result.getErrors().addAll(variableResult.getErrors());
                    result.setValid(false);
                }
            }
        }

        return result;
    }

    /**
     * Validates the variable object
     * @param variable variable to be validated
     * @param context context of the variable
     * @return SettingsSchema object containing the validation result
     */
    private SettingsSchema validateVariableObject(Variable variable, String context) {
        SettingsSchema result = new SettingsSchema();
        String prefix = context + ": ";
        
        if (variable == null) {
            result.addError(prefix + "Variable object is null");
            return result;
        }
        
        if (variable.getId() == null) {
            result.addError(prefix + "Variable id is null");
        }
        
        if (variable.getType() == null) {
            result.addError(prefix + "Variable type is null");
        }
        
        if (variable.getKey() == null) {
            result.addError(prefix + "Variable key is null");
        }
        
        if (variable.getValue() == null) {
            result.addError(prefix + "Variable value is null");
        }

        return result;
    }

    /**
     * Validates the feature
     * @param feature feature to be validated
     * @param index index of the feature
     * @return SettingsSchema object containing the validation result
     */
    private SettingsSchema validateFeature(Feature feature, int index) {
        SettingsSchema result = new SettingsSchema();
        String prefix = "Feature[" + index + "]: ";
        
        if (feature == null) {
            result.addError(prefix + "Feature object is null");
            return result;
        }
        
        if (feature.getId() == null) {
            result.addError(prefix + "Feature id is null");
        }
        
        if (feature.getKey() == null) {
            result.addError(prefix + "Feature key is null");
        }
        
        if (feature.getStatus() == null) {
            result.addError(prefix + "Feature status is null");
        }
        
        if (feature.getName() == null) {
            result.addError(prefix + "Feature name is null");
        }
        
        if (feature.getType() == null) {
            result.addError(prefix + "Feature type is null");
        }

        if (feature.getMetrics() == null) {
            result.addError(prefix + "Feature metrics list is null");
        } else if (feature.getMetrics().isEmpty()) {
            result.addError(prefix + "Feature metrics list is empty");
        } else {
            for (int i = 0; i < feature.getMetrics().size(); i++) {
                Metric metric = feature.getMetrics().get(i);
                SettingsSchema metricResult = validateCampaignMetric(metric, index, i);
                if (!metricResult.isValid()) {
                    result.getErrors().addAll(metricResult.getErrors());
                    result.setValid(false);
                }
            }
        }

        if (feature.getRules() != null) {
            for (int i = 0; i < feature.getRules().size(); i++) {
                Rule rule = feature.getRules().get(i);
                SettingsSchema ruleResult = validateRule(rule, index, i);
                if (!ruleResult.isValid()) {
                    result.getErrors().addAll(ruleResult.getErrors());
                    result.setValid(false);
                }
            }
        }

        if (feature.getVariables() != null) {
            for (int i = 0; i < feature.getVariables().size(); i++) {
                Variable variable = feature.getVariables().get(i);
                SettingsSchema variableResult = validateVariableObject(variable, "Feature[" + index + "].Variable[" + i + "]");
                if (!variableResult.isValid()) {
                    result.getErrors().addAll(variableResult.getErrors());
                    result.setValid(false);
                }
            }
        }

        return result;
    }

    /**
     * Validates the campaign metric
     * @param metric metric to be validated
     * @param featureIndex index of the feature
     * @param metricIndex index of the metric
     * @return SettingsSchema object containing the validation result
     */
    private SettingsSchema validateCampaignMetric(Metric metric, int featureIndex, int metricIndex) {
        SettingsSchema result = new SettingsSchema();
        String prefix = "Feature[" + featureIndex + "].Metric[" + metricIndex + "]: ";
        
        if (metric == null) {
            result.addError(prefix + "Metric object is null");
            return result;
        }
        
        if (metric.getId() == null) {
            result.addError(prefix + "Metric id is null");
        }
        
        if (metric.getType() == null) {
            result.addError(prefix + "Metric type is null");
        }
        
        if (metric.getIdentifier() == null) {
            result.addError(prefix + "Metric identifier is null");
        }

        return result;
    }

    /**
     * Validates the rule
     * @param rule rule to be validated
     * @param featureIndex index of the feature
     * @param ruleIndex index of the rule
     * @return SettingsSchema object containing the validation result
     */
    private SettingsSchema validateRule(Rule rule, int featureIndex, int ruleIndex) {
        SettingsSchema result = new SettingsSchema();
        String prefix = "Feature[" + featureIndex + "].Rule[" + ruleIndex + "]: ";
        
        if (rule == null) {
            result.addError(prefix + "Rule object is null");
            return result;
        }
        
        if (rule.getType() == null) {
            result.addError(prefix + "Rule type is null");
        }
        
        if (rule.getRuleKey() == null) {
            result.addError(prefix + "Rule ruleKey is null");
        }
        
        if (rule.getCampaignId() == null) {
            result.addError(prefix + "Rule campaignId is null");
        }

        return result;
    }
}