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

package org.codegist.crest.io.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.params.HttpProtocolParams;
import org.codegist.common.lang.Disposable;
import org.codegist.crest.CRestConfig;
import org.codegist.crest.config.MethodType;

import java.nio.charset.Charset;

/**
 * Apache's {@link org.apache.http.client.HttpClient}-backed HttpChannelFactory implementation
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public final class HttpClientHttpChannelFactory implements HttpChannelFactory, Disposable {

    /**
     * <p>CRestConfig property to provide a preconfigured {@link org.apache.http.client.HttpClient} instance.</p>
     * <p>Can be overridden by setting this property as follow:</p>
     * <code><pre>
     * HttpClient httpClient = ...;
     * CRest crest = CRest.property(HttpClientHttpChannelFactory.HTTP_CLIENT_PROP, httpClient).buid();
     * </pre></code>
     * <p>Default is automatically instanciated</p>
     * <p>Expects an {@link org.apache.http.client.HttpClient} instance</p>
     */
    public static final String HTTP_CLIENT_PROP = HttpClientHttpChannelFactory.class.getName() + HttpClientFactory.HTTP_CLIENT;

    private final HttpClient client;

    /**
     *
     * @param client the HTTP client instance to use
     */
    public HttpClientHttpChannelFactory(HttpClient client) {
        this.client = client;
    }

    /**
     *
     * @param crestConfig the crest config
     */
    public HttpClientHttpChannelFactory(CRestConfig crestConfig) {
        this(HttpClientFactory.create(crestConfig, HttpClientHttpChannelFactory.class));
    }

    /**
     * @inheritDoc
     */
    public HttpChannel open(MethodType methodType, String url, Charset charset) {
        HttpUriRequest request;
        switch(methodType) {
            case GET:
                request = new HttpGet(url);
            break;
            case POST:
                request = new HttpPost(url);
            break;
            case PUT:
                request = new HttpPut(url);
            break;
            case DELETE:
                request = new HttpDelete(url);
            break;
            case OPTIONS:
                request = new HttpOptions(url);
            break;
            case HEAD:
                request = new HttpHead(url);
            break;
            default:
                throw new IllegalArgumentException("Method " + methodType + " not supported");
        }

        HttpProtocolParams.setContentCharset(request.getParams(), charset.displayName());

        return new HttpClientHttpChannel(client, request);
    }

    public void dispose() {
        client.getConnectionManager().shutdown();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            dispose();
        } finally {
            super.finalize();
        }
    }
}
