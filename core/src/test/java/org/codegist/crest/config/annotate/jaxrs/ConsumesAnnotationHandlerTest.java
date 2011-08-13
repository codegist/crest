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

package org.codegist.crest.config.annotate.jaxrs;

import org.codegist.crest.config.annotate.DownToMethodAnnotationBaseTest;
import org.junit.Test;

import javax.ws.rs.Consumes;

import static org.mockito.Mockito.*;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class ConsumesAnnotationHandlerTest extends DownToMethodAnnotationBaseTest<Consumes> {

    public ConsumesAnnotationHandlerTest() {
        super(Consumes.class, new ConsumesAnnotationHandler());
    }

    @Test
    public void handleInterfaceAnnotationShouldFillMethodsProducesWithFirstAnnotationValue() throws Exception {
        when(mockAnnotation.value()).thenReturn(new String[]{"a","b"});
        when(mockInterfaceConfigBuilder.setMethodsProduces("a")).thenReturn(mockInterfaceConfigBuilder);
        toTest.handleInterfaceAnnotation(mockAnnotation, mockInterfaceConfigBuilder);
        verify(mockAnnotation, times(2)).value();
        verify(mockInterfaceConfigBuilder).setMethodsProduces("a");
    }

    @Test
    public void handleMethodAnnotationShouldFillProducesWithFirstAnnotationValue() throws Exception {
        when(mockAnnotation.value()).thenReturn(new String[]{"a","b"});
        when(mockMethodConfigBuilder.setProduces("a")).thenReturn(mockMethodConfigBuilder);
        toTest.handleMethodAnnotation(mockAnnotation, mockMethodConfigBuilder);
        verify(mockAnnotation, times(2)).value();
        verify(mockMethodConfigBuilder).setProduces("a");
    }
}
