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
import org.codegist.crest.param.Param;
import org.codegist.crest.serializer.Serializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class JsonEncodedFormJacksonSerializer implements Serializer<List<Param>> {

    private final ObjectMapper jackson;

    public JsonEncodedFormJacksonSerializer(CRestConfig crestConfig) {
        this.jackson = JacksonFactory.createObjectMapper(crestConfig, getClass());
    }

    public void serialize(List<Param> value, Charset charset, OutputStream out) throws Exception {
        JsonObject json = JsonObject.toJsonObject(value);
        jackson.writeValue(out, json);
    }

    private static class JsonObject extends LinkedHashMap<String,Object> {

        static JsonObject toJsonObject(List<Param> value){
            JsonObject json = new JsonObject();
            for(Param p : value){
                String name = p.getParamConfig().getName();
                for(Object o : p.getValue()){
                    json.put(name, o);
                }
            }
            return json;
        }

        @Override
        public Object put(String key, Object value) {
            if(containsKey(key)) {
                Object o = get(key);
                Object prev = o;
                Collection<Object> dest;
                if(o instanceof Collection) {
                    dest = (Collection<Object>) o;
                }else{
                    dest = new ArrayList<Object>();
                    dest.add(o);
                    prev = super.put(key, dest);
                }
                dest.add(value);
                return prev;
            }else{
                return super.put(key, value);
            }
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m) {
            for(Map.Entry<? extends String, ? extends Object> e : m.entrySet()){
                put(e.getKey(), e.getValue());
            }
        }
    }

}
