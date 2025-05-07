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
package com.vwo.utils;

import com.vwo.services.UrlService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class SDKMetaUtil {

    private static final String POM_FILE_PATH = "pom.xml";
    private static String sdkVersion;

    /**
     * Initializes the SDKMetaUtil with the sdkVersion from pom.xml
     */
    public static void init() {
        try {
            File pomFile = new File(POM_FILE_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile);
            doc.getDocumentElement().normalize();
            Element versionElement = (Element) doc.getElementsByTagName("version").item(0);
            sdkVersion = versionElement.getTextContent();
        } catch (Exception e) {
            sdkVersion = "1.0.0-error";
            throw new RuntimeException("Failed to read version from pom.xml", e);
        }
    }

    /**
     * Returns the sdkVersion
     */
    public static String getSdkVersion(){
        return sdkVersion;
    }
}
