/*
 * Copyright 2011 CodeGist.org
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *  ===================================================================
 *
 *  More information at http://www.codegist.org.
 */

package org.codegist.crest.serializer.jackson;

import org.codegist.crest.CRestConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.Map;

final class JacksonFactory {

    static final String JACKSON_OBJECT_MAPPER = "#user-object-mapper";
    static final String JACKSON_DESERIALIZER_CONFIG = "#deserializer-config";
    static final String JACKSON_SERIALIZER_CONFIG= "#serializer-config";

    private static final Map<SerializationFeature, Boolean> DEFAULT_SERIALIZER_CONFIG = new HashMap<SerializationFeature, Boolean>();
    private static final Map<DeserializationFeature, Boolean> DEFAULT_DESERIALIZER_CONFIG = new HashMap<DeserializationFeature, Boolean>();
    static{
        DEFAULT_DESERIALIZER_CONFIG.put(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JacksonFactory(){
        throw new IllegalStateException();
    }

    static ObjectMapper createObjectMapper(CRestConfig crestConfig, Class<?> source){
        String prefix = source.getName();

        ObjectMapper mapper = crestConfig.get(prefix + JACKSON_OBJECT_MAPPER);
        if(mapper != null){
            return mapper;
        }
        
        mapper = new ObjectMapper();

        Map<DeserializationFeature, Boolean> deserConfig = crestConfig.get(prefix + JACKSON_DESERIALIZER_CONFIG, DEFAULT_DESERIALIZER_CONFIG);
        for(Map.Entry<DeserializationFeature,Boolean> feature : deserConfig.entrySet()){
            mapper.configure(feature.getKey(), feature.getValue());
        }

        Map<SerializationFeature, Boolean> serConfig = crestConfig.get(prefix + JACKSON_SERIALIZER_CONFIG, DEFAULT_SERIALIZER_CONFIG);
        for(Map.Entry<SerializationFeature,Boolean> feature : serConfig.entrySet()){
            mapper.configure(feature.getKey(), feature.getValue());
        }

        return mapper;
    }

}
