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
package data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class DummySettingsReader {
    public Map<String,String> settingsMap;

    /**
     * Reads JSON files from a specified folder and returns a map where the keys are the filenames
     * (without extensions) and the values are the contents of the files as strings.
     *
     * @param folderPath The path to the folder containing the JSON files.
     * @return A map with filenames (without extensions) as keys and file contents as values.
     * @throws IOException If an I/O error occurs.
     */
    public static Map<String, String> readJsonFilesFromFolder(String folderPath) throws IOException {
        Map<String, String> jsonFilesMap = new HashMap<>();

        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            byte[] bytes = Files.readAllBytes(path);
                            String content = new String(bytes);
                            String filenameWithoutExtension = path.getFileName().toString().replaceFirst("[.][^.]+$", "");
                            jsonFilesMap.put(filenameWithoutExtension, content);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }

        return jsonFilesMap;
    }

    /**
     * Constructor for DummySettingsReader.
     * Initializes the settingsMap by reading JSON files from a specified directory.
     */
    public DummySettingsReader(){
        try {
            settingsMap = readJsonFilesFromFolder("src/test/java/data/settings");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
