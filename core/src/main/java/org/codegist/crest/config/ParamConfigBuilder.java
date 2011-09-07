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

package org.codegist.crest.config;

import org.codegist.crest.serializer.Serializer;

import java.util.Map;

/**
 * @author laurent.gilles@codegist.org
 */
public interface ParamConfigBuilder {

    ParamConfig build() throws Exception;

    ParamConfigBuilder setName(String name);

    ParamConfigBuilder setDefaultValue(String defaultValue);

    ParamConfigBuilder setListSeparator(String listSeparator);

    ParamConfigBuilder setEncoded(boolean encoded);

    ParamConfigBuilder setMetaDatas(Map<String,Object> metadatas);

    ParamConfigBuilder setSerializer(Class<? extends Serializer> serializer);

    ParamConfigBuilder setType(ParamType type);

    ParamConfigBuilder forCookie();

    ParamConfigBuilder forQuery();

    ParamConfigBuilder forPath();

    ParamConfigBuilder forForm();

    ParamConfigBuilder forMultiPart();

    ParamConfigBuilder forHeader();

    ParamConfigBuilder forMatrix();
}
