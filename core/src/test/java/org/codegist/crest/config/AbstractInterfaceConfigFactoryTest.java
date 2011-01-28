/*
 * Copyright 2010 CodeGist.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest.config;

import org.codegist.crest.Stubs;
import org.codegist.crest.TestUtils;

import java.lang.reflect.Method;
import static org.codegist.crest.config.Destination.BODY;
import static org.codegist.crest.config.Destination.URL;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public abstract class AbstractInterfaceConfigFactoryTest {


    public void assertMinimalExpected(InterfaceConfig test, Class clazz) {
        InterfaceConfigTestHelper.assertExpected(MINIMAL_EXPECTED_CONFIG, test, clazz);
    }

    public void assertPartialExpected(InterfaceConfig test, Class clazz) {
        InterfaceConfigTestHelper.assertExpected(PARTIAL_EXPECTED_CONFIG, test, clazz);
    }

    public void assertFullExpected(InterfaceConfig test, Class clazz) {
        InterfaceConfigTestHelper.assertExpected(FULLY_EXPECTED_CONFIG, test, clazz);
    }


    interface Interface {

        Object m1();

        Object m1(String a);

        Object m1(String a, int b);

        Object m1(String a, int[] b);

        void m2();

        void m2(float f, String... a);

        Method METH_m1 = TestUtils.getMethod(Interface.class, "m1");
        Method METH_m1S = TestUtils.getMethod(Interface.class, "m1", String.class);
        Method METH_m1SI = TestUtils.getMethod(Interface.class, "m1", String.class, int.class);
        Method METH_m1SIs = TestUtils.getMethod(Interface.class, "m1", String.class, int[].class);
        Method METH_m2 = TestUtils.getMethod(Interface.class, "m2");
        Method METH_m2FSs = TestUtils.getMethod(Interface.class, "m2", float.class, String[].class);
    }

    public static final InterfaceConfig MINIMAL_EXPECTED_CONFIG;

    static {
        try {
            MINIMAL_EXPECTED_CONFIG = new ConfigBuilders.InterfaceConfigBuilder(Interface.class)
                    .setEndPoint("http://localhost:8080")
                    .setContextPath("/my-path")
                    .startMethodConfig(Interface.METH_m1).setPath("/m1").endMethodConfig()
                    .startMethodConfig(Interface.METH_m1S).endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SI)
                    .setPath("/m1")
                    .startParamConfig(0).endParamConfig()
                    .startParamConfig(1).endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SIs).endMethodConfig()
                    .startMethodConfig(Interface.METH_m2).setPath("/m2/1").endMethodConfig()
                    .startMethodConfig(Interface.METH_m2FSs).endMethodConfig()
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static final InterfaceConfig PARTIAL_EXPECTED_CONFIG;

    static {
        try {
            PARTIAL_EXPECTED_CONFIG = new ConfigBuilders.InterfaceConfigBuilder(Interface.class)
                    .setEndPoint("http://localhost:8080")
                    .setContextPath("/my-path")
                    .setParamsSerializer(new Stubs.Serializer1())
                    .setParamsInjector(new Stubs.RequestParameterInjector1())
                    .startMethodConfig(Interface.METH_m1).setPath("/m1").setResponseHandler(new Stubs.ResponseHandler1()).endMethodConfig()
                    .startMethodConfig(Interface.METH_m1S)
                    .setPath("/m1").setHttpMethod("POST")
                    .setParamsSerializer(new Stubs.Serializer2())
                    .startParamConfig(0).setInjector(new Stubs.RequestParameterInjector3()).setSerializer(new Stubs.Serializer3()).endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SI)
                    .setPath("/m1")
                    .setParamsInjector(new Stubs.RequestParameterInjector2())
                    .startParamConfig(1).setName("c").setDefaultValue("444").endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SIs)
                    .setPath("/m1")
                    .setParamsInjector(new Stubs.RequestParameterInjector2())
                    .startParamConfig(1).setName("c").endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m2).setPath("/m2/1")
                    .setHttpMethod("GET")
                    .setSocketTimeout(11l)
                    .setConnectionTimeout(12l)
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m2FSs).endMethodConfig()
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }


    public static final InterfaceConfig FULLY_EXPECTED_CONFIG;

    static {
        try {
            FULLY_EXPECTED_CONFIG = new ConfigBuilders.InterfaceConfigBuilder(Interface.class)
                    .setEndPoint("http://localhost:8080")
                    .setContextPath("/my-path")
                    .addMethodsExtraParam("body-param", "body-value", Destination.BODY)
                    .addMethodsExtraParam("body-param1", "body-value1", Destination.BODY)
                    .addMethodsExtraParam("body-param2", "body-value2", Destination.BODY)
                    .addMethodsExtraParam("header-param", "header-value", Destination.HEADER)
                    .addMethodsExtraParam("header-param1", "header-value1", Destination.HEADER)
                    .addMethodsExtraParam("header-param2", "header-value2", Destination.HEADER)
                    .addMethodsExtraParam("query-param", "query-value", Destination.URL)
                    .addMethodsExtraParam("query-param1", "query-value1", Destination.URL)
                    .addMethodsExtraParam("query-param2", "query-value2", Destination.URL)
                    .addMethodsExtraParam("path-param", "path-value", Destination.URL)
                    .addMethodsExtraParam("path-param1", "path-value1", Destination.URL)
                    .addMethodsExtraParam("path-param2", "path-value2", Destination.URL)
                    .setMethodsSocketTimeout(1l)
                    .setMethodsConnectionTimeout(2l)
                    .setEncoding("utf-8")
                    .setGlobalInterceptor(new Stubs.RequestInterceptor1())
                    .setParamsSerializer(new Stubs.Serializer1())
                    .setMethodsPath("/hello")
                    .setMethodsHttpMethod("DELETE")
                    .setMethodsResponseHandler(new Stubs.ResponseHandler1())
                    .setMethodsRetryHandler(new Stubs.RetryHandler1())
                    .setMethodsErrorHandler(new Stubs.ErrorHandler1())
                    .setMethodsRequestInterceptor(new Stubs.RequestInterceptor1())
                    .setParamsInjector(new Stubs.RequestParameterInjector1())
                    .startMethodConfig(Interface.METH_m1)
                    .setPath("/m1")
                    .addExtraParam("body-param", "over-value1", Destination.BODY)
                    .addExtraParam("body-param3", "new-value", Destination.BODY)
                    .setHttpMethod("PUT")
                    .setRetryHandler(new Stubs.RetryHandler2())
                    .setSocketTimeout(3l)
                    .setConnectionTimeout(4l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor3())
                    .setResponseHandler(new Stubs.ResponseHandler1())
                    .setErrorHandler(new Stubs.ErrorHandler2())
                    .setParamsInjector(new Stubs.RequestParameterInjector2())
                    .setParamsSerializer(new Stubs.Serializer3())
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1S)
                    .addExtraParam("body-param", "over-value1", Destination.URL)
                    .setPath("/m1")
                    .setHttpMethod("POST")
                    .setSocketTimeout(5l)
                    .setConnectionTimeout(6l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor2())
                    .setResponseHandler(new Stubs.ResponseHandler2())
                    .setParamsSerializer(new Stubs.Serializer2())
                    .setParamsInjector(new Stubs.RequestParameterInjector2())
                    .startParamConfig(0)
                    .setName("a")
                    .setDefaultValue("deff")
                    .setDestination(Destination.HEADER)
                    .setSerializer(new Stubs.Serializer3())
                    .setInjector(new Stubs.RequestParameterInjector3())
                    .endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SI)
                    .setPath("/m1")
                    .setHttpMethod("DELETE")
                    .setSocketTimeout(7l)
                    .setConnectionTimeout(8l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor3())
                    .setResponseHandler(new Stubs.ResponseHandler1())
                    .setParamsSerializer(new Stubs.Serializer3())
                    .startParamConfig(0)
                    .setName("b")
                    .setDestination(BODY)
                    .setSerializer(new Stubs.Serializer1())
                    .setInjector(new Stubs.RequestParameterInjector3())
                    .endParamConfig()
                    .startParamConfig(1)
                    .setName("c")
                    .setDestination(URL)
                    .setSerializer(new Stubs.Serializer2())
                    .endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SIs)
                    .setPath("/m1")
                    .setHttpMethod("HEAD")
                    .setSocketTimeout(9l)
                    .setConnectionTimeout(10l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor1())
                    .setResponseHandler(new Stubs.ResponseHandler1())
                    .setParamsSerializer(new Stubs.Serializer1())
                    .startParamConfig(0)
                    .setName("d")
                    .setDestination(URL)
                    .setSerializer(new Stubs.Serializer1())
                    .endParamConfig()
                    .startParamConfig(1)
                    .setName("e")
                    .setDestination(BODY)
                    .setSerializer(new Stubs.Serializer3())
                    .endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m2)
                    .setPath("/m2/1")
                    .setHttpMethod("GET")
                    .setSocketTimeout(11l)
                    .setConnectionTimeout(12l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor3())
                    .setResponseHandler(new Stubs.ResponseHandler1())
                    .setParamsSerializer(new Stubs.Serializer1())
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m2FSs)
                    .setPath("/m2/2")
                    .setHttpMethod("POST")
                    .setSocketTimeout(13l)
                    .setConnectionTimeout(14l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor2())
                    .setResponseHandler(new Stubs.ResponseHandler2())
                    .setParamsSerializer(new Stubs.Serializer2())
                    .startParamConfig(0)
                    .setName("f")
                    .setDestination(URL)
                    .setSerializer(new Stubs.Serializer3())
                    .endParamConfig()
                    .startParamConfig(1)
                    .setName("g")
                    .setDestination(URL)
                    .setSerializer(new Stubs.Serializer1())
                    .endParamConfig()
                    .endMethodConfig()
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
