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

import org.codegist.common.io.IOs;
import org.codegist.crest.CRestConfig;
import org.codegist.crest.serializer.BaseDeserializerTest;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author laurent.gilles@codegist.org
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JacksonFactory.class, TypeFactory.class})
public class JacksonDeserializerTest extends BaseDeserializerTest {

    private final CRestConfig crestConfig = mock(CRestConfig.class);

    @Test
    public void shouldDeserializeInputStreamUsingGenericTypeAndObjectMapper() throws IOException {
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        JavaType mockJavaType = mock(JavaType.class);
        Object mockResult = mock(Object.class);
        Type mockType = mock(Type.class);
        InputStream mockInputStream = toInputStream("hello");
        Charset charset = Charset.forName("UTF-8");
        TypeFactory mockTypeFactory = mock(TypeFactory.class);

        mockStatic(JacksonFactory.class);
        mockStatic(TypeFactory.class);
        when(JacksonFactory.createObjectMapper(any(CRestConfig.class), any(Class.class))).thenReturn(mockObjectMapper);
        when(mockObjectMapper.getTypeFactory()).thenReturn(mockTypeFactory);
        when(mockTypeFactory.constructType(any(Type.class))).thenReturn(mockJavaType);
        when(mockObjectMapper.readValue(any(InputStreamReader.class), any(JavaType.class))).thenReturn(mockResult);

        Object actual = new JacksonDeserializer(crestConfig).deserialize(null, mockType, mockInputStream, charset);

        ArgumentCaptor<InputStreamReader> inputStreamReader = ArgumentCaptor.forClass(InputStreamReader.class);

        assertEquals(mockResult, actual);
        verify(mockObjectMapper).readValue(inputStreamReader.capture(), eq(mockJavaType));

        InputStreamReader actualInputStream = inputStreamReader.getValue();
        assertEquals("UTF8", actualInputStream.getEncoding());
        assertEquals("hello", IOs.toString(actualInputStream));
        verifyStatic();
        JacksonFactory.createObjectMapper(crestConfig, JacksonDeserializer.class);
        mockTypeFactory.constructType(mockType);
    }

}
