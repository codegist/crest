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

package org.codegist.crest.error;

import org.codegist.crest.BaseCRestTest;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.CRestException;
import org.codegist.crest.CRestProperty;
import org.codegist.crest.annotate.*;
import org.codegist.crest.io.Request;
import org.codegist.crest.io.RequestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author laurent.gilles@codegist.org
 */
public class ErrorHandlersTest extends BaseCRestTest<ErrorHandlersTest.ErrorHandlers> {

    public ErrorHandlersTest(CRestHolder holder) {
        super(holder, ErrorHandlers.class);
    }

    @Parameterized.Parameters
    public static Collection<CRestHolder[]> getData() {
        return crest(arrify(forEachBaseBuilder(new Builder() {
            public CRestHolder build(CRestBuilder builder) {
                return new CRestHolder(builder
                        .setProperty(CRestProperty.HANDLER_RETRY_MAX_ATTEMPTS, 3)
                        .build());
            }
        })));
    }

    @Before
    public void setup(){
        toTest.reset();
    }

    @Test
    public void testFailAndRetryAndSuccess() {
        toTest.failFor(2);
        String value = toTest.retry("value");

        assertEquals("value", value);
        assertEquals(2, toTest.callCount());
    }

    @Test(expected = CRestException.class)
    public void testFailAndRetryAndFail() throws Exception {
        toTest.failFor(4);
        try {
            toTest.retry("value");
        }catch(Exception e){
            assertEquals(3, toTest.callCount());
            throw e;
        }
    }

    @Test(expected = CRestException.class)
    public void testRetryCustom() throws Exception {
        try {
            toTest.retryCustom();
        }catch(Exception e){
            assertEquals(1, toTest.callCount());
            throw e;
        }
    }

    @Test
    public void testHandleError(){
        assertEquals("Hello!", toTest.handleError());
    }

    @Test
    public void testHttpCode400() {
        try {
            toTest.httpCode(400);
            fail();
        }catch(CRestException e){
            e.printStackTrace();
        }
    }
    @Test
    public void testHttpCode500() {
        try {
            toTest.httpCode(500);
            fail();
        }catch(CRestException e){
            e.printStackTrace();
        }
    }
    @Test
    public void testHttpCode200() {
        assertEquals("ok", toTest.httpCode(200));
    }

    @EndPoint("{crest.server.end-point}")
    @Path("error")
    @GET
    public static interface ErrorHandlers {

        @Path("call-count")
        int callCount();

        @Path("reset")
        void reset();

        @Path("fail-for")
        void failFor(@QueryParam("time") int times);

        @Path("retry")
        String retry(@QueryParam("value") String value);

        @ErrorHandler(MyErrorHandler.class)
        @Path("error-handler")
        String handleError();

        @RetryHandler(MyRetryHandler.class)
        @Path("retry-custom")
        String retryCustom();

        @Path("http-code")
        String httpCode(@QueryParam("code") int httpCode);

    }

    public static class MyErrorHandler implements org.codegist.crest.handler.ErrorHandler {
        public <T> T handle(Request request, Exception e) throws Exception {
            return (T) "Hello!";
        }
    }
    public static class MyRetryHandler implements org.codegist.crest.handler.RetryHandler {
        public boolean retry(RequestException exception, int retryNumber) {
            return false;
        }
    }
}