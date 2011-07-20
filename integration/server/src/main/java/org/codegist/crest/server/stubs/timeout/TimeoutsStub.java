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

package org.codegist.crest.server.stubs.timeout;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * @author laurent.gilles@codegist.org
 */
@Produces("text/html;charset=UTF-8")
@Path("timeout")
public class TimeoutsStub {

    @GET
    @Path("connection-timeout")
    public String connectionTimeout(@QueryParam("timeout") int timeout) throws InterruptedException {
        if(timeout > 0) {
            Thread.sleep(timeout);
        }
        return "ok";
    }

    @GET
    @Path("socket-timeout")
    public String socketTimeout(@QueryParam("timeout") int timeout) throws InterruptedException {
        if(timeout > 0) {
            Thread.sleep(timeout);
        }
        return "ok";
    }
}
