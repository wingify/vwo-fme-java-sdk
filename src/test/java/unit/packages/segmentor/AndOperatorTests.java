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

package unit.packages.segmentor;

import com.vwo.VWO;
import com.vwo.models.user.VWOInitOptions;
import com.vwo.packages.segmentation_evaluator.core.SegmentationManager;
import com.vwo.services.CampaignDecisionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.vwo.services.LoggerService;
import com.vwo.ServiceContainer;
import com.vwo.models.Feature;
import com.vwo.models.user.VWOContext;
import com.vwo.models.Settings;
import com.vwo.services.SettingsManager;
import com.vwo.services.BatchEventQueue;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

public class AndOperatorTests {

  private static final String SDK_KEY = "abcd";
  private static final int ACCOUNT_ID = 1234;

  private static SegmentationManager segmentationManager;

  @BeforeAll
  public static void initialize(){
    VWOInitOptions vwoInitOptions = new VWOInitOptions();
    vwoInitOptions.setSdkKey(SDK_KEY);
    vwoInitOptions.setAccountId(ACCOUNT_ID);
    VWO instance = VWO.init(vwoInitOptions);

    LoggerService loggerService = new LoggerService(new HashMap<>());
    segmentationManager = new SegmentationManager(loggerService, true);
    
    // Create mock objects for setContextualData parameters
    SettingsManager settingsManager = mock(SettingsManager.class);
    BatchEventQueue batchEventQueue = mock(BatchEventQueue.class);
    Settings settings = new Settings();
    
    ServiceContainer serviceContainer = new ServiceContainer("test-user", loggerService, settingsManager, vwoInitOptions, batchEventQueue, settings);
    
    Feature feature = new Feature();
    feature.setIsGatewayServiceRequired(false);
    
    VWOContext context = new VWOContext();
    context.setId("test-user");
    
    // Initialize the evaluator by calling setContextualData
    segmentationManager.setContextualData(serviceContainer, feature, context);
  }

  @Test
  public void singleAndOperatorMatchingTest() {
    String dsl = "{\"and\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "eq_value");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void singleAndOperatorMismatchTest() {
    String dsl = "{\"and\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("a", "n_eq_value");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void singleAndOperatorCaseMismatchTest() {
    String dsl = "{\"and\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "Eq_Value");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleAndOperatorTest2() {
    String dsl = "{\"and\":[{\"and\":[{\"and\":[{\"and\":[{\"and\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]}]}]}]}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "eq_value");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleAndOperatorWithSingleCorrectValueTest() {
    String dsl = "{\"and\":[{\"or\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]},{\"or\":[{\"custom_variable\":{\"reg\":\"regex(myregex+)\"}}]}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "eq_value");
      put("reg", "wrong");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleAndOperatorWithSingleCorrectValueTest2() {
    String dsl = "{\"and\":[{\"or\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]},{\"or\":[{\"custom_variable\":{\"reg\":\"regex(myregex+)\"}}]}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "wrong");
      put("reg", "myregexxxxxx");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleAndOperatorWithAllCorrectCustomVariablesTest() {
    String dsl = "{\"and\":[{\"or\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]},{\"or\":[{\"custom_variable\":{\"reg\":\"regex(myregex+)\"}}]}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "eq_value");
      put("reg", "myregexxxxxx");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleAndOperatorWithAllIncorrectCorrectCustomVariablesTest() {
    String dsl = "{\"and\":[{\"or\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]},{\"or\":[{\"custom_variable\":{\"reg\":\"regex(myregex+)\"}}]}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "wrong");
      put("reg", "wrong");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void sdhgsjdh() {
    String dsl = "{\"or\":[{\"user\":\"Varun\"}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "wrong");
      put("reg", "wrong");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  public static void verifyExpectation(String dsl, Map<String, Object> customVariables) {
    Assertions.assertEquals(segmentationManager.validateSegmentation(dsl, customVariables), customVariables.get("expectation"));
  }
}
