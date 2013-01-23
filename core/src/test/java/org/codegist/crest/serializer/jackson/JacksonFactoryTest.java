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
import org.codegist.crest.NonInstanciableClassTest;
import org.codegist.crest.test.util.CRestConfigs;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author laurent.gilles@codegist.org
 */
public class JacksonFactoryTest extends NonInstanciableClassTest {

    private final CRestConfig mockCRestConfig = CRestConfigs.mockDefaultBehavior();

    public JacksonFactoryTest() {
        super(JacksonFactory.class);
    }

    @Test
    public void shouldNotCreateAnObjectMapperButUseTheCustomOn(){
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

        when(mockCRestConfig.get(JacksonFactoryTest.class.getName() + JacksonFactory.JACKSON_OBJECT_MAPPER)).thenReturn(mockObjectMapper);
        ObjectMapper actual = JacksonFactory.createObjectMapper(mockCRestConfig, getClass());
        assertEquals(mockObjectMapper, actual);
    }

    @Test
    public void shouldCreateAnObjectMapperWithDefaultConfig(){
        ObjectMapper actual = JacksonFactory.createObjectMapper(mockCRestConfig, getClass());
        assertNotNull(actual);
        assertFalse(actual.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void shouldCreateAnObjectMapperWithGivenConfig(){
        Map<DeserializationFeature, Boolean> deserialization = new HashMap<DeserializationFeature, Boolean>();
        Map<SerializationFeature, Boolean> serialization = new HashMap<SerializationFeature, Boolean>();
        deserialization.put(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        serialization.put(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        when(mockCRestConfig.get(eq(JacksonFactoryTest.class.getName() + JacksonFactory.JACKSON_DESERIALIZER_CONFIG), any(Object.class))).thenReturn(deserialization);
        when(mockCRestConfig.get(eq(JacksonFactoryTest.class.getName() + JacksonFactory.JACKSON_SERIALIZER_CONFIG), any(Object.class))).thenReturn(serialization);

        ObjectMapper actual = JacksonFactory.createObjectMapper(mockCRestConfig, getClass());
        assertNotNull(actual);
        assertTrue(actual.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(actual.getSerializationConfig().isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
    }
}
