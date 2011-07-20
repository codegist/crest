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

package org.codegist.crest.security.basic;

import org.codegist.crest.param.EncodedPair;
import org.codegist.crest.security.Authorization;
import org.codegist.crest.security.AuthorizationToken;

import java.io.UnsupportedEncodingException;

import static org.codegist.common.codec.Base64.encodeToString;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class BasicAuthorization implements Authorization {

    private final AuthorizationToken token;

    public BasicAuthorization(String name, String password) throws UnsupportedEncodingException {
        this.token = new AuthorizationToken("Basic", encodeToString((name + ":" + password).getBytes("utf-8")));
    }

    public AuthorizationToken authorize(String action, String url, EncodedPair... parameters) {
        return token;
    }

    public void refresh() {
        // NoOp
    }

}
