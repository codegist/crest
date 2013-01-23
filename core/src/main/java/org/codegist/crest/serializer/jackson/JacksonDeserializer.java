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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codegist.common.io.IOs;
import org.codegist.crest.CRestConfig;
import org.codegist.crest.serializer.Deserializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * <p><a href="http://jackson.codehaus.org/">Jackson</a> JSON deserializer implementation</p>
 * @author laurent.gilles@codegist.org
 */
public class JacksonDeserializer implements Deserializer {

    private static final String PREFIX = JacksonDeserializer.class.getName();

    /**
     * <p>CRestConfig property to provide a custom jackson {@link com.fasterxml.jackson.databind.ObjectMapper} instance.</p>
     * <p>Can be overridden by setting this property as follow:</p>
     * <code><pre>
     * ObjectMapper mapper = ...;
     * CRest crest = CRest.property(JacksonDeserializer.OBJECT_MAPPER_PROP, mapper).buid();
     * </pre></code>
     * <p>Default is auto-instantiated</p>
     * <p>Expects a {@link com.fasterxml.jackson.databind.ObjectMapper} instance</p>
     */
    public static final String OBJECT_MAPPER_PROP = PREFIX + JacksonFactory.JACKSON_OBJECT_MAPPER;

    /**
     * <p>CRestConfig property to turn on/off jackson deserialization features.</p>
     * <p>Can be overridden by setting this property as follow:</p>
     * <code><pre>
     * Map&lt;DeserializationConfig.Feature, Boolean&gt; features = ...;
     * CRest crest = CRest.property(JacksonDeserializer.JACKSON_DESERIALIZER_CONFIG_PROP, features).buid();
     * </pre></code>
     * <p>Default is auto-instantiated</p>
     * <p>Expects a {@link java.util.Map}&lt;{@link com.fasterxml.jackson.databind.DeserializationFeature}, {@link java.lang.Boolean}&gt; instance</p>
     */
    public static final String JACKSON_DESERIALIZER_CONFIG_PROP = PREFIX + JacksonFactory.JACKSON_DESERIALIZER_CONFIG;

    private final ObjectMapper jackson;

    /**
     * @param crestConfig CRest injected CRestConfig
     */
    public JacksonDeserializer(CRestConfig crestConfig) {
        this.jackson = JacksonFactory.createObjectMapper(crestConfig, getClass());
    }

    /**
     * @inheritDoc
     */
    public <T> T deserialize(Class<T> type, Type genericType, InputStream stream, Charset charset) throws IOException {
        try {
            return jackson.<T>readValue(new InputStreamReader(stream, charset), jackson.getTypeFactory().constructType(genericType));
        } finally {
            IOs.close(stream);
        }
    }
}
