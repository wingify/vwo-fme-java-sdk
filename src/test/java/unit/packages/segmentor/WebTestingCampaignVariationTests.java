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

package unit.packages.segmentor;

import com.wingify.ServiceContainer;
import com.wingify.Wingify;
import com.wingify.models.Feature;
import com.wingify.models.Settings;
import com.wingify.models.user.WingifyUserContext;
import com.wingify.models.user.WingifyInitOptions;
import com.wingify.packages.segmentation_evaluator.core.SegmentationManager;
import com.wingify.packages.segmentation_evaluator.utils.WebTestingSegmentUtil;
import com.wingify.services.BatchEventQueue;
import com.wingify.services.LoggerService;
import com.wingify.services.SettingsManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class WebTestingCampaignVariationTests {
    private static final String SDK_KEY = "abcd";
    private static final int ACCOUNT_ID = 1234;
    private static SegmentationManager segmentationManager;
    private static WingifyUserContext context;

    @BeforeAll
    public static void initialize() {
        WingifyInitOptions wingifyInitOptions = new WingifyInitOptions();
        wingifyInitOptions.setSdkKey(SDK_KEY);
        wingifyInitOptions.setAccountId(ACCOUNT_ID);
        Wingify.init(wingifyInitOptions);

        LoggerService loggerService = new LoggerService(new HashMap<>());
        segmentationManager = new SegmentationManager(loggerService, true);

        SettingsManager settingsManager = mock(SettingsManager.class);
        BatchEventQueue batchEventQueue = mock(BatchEventQueue.class);
        Settings settings = new Settings();
        ServiceContainer serviceContainer =
                new ServiceContainer("test-user", loggerService, settingsManager, wingifyInitOptions, batchEventQueue, settings);

        Feature feature = new Feature();
        feature.setIsGatewayServiceRequired(false);

        context = new WingifyUserContext();
        context.setId("test-user");

        segmentationManager.setContextualData(serviceContainer, feature, context);
    }


    @Test
    public void evaluateWebTestingCampaignVariationFormats() {
        Map<String, String> map = new HashMap<>();
        map.put("1", "1");
        map.put("2", "2");

        Assertions.assertTrue(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("1_1", map).isResult());
        Assertions.assertFalse(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("1_2", map).isResult());
        Assertions.assertFalse(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("99_1", map).isResult());

        Assertions.assertTrue(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("1_!2", map).isResult());
        Assertions.assertFalse(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("1_!1", map).isResult());
        Assertions.assertFalse(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("99_!1", map).isResult());

        Assertions.assertTrue(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("!99", map).isResult());
        Assertions.assertFalse(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("!1", map).isResult());

        Assertions.assertTrue(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("100", Map.of("100", "1")).isResult());
        Assertions.assertFalse(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("100", map).isResult());

        Assertions.assertTrue(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("!1", null).isResult());
        Assertions.assertFalse(WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("1_1", null).isResult());
    }

    @Test
    public void invalidOperandEncodingReturnsFalse() {
        WebTestingSegmentUtil.WebTestingCampaignVariationEval result =
                WebTestingSegmentUtil.evaluateWebTestingCampaignVariation("bogus", Map.of("1", "1"));
        Assertions.assertFalse(result.isResult());
        Assertions.assertTrue(result.isInvalidFormat());
    }

    @Test
    public void normalizeWebTestingCampaignsMapCoercesValuesToStrings() {
        Map<Object, Object> rawMap = new HashMap<>();
        rawMap.put(129, 1);
        rawMap.put("14", 2);

        Map<String, String> normalized = WebTestingSegmentUtil.normalizeWebTestingCampaignsMap(rawMap);
        Assertions.assertEquals("1", normalized.get("129"));
        Assertions.assertEquals("2", normalized.get("14"));
    }

    @Test
    public void segmentEvaluatorCampaignVariationWithJsonStringWebTestingCampaigns() {
        context.setPlatformVariables(Map.of("webTestingCampaigns", "{\"1\":\"1\"}"));
        String dsl = "{\"or\":[{\"campaignVariation\":\"1_1\"}]}";
        Assertions.assertTrue(segmentationManager.validateSegmentation(dsl, new HashMap<>()));
    }

    @Test
    public void segmentEvaluatorCampaignVariationWithObjectWebTestingCampaigns() {
        context.setPlatformVariables(Map.of("webTestingCampaigns", Map.of("1", 1)));
        String dsl = "{\"or\":[{\"campaignVariation\":\"1_1\"}]}";
        Assertions.assertTrue(segmentationManager.validateSegmentation(dsl, new HashMap<>()));
    }

    @Test
    public void segmentEvaluatorCampaignVariationNotInCampaign() {
        context.setPlatformVariables(Map.of("webTestingCampaigns", "{}"));
        String dsl = "{\"or\":[{\"campaignVariation\":\"!1\"}]}";
        Assertions.assertTrue(segmentationManager.validateSegmentation(dsl, new HashMap<>()));
    }

    @Test
    public void segmentEvaluatorCampaignVariationWithNestedNot() {
        context.setPlatformVariables(Map.of("webTestingCampaigns", "{\"1\":\"1\"}"));
        String dsl = "{\"not\":{\"campaignVariation\":\"1_1\"}}";
        Assertions.assertFalse(segmentationManager.validateSegmentation(dsl, new HashMap<>()));
    }

    @Test
    public void segmentEvaluatorCampaignVariationWithCampaignOnly() {
        context.setPlatformVariables(Map.of("webTestingCampaigns", "{\"100\":\"2\"}"));
        String dsl = "{\"or\":[{\"campaignVariation\":\"100\"}]}";
        Assertions.assertTrue(segmentationManager.validateSegmentation(dsl, new HashMap<>()));
    }

    @Test
    public void segmentEvaluatorCampaignVariationOperandTrimmed() {
        context.setPlatformVariables(Map.of("webTestingCampaigns", "{\"1\":\"1\"}"));
        String dsl = "{\"or\":[{\"campaignVariation\":\"  1_1  \"}]}";
        Assertions.assertTrue(segmentationManager.validateSegmentation(dsl, new HashMap<>()));
    }

    @Test
    public void segmentEvaluatorCampaignVariationJsonArrayRejected() {
        context.setPlatformVariables(Map.of("webTestingCampaigns", "[]"));
        String dsl = "{\"or\":[{\"campaignVariation\":\"1_1\"}]}";
        Assertions.assertFalse(segmentationManager.validateSegmentation(dsl, new HashMap<>()));
    }
}