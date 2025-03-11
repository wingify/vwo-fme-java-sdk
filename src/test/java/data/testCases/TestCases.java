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
package data.testCases;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class TestCases {
    @JsonProperty("GETFLAG_WITHOUT_STORAGE")
    private List<TestData> GETFLAG_WITHOUT_STORAGE;
    @JsonProperty("GETFLAG_MEG_RANDOM")
    private List<TestData> GETFLAG_MEG_RANDOM;
    @JsonProperty("GETFLAG_MEG_ADVANCE")
    private List<TestData> GETFLAG_MEG_ADVANCE;
    @JsonProperty("GETFLAG_WITH_STORAGE")
    private List<TestData> GETFLAG_WITH_STORAGE;
    @JsonProperty("GETFLAG_WITH_SALT")
    private List<TestData> GETFLAG_WITH_SALT;

    public List<TestData> getGETFLAG_WITHOUT_STORAGE() {
        return GETFLAG_WITHOUT_STORAGE;
    }

    public void setGETFLAG_WITHOUT_STORAGE(List<TestData> GETFLAG_WITHOUT_STORAGE) {
        this.GETFLAG_WITHOUT_STORAGE = GETFLAG_WITHOUT_STORAGE;
    }

    public List<TestData> getGETFLAG_MEG_RANDOM() {
        return GETFLAG_MEG_RANDOM;
    }

    public void setGETFLAG_MEG_RANDOM(List<TestData> GETFLAG_MEG_RANDOM) {
        this.GETFLAG_MEG_RANDOM = GETFLAG_MEG_RANDOM;
    }

    public List<TestData> getGETFLAG_MEG_ADVANCE() {
        return GETFLAG_MEG_ADVANCE;
    }

    public void setGETFLAG_MEG_ADVANCE(List<TestData> GETFLAG_MEG_ADVANCE) {
        this.GETFLAG_MEG_ADVANCE = GETFLAG_MEG_ADVANCE;
    }

    public List<TestData> getGETFLAG_WITH_STORAGE() {
        return GETFLAG_WITH_STORAGE;
    }

    public void setGETFLAG_WITH_STORAGE(List<TestData> GETFLAG_WITH_STORAGE) {
        this.GETFLAG_WITH_STORAGE = GETFLAG_WITH_STORAGE;
    }

    public List<TestData> getGETFLAG_WITH_SALT() {
        return GETFLAG_WITH_SALT;
    }

    public void setGETFLAG_WITH_SALT(List<TestData> GETFLAG_WITH_SALT) {
        this.GETFLAG_WITH_SALT = GETFLAG_WITH_SALT;
    }
}
