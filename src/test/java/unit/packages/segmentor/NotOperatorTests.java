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

public class NotOperatorTests {

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
    
    ServiceContainer serviceContainer = new ServiceContainer(loggerService, settingsManager, vwoInitOptions, batchEventQueue, settings);
    
    Feature feature = new Feature();
    feature.setIsGatewayServiceRequired(false);
    
    VWOContext context = new VWOContext();
    context.setId("test-user");
    
    // Initialize the evaluator by calling setContextualData
    segmentationManager.setContextualData(serviceContainer, feature, context);
  }

  @Test
  public void exactMatchTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"something\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "something");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void exactMatchWithSpecialCharactersTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"f25u!v@b#k$6%9^f&o*v(m)w_-=+s,./`(*&^%$#@!\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "f25u!v@b#k$6%9^f&o*v(m)w_-=+s,./`(*&^%$#@!");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void exactMatchWithSpacesTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"nice to see you. will    you be   my        friend?\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "nice to see you. will    you be   my        friend?");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void exactMatchWithUpperCaseTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"HgUvshFRjsbTnvsdiUFFTGHFHGvDRT.YGHGH\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "HgUvshFRjsbTnvsdiUFFTGHFHGvDRT.YGHGH");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void numericDataTypeTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"123\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 123);
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void floatDataTypeTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"123.456\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 123.456);
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void floatDataTypeExtraDecimalZerosTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"123.456\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 123.456000000);
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void numericDataTypeMismatchTest2() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"123\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 123.0);
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void stringifiedFloatTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"123.456\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "123.456000000");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void stringifiedFloatTest2() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"123.0\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 123);
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void stringifiedFloatTest3() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"123.4560000\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 123.456);
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void charDataTypeTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"E\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 'E'); // Char in JAVA
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void booleanDataTypeTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"true\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", true);
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void booleanDataTypeTest2() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"false\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", false);
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void mismatchTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"something\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "notsomething");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void partOfTextTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"zzsomethingzz\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "something");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void singleCharTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"zzsomethingzz\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "i");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void CaseMismatchTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"something\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "Something");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void CaseMismatchTest2() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"something\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "SOMETHING");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void noValueProvidedTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"something\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void missingkeyValueTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"something\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void nullValueProvidedTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"something\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", null);
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void incorrectKeyTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"something\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("neq", "something");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void incorrectKeyCaseTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"something\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("EQ", "something");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void numericDataTypeMismatchTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"123\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 12);
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void floatDataTypeMismatchTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"123.456\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 123);
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void floatDataTypeMismatchTest2() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"123.456\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 123.4567);
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void booleanDataTypeMismatchTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"false\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", true);
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void booleanDataTypeMismatchTest2() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"true\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", false);
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void charDataTypeCaseMismatchTest() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"E\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 'e');
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void charDataTypeCaseMismatchTest2() {
    String dsl = "{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"e\"}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", 'E');
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void nestedNotOperatorTest() {
    String dsl = "{\"or\":[{\"or\":[{\"not\":{\"or\":[{\"or\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]}]}}]}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "eq_value");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleNotOperatorTest() {
    String dsl = "{\"or\":[{\"not\":{\"or\":[{\"not\":{\"or\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]}}]}}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "eq_value");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleNotOperatorTest2() {
    String dsl = "{\"and\":[{\"and\":[{\"not\":{\"and\":[{\"and\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]}]}}]}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "eq_value");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleNotOperatorTest3() {
    String dsl = "{\"and\":[{\"not\":{\"and\":[{\"not\":{\"and\":[{\"custom_variable\":{\"eq\":\"eq_value\"}}]}}]}}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("eq", "eq_value");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleNotOperatorTest4() {
    String dsl = "{\"not\":{\"or\":[{\"not\":{\"or\":[{\"not\":{\"or\":[{\"not\":{\"or\":[{\"custom_variable\":{\"neq\":\"eq_value\"}}]}}]}}]}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("neq", "eq_value");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleNotOperatorTest5() {
    String dsl = "{\"not\":{\"or\":[{\"not\":{\"or\":[{\"not\":{\"or\":[{\"not\":{\"or\":[{\"custom_variable\":{\"neq\":\"not_eq_value\"}}]}}]}}]}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("neq", "eq_value");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleNotOperatorTest6() {
    String dsl = "{\"not\":{\"or\":[{\"not\":{\"or\":[{\"not\":{\"or\":[{\"not\":{\"or\":[{\"not\":{\"or\":[{\"custom_variable\":{\"neq\":\"eq_value\"}}]}}]}}]}}]}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("neq", "eq_value");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void multipleNotOperatorTest7() {
    String dsl = "{\"not\":{\"or\":[{\"not\":{\"or\":[{\"not\":{\"or\":[{\"not\":{\"or\":[{\"not\":{\"or\":[{\"custom_variable\":{\"neq\":\"neq_value\"}}]}}]}}]}}]}}]}}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("neq", "eq_value");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  public static void verifyExpectation(String dsl, Map<String, Object> customVariables) {
    Assertions.assertEquals(segmentationManager.validateSegmentation(dsl, customVariables), customVariables.get("expectation"));
  }
}
