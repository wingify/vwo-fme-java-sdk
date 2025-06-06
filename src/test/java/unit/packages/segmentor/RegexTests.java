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

import java.util.HashMap;
import java.util.Map;

public class RegexTests {
  private static final String SDK_KEY = "abcd";
  private static final int ACCOUNT_ID = 1234;

  @BeforeAll
  public static void initialize(){
    VWOInitOptions vwoInitOptions = new VWOInitOptions();
    vwoInitOptions.setSdkKey(SDK_KEY);
    vwoInitOptions.setAccountId(ACCOUNT_ID);
    VWO instance = VWO.init(vwoInitOptions);
  }

  @Test
  public void regexOperandTest() {
    String dsl = "{\"or\":[{\"custom_variable\":{\"reg\":\"regex(myregex+)\"}}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("reg", "myregexxxxxx");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void regexOperandTest2() {
    String dsl = "{\"or\":[{\"custom_variable\":{\"reg\":\"regex(<(W[^>]*)(.*?)>)\"}}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("reg", "<WingifySDK id=1></WingifySDK>");
      put("expectation", true);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void regexOperandMismatchTest2() {
    String dsl = "{\"or\":[{\"custom_variable\":{\"reg\":\"regex(<(W[^>]*)(.*?)>)\"}}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("reg", "<wingifySDK id=1></wingifySDK>");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void regexOperandCaseMismatchTest() {
    String dsl = "{\"or\":[{\"custom_variable\":{\"reg\":\"regex(myregex+)\"}}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("reg", "myregeXxxxxx");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void invalidReqexTest() {
    String dsl = "{\"or\":[{\"custom_variable\":{\"reg\":\"regex(*)\"}}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("reg", "*");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  @Test
  public void invalidReqexTest2() {
    String dsl = "{\"or\":[{\"custom_variable\":{\"reg\":\"regex(*)\"}}]}";
    Map<String, Object> customVariables = new HashMap<String, Object>() {{
      put("reg", "asdf");
      put("expectation", false);
    }};

    verifyExpectation(dsl, customVariables);
  }

  public static void verifyExpectation(String dsl, Map<String, Object> customVariables) {
    SegmentationManager.getInstance().attachEvaluator();
    Assertions.assertEquals(SegmentationManager.getInstance().validateSegmentation(dsl, customVariables), customVariables.get("expectation"));
  }
}
