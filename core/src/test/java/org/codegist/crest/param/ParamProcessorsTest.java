/*
 * Copyright 2010 CodeGist.org
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

package org.codegist.crest.param;

import org.codegist.crest.CRestException;
import org.codegist.crest.NonInstanciableClassTest;
import org.codegist.crest.config.ParamConfig;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.codegist.crest.test.util.Values.UTF8;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class ParamProcessorsTest extends NonInstanciableClassTest {



    public ParamProcessorsTest() {
        super(ParamProcessors.class);
    }

    @Test
    public void shouldIterateOverPreProccessedParamValues() throws Exception {
        List<EncodedPair> expected1 = asList(mock(EncodedPair.class), mock(EncodedPair.class));
        List<EncodedPair> expected2 = asList();
        List<EncodedPair> expected3 = asList(mock(EncodedPair.class));
        Param mockParam1 = mockParam(true, expected1);
        Param mockParam2 = mockParam(true, expected2);
        Param mockParam3 = mockParam(true, expected3);

        Iterator<EncodedPair> actual = ParamProcessors.iterate(asList(mockParam1, mockParam2, mockParam3), UTF8);
        assertTrue(actual.hasNext());
        assertEquals(expected1.get(0), actual.next());
        assertTrue(actual.hasNext());
        assertEquals(expected1.get(1), actual.next());
        assertTrue(actual.hasNext());
        assertEquals(expected3.get(0), actual.next());
        assertFalse(actual.hasNext());
    }

    @Test
    public void shouldIterateOverPreProccessedParamValuesNonEncoding() throws Exception {
        List<EncodedPair> expected1 = asList(mock(EncodedPair.class), mock(EncodedPair.class));
        List<EncodedPair> expected2 = asList();
        List<EncodedPair> expected3 = asList(mock(EncodedPair.class));
        Param mockParam1 = mockParam(false, expected1);
        Param mockParam2 = mockParam(false, expected2);
        Param mockParam3 = mockParam(false, expected3);

        Iterator<EncodedPair> actual = ParamProcessors.iterate(asList(mockParam1, mockParam2, mockParam3), UTF8, false);
        assertTrue(actual.hasNext());
        assertEquals(expected1.get(0), actual.next());
        assertTrue(actual.hasNext());
        assertEquals(expected1.get(1), actual.next());
        assertTrue(actual.hasNext());
        assertEquals(expected3.get(0), actual.next());
        assertFalse(actual.hasNext());
    }

    @Test(expected = CRestException.class)
    public void shouldThrowExceptionWhenParamProcessingFail() throws Exception {
        Param mockParam = mock(Param.class);

        Exception exception = new Exception();
        ParamProcessor paramProcessor = mock(ParamProcessor.class);
        when(paramProcessor.process(mockParam, UTF8, true)).thenThrow(exception);

        ParamConfig paramConfig = mock(ParamConfig.class);
        when(paramConfig.getParamProcessor()).thenReturn(paramProcessor);
        when(mockParam.getParamConfig()).thenReturn(paramConfig);

        try {
            ParamProcessors.iterate(asList(mockParam), UTF8).hasNext();
        } catch (Exception e) {
            assertSame(exception, e.getCause());
            throw e;
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldFailWhenTryingToRemoveFromIterator() throws Exception {
        Param mockParam = mock(Param.class);
        ParamProcessors.iterate(asList(mockParam), UTF8).remove();
    }


    private Param mockParam(boolean encodeIfNeeded, List<EncodedPair> pairs) throws Exception {
         Param mockParam = mock(Param.class);

        ParamProcessor paramProcessor = mock(ParamProcessor.class);
        when(paramProcessor.process(mockParam, UTF8, encodeIfNeeded)).thenReturn(pairs);

        ParamConfig paramConfig = mock(ParamConfig.class);
        when(paramConfig.getParamProcessor()).thenReturn(paramProcessor);
        when(mockParam.getParamConfig()).thenReturn(paramConfig);
        return mockParam;
    }
}
