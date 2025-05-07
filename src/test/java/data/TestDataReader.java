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
package data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vwo.VWOClient;
import data.testCases.TestCases;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDataReader {
    public TestCases testCases;

    /**
     * Reads the test cases from a JSON file located in the specified folder.
     * The JSON file must be named "index.json".
     *
     * @param folderPath The path to the folder containing the "index.json" file.
     * @return An instance of TestCases containing the data from the JSON file, or null if the file does not exist.
     */
    public static TestCases readTestCases(String folderPath) {
        Path indexPath = Paths.get(folderPath, "index.json");

        if (Files.exists(indexPath) && Files.isRegularFile(indexPath)) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                byte[] bytes = Files.readAllBytes(indexPath);
                String content = new String(bytes);
                TestCases testCases = mapper.readValue(content, TestCases.class);
                return testCases;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    /**
     * Constructor for TestDataReader.
     * Initializes the testCases field by reading the test cases from the specified directory.
     */
    public TestDataReader() {
        testCases = readTestCases("src/test/java/data/testCases");
    }
}
