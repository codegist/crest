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

package org.codegist.crest.serializer;

import org.codegist.common.io.IOs;
import org.codegist.crest.CRestException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * @author laurent.gilles@codegist.org
 */
public class CommaSeparatedIntDeserializer implements Deserializer  {
    public <T> T deserialize(Class<T> type, Type genericType, InputStream stream, Charset charset) throws CRestException {
        try {
            String[] ints = IOs.toString(stream, charset, true).split(",");
            int[] intPrims = new int[ints.length];
            int i = 0;
            for(String in : ints){
                intPrims[i++] = Integer.parseInt(in);
            }
            return (T) intPrims;
        } catch (IOException e) {
            throw CRestException.handle(e);
        }
    }
}