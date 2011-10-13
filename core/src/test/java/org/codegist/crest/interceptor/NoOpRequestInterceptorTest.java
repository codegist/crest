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

package org.codegist.crest.interceptor;

import org.junit.Test;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class NoOpRequestInterceptorTest {

    private final NoOpRequestInterceptor toTest = new NoOpRequestInterceptor();


    @Test
    public void shouldDoNothing() throws Exception {
        toTest.beforeFire(null, null, null);
    }
}