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
package com.vwo.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metric {
    @JsonProperty("mca")
    private Integer mca;
    @JsonProperty("hasProps")
    private Boolean hashProps;
    @JsonProperty("identifier")
    private String identifier;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("type")
    private String type;

    public Integer getMca() {
        return mca;
    }

    public void setMca(Integer mca) {
        this.mca = mca;
    }

    public Boolean getHashProps() {
        return hashProps;
    }

    public void setHashProps(Boolean hashProps) {
        this.hashProps = hashProps;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
