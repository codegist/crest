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

import org.codegist.common.log.Logger;
import org.codegist.crest.config.MethodType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import static org.codegist.crest.io.http.HttpConstants.HTTP_BAD_REQUEST;

final class HttpURLConnectionHttpChannel implements HttpChannel {

    private static final Logger LOG = Logger.getLogger(HttpURLConnectionHttpChannel.class);
    private final HttpURLConnection con;
    private final MethodType methodType;
    private HttpEntityWriter httpEntityWriter;


    public HttpURLConnectionHttpChannel(HttpURLConnection con, MethodType methodType){
        this.methodType = methodType;
        this.con = con;
        this.con.setRequestProperty("Connection", "Keep-Alive");
        this.con.setRequestProperty("User-Agent", "CodeGist-CRest Agent");
    }

    public void setSocketTimeout(int timeout) throws IOException {
        con.setReadTimeout(timeout);
    }

    public void setConnectionTimeout(int timeout) throws IOException {
        con.setConnectTimeout(timeout);
    }

    public void setHeader(String name, String value) throws IOException {
        con.setRequestProperty(name, value);
    }

    public void addHeader(String name, String value) throws IOException {
        con.addRequestProperty(name, value);
    }

    public void setContentType(String value) throws IOException {
        setHeader("Content-Type", value);
    }

    public void setAccept(String value) throws IOException {
        setHeader("Accept", value);
    }

    public void writeEntityWith(HttpEntityWriter httpEntityWriter) throws IOException {
        this.httpEntityWriter = httpEntityWriter;
    }

    public Response send() throws IOException {
        if(methodType.hasEntity()) {
            if(httpEntityWriter.getContentLength() >= 0) {
               con.setRequestProperty("Content-Length", String.valueOf(httpEntityWriter.getContentLength()));
            }
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            httpEntityWriter.writeEntityTo(os);
            os.flush();
            os.close();
        }
        return new HttpURLResponse(con);
    }

    private static final class HttpURLResponse implements Response {

        private final HttpURLConnection con;

        private HttpURLResponse(HttpURLConnection con) {
            this.con = con;
        }

        public int getStatusCode() throws IOException {
            return con.getResponseCode();
        }

        public String getStatusMessage() throws IOException {
            return con.getResponseMessage();
        }

        public InputStream getEntity() throws IOException {
            if(getStatusCode() >= HTTP_BAD_REQUEST) {
                return con.getErrorStream();
            }else{
                return con.getInputStream();
            }
        }
        
        public String getHeaderField(String field) {
            return con.getHeaderField(field);
        }
        
        public Map<String, List<String>> getHeaderFields () {
            return con.getHeaderFields();
        }

        public String getContentType() {
            return con.getContentType();
        }

        public String getContentEncoding() {
            return con.getContentEncoding();
        }

        public void close() {
            LOG.trace("Disconnecting...");
            con.disconnect();
        }
    }

}
