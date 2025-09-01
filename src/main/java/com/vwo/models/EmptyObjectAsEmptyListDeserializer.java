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

package com.vwo.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class EmptyObjectAsEmptyListDeserializer<T>
        extends JsonDeserializer<List<T>>
        implements ContextualDeserializer {

    private JavaType valueType; // T

    public EmptyObjectAsEmptyListDeserializer() {}

    private EmptyObjectAsEmptyListDeserializer(JavaType valueType) {
        this.valueType = valueType;
    }

    @Override
    public List<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.START_OBJECT) {
            // Backend sent {} — treat as empty list
            p.skipChildren();
            return Collections.emptyList();
        }
        if (t == JsonToken.VALUE_NULL) {
            return Collections.emptyList();
        }
        // Normal case: it's an array
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JavaType listType = TypeFactory.defaultInstance()
                .constructCollectionType(List.class, valueType);
        return mapper.readValue(p, listType);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
            throws JsonMappingException {
        // Infer the list's element type from the field being deserialized
        JavaType propType = property.getType(); // e.g., List<Feature>
        JavaType elemType = propType.getContentType(); // e.g., Feature
        return new EmptyObjectAsEmptyListDeserializer<>(elemType);
    }
}
