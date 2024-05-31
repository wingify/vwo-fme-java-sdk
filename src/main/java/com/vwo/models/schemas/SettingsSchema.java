/**
 * Copyright 2024 Wingify Software Pvt. Ltd.
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

public class SettingsSchema {


    public boolean isSettingsValid(Settings settings) {
        if (settings == null) {
            return false;
        }

        // Validate SettingsModel fields
        if (settings.getVersion() == null || settings.getAccountId() == null) {
            return false;
        }

        if (settings.getCampaigns() == null || settings.getCampaigns().isEmpty()) {
            return false;
        }

        for (Campaign campaign : settings.getCampaigns()) {
            if (!isValidCampaign(campaign)) {
                return false;
            }
        }

        if (settings.getFeatures() != null) {
            for (Feature feature : settings.getFeatures()) {
                if (!isValidFeature(feature)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isValidCampaign(Campaign campaign) {
        if (campaign.getId() == null || campaign.getType() == null || campaign.getKey() == null || campaign.getStatus() == null || campaign.getName() == null) {
            return false;
        }

        if (campaign.getVariations() == null || campaign.getVariations().isEmpty()) {
            return false;
        }

        for (Variation variation : campaign.getVariations()) {
            if (!isValidCampaignVariation(variation)) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidCampaignVariation(Variation variation) {
        if (variation.getId() == null || variation.getName() == null || String.valueOf(variation.getWeight()).isEmpty()) {
            return false;
        }

        if (variation.getVariables() != null) {
            for (Variable variable : variation.getVariables()) {
                if (!isValidVariableObject(variable)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isValidVariableObject(Variable variable) {
        return variable.getId() != null && variable.getType() != null && variable.getKey() != null && variable.getValue() != null;
    }

    private boolean isValidFeature(Feature feature) {
        if (feature.getId() == null || feature.getKey() == null || feature.getStatus() == null || feature.getName() == null || feature.getType() == null) {
            return false;
        }

        if (feature.getMetrics() == null || feature.getMetrics().isEmpty()) {
            return false;
        }

        for (Metric metric : feature.getMetrics()) {
            if (!isValidCampaignMetric(metric)) {
                return false;
            }
        }

        if (feature.getRules() != null) {
            for (Rule rule : feature.getRules()) {
                if (!isValidRule(rule)) {
                    return false;
                }
            }
        }

        if (feature.getVariables() != null) {
            for (Variable variable : feature.getVariables()) {
                if (!isValidVariableObject(variable)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isValidCampaignMetric(Metric metric) {
        return metric.getId() != null && metric.getType() != null && metric.getIdentifier() != null;
    }

    private boolean isValidRule(Rule rule) {
        return rule.getType() != null && rule.getRuleKey() != null && rule.getCampaignId() != null;
    }

}