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

package org.codegist.crest;

import org.codegist.common.reflect.JdkProxyFactory;
import org.codegist.common.reflect.ProxyFactory;
import org.codegist.crest.config.*;
import org.codegist.crest.config.annotate.AnnotationHandler;
import org.codegist.crest.config.annotate.CRestAnnotations;
import org.codegist.crest.config.annotate.NoOpAnnotationHandler;
import org.codegist.crest.config.annotate.jaxrs.JaxRsAnnotations;
import org.codegist.crest.handler.DefaultResponseHandler;
import org.codegist.crest.io.RequestBuilderFactory;
import org.codegist.crest.io.RequestExecutor;
import org.codegist.crest.io.RetryingRequestExecutor;
import org.codegist.crest.io.http.*;
import org.codegist.crest.security.Authorization;
import org.codegist.crest.security.basic.BasicAuthorization;
import org.codegist.crest.security.handler.RefreshAuthorizationRetryHandler;
import org.codegist.crest.security.oauth.OAuthApi;
import org.codegist.crest.security.oauth.OAuthToken;
import org.codegist.crest.security.oauth.OAuthorization;
import org.codegist.crest.security.oauth.v1.OAuthApiV1Builder;
import org.codegist.crest.security.oauth.v1.OAuthenticatorV1;
import org.codegist.crest.serializer.*;
import org.codegist.crest.serializer.jackson.JacksonDeserializer;
import org.codegist.crest.serializer.jaxb.JaxbDeserializer;
import org.codegist.crest.serializer.primitive.*;
import org.codegist.crest.util.ComponentFactory;
import org.codegist.crest.util.ComponentRegistry;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.util.*;

import static java.util.Collections.singletonMap;
import static org.codegist.common.collect.Arrays.arrify;
import static org.codegist.common.collect.Collections.asSet;
import static org.codegist.common.collect.Maps.putIfAbsent;
import static org.codegist.crest.CRestConfig.*;
import static org.codegist.crest.io.http.HttpConstants.HTTP_UNAUTHORIZED;
import static org.codegist.crest.util.Placeholders.compile;

/**
 * <p><b>CRest</b> instance can be obtain through this builder.</p>
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class CRestBuilder {

    private static final int MIN_ERROR_STATUS_CODE = HttpConstants.HTTP_BAD_REQUEST;

    private final Map<String, Object> crestProperties = new HashMap<String, Object>();
    private final RequestBuilderFactory requestBuilderFactory = new HttpRequestBuilderFactory();
    private final Map<String, String> placeholders = new HashMap<String, String>();
    private final Map<String, EntityParamExtractor> httpEntityParamExtrators = new HashMap<String, EntityParamExtractor>(singletonMap("application/x-www-form-urlencoded", new UrlEncodedFormEntityParamExtractor()));

    private final Set<String> plainTextMimes = asSet("plain/text");
    private final Set<String> xmlMimes = asSet("application/xml", "text/xml");
    private final Set<String> jsonMimes = asSet("application/json", "application/javascript", "text/javascript", "text/json");

    private final Map<String, Object> xmlDeserializerConfig = new HashMap<String, Object>();

    private final Map<String, Object> jsonDeserializerConfig = new HashMap<String, Object>();

    private final ComponentRegistry.Builder<Class<? extends Annotation>, AnnotationHandler> annotationHandlerBuilder = new ComponentRegistry.Builder<Class<? extends Annotation>, AnnotationHandler>()
                            .defaultAs(NoOpAnnotationHandler.class)
                            .register(CRestAnnotations.getMapping());

    private final ComponentRegistry.Builder<String,Deserializer> mimeDeserializerBuilder = new ComponentRegistry.Builder<String,Deserializer>();
    private final ComponentRegistry.Builder<Class<?>,Deserializer> classDeserializerBuilder = new ComponentRegistry.Builder<Class<?>,Deserializer>()
                            .register(VoidDeserializer.class, Void.class, void.class)
                            .register(ByteArrayDeserializer.class, byte[].class)
                            .register(StringDeserializer.class, String.class)
                            .register(ByteWrapperDeserializer.class, Byte.class)
                            .register(BytePrimitiveDeserializer.class, byte.class)
                            .register(ShortWrapperDeserializer.class, Short.class)
                            .register(ShortPrimitiveDeserializer.class, short.class)
                            .register(IntegerWrapperDeserializer.class, Integer.class)
                            .register(IntegerPrimitiveDeserializer.class, int.class)
                            .register(LongWrapperDeserializer.class, Long.class)
                            .register(LongPrimitiveDeserializer.class, long.class)
                            .register(DoubleWrapperDeserializer.class, Double.class)
                            .register(DoublePrimitiveDeserializer.class, double.class)
                            .register(FloatWrapperDeserializer.class, Float.class)
                            .register(FloatPrimitiveDeserializer.class, float.class)
                            .register(BooleanWrapperDeserializer.class, Boolean.class)
                            .register(BooleanPrimitiveDeserializer.class, boolean.class)
                            .register(CharacterWrapperDeserializer.class, Character.class)
                            .register(CharacterPrimitiveDeserializer.class, char.class)
                            .register(DateDeserializer.class, Date.class)
                            .register(InputStreamDeserializer.class, InputStream.class)
                            .register(ReaderDeserializer.class, Reader.class);

    private final ComponentRegistry.Builder<Class<?>,Serializer> classSerializerBuilder = new ComponentRegistry.Builder<Class<?>,Serializer>()
                            .defaultAs(ToStringSerializer.class)
                            .register(DateSerializer.class, Date.class)
                            .register(BooleanSerializer.class, Boolean.class, boolean.class)
                            .register(FileSerializer.class, File.class)
                            .register(InputStreamSerializer.class, InputStream.class)
                            .register(ReaderSerializer.class, Reader.class);


    private Class<? extends ProxyFactory> proxyFactoryClass = JdkProxyFactory.class;
    private Class<? extends Deserializer> xmlDeserializer = JaxbDeserializer.class;
    private Class<? extends Deserializer> jsonDeserializer = JacksonDeserializer.class;
    private Class<? extends HttpChannelFactory> httpChannelFactoryClass = HttpURLConnectionHttpChannelFactory.class;
    private HttpChannelFactory httpChannelFactory;
    private ProxyFactory proxyFactory;
    private String auth;
    private String username;
    private String password;
    private OAuthToken consumerOAuthToken;
    private OAuthToken accessOAuthToken;
    private String accessTokenRefreshUrl;

    /**
     * <p>Build a <b>CRest</b> instance.</p>
     * <p><b>CRest</b> is an expensive object to create and should be created once at the application bootstrap and re-used. <b>CRest</b> instances are threadsafe.</p>
     * @return a <b>CRest</b> instance
     */
    public CRest build() {
        putIfAbsentAndNotNull(crestProperties, DefaultResponseHandler.MIN_ERROR_STATUS_CODE_PROP, MIN_ERROR_STATUS_CODE);
        putIfAbsentAndNotNull(crestProperties, CRestConfig.class.getName() + "#placeholders", compile(placeholders));
        CRestConfig crestConfig = new DefaultCRestConfig(crestProperties);

        ComponentRegistry<String,Deserializer> mimeDeserializerRegistry = buildDeserializerRegistry(crestConfig);
        ComponentRegistry<Class<?>,Deserializer> classDeserializerRegistry = classDeserializerBuilder.build(crestConfig);

        ComponentRegistry<Class<?>,Serializer> classSerializerRegistry = classSerializerBuilder.build(crestConfig);
        InterfaceConfigBuilderFactory icbf = new DefaultInterfaceConfigBuilderFactory(crestConfig, mimeDeserializerRegistry, classSerializerRegistry);
        ParamConfigBuilderFactory pcbf = new DefaultParamConfigBuilderFactory(crestConfig, classSerializerRegistry);
        putIfAbsentAndNotNull(crestProperties, ParamConfigBuilderFactory.class.getName(), pcbf);

        ProxyFactory pProxyFactory = getInstance(this.proxyFactory, this.proxyFactoryClass, crestConfig);
        HttpChannelFactory plainChannelFactory = getInstance(this.httpChannelFactory, this.httpChannelFactoryClass, crestConfig);

        Authorization authorization = buildAuthorization(plainChannelFactory);
        putIfAbsentAndNotNull(crestProperties, Authorization.class.getName(), authorization);

        ResponseDeserializer mimeResponseDeserializer = new ResponseDeserializerByMimeType(mimeDeserializerRegistry);
        ResponseDeserializer classResponseDeserializer = new ResponseDeserializerByClass(classDeserializerRegistry);
        ResponseDeserializer deserializersResponseDeserializer = new ResponseDeserializerByDeserializers();

        ResponseDeserializer baseResponseDeserializer = new ResponseDeserializerComposite(deserializersResponseDeserializer, mimeResponseDeserializer, classResponseDeserializer);
        ResponseDeserializer customTypeResponseDeserializer = new ResponseDeserializerComposite(classResponseDeserializer, mimeResponseDeserializer);

        RequestExecutor requestExecutor = buildRequestExecutor(plainChannelFactory, authorization, baseResponseDeserializer, customTypeResponseDeserializer);



        if(JaxRsAnnotations.isJaxRsAware()) {
            this.annotationHandlerBuilder.register(JaxRsAnnotations.getMapping());
        }

        InterfaceConfigFactory configFactory = new AnnotationDrivenInterfaceConfigFactory(icbf, annotationHandlerBuilder.build(crestConfig));

        return new DefaultCRest(pProxyFactory, requestExecutor, requestBuilderFactory, configFactory);
    }

    private static <K,V> void putIfAbsentAndNotNull(Map<K, V> map, K key, V value){
        if(value == null) {
            return;
        }
        putIfAbsent(map, key, value);
    }

    private static <T> T getInstance(T defaultIfSet, Class<? extends T> klass, CRestConfig crestConfig) {
        if(defaultIfSet != null) {
            return defaultIfSet;
        }
        try {
            return ComponentFactory.instantiate(klass, crestConfig);
        } catch (Exception e) {
            throw CRestException.handle(e);
        }
    }

    private RequestExecutor buildRequestExecutor(HttpChannelFactory plainChannelFactory, Authorization authorization, ResponseDeserializer baseResponseDeserializer, ResponseDeserializer customTypeResponseDeserializer){
        HttpChannelFactory channelFactory = plainChannelFactory;
        if(authorization != null) {
            channelFactory = new AuthorizationHttpChannelFactory(plainChannelFactory, authorization, httpEntityParamExtrators);
        }
        return new RetryingRequestExecutor(new HttpRequestExecutor(channelFactory, baseResponseDeserializer, customTypeResponseDeserializer), MIN_ERROR_STATUS_CODE);
    }

    private ComponentRegistry<String,Deserializer> buildDeserializerRegistry(CRestConfig crestConfig) {
        mimeDeserializerBuilder.register(jsonDeserializer, arrify(jsonMimes, String.class), jsonDeserializerConfig);
        mimeDeserializerBuilder.register(xmlDeserializer, arrify(xmlMimes, String.class), xmlDeserializerConfig);
        mimeDeserializerBuilder.register(StringDeserializer.class, arrify(plainTextMimes, String.class));
        return mimeDeserializerBuilder.build(crestConfig);
    }

    private Authorization buildAuthorization(HttpChannelFactory channelFactory) {
        if("oauth".equals(auth)) {
            return buildOAuthorization(channelFactory);
        }else if("basic".equals(auth)) {
            return buildBasicAuthorization();
        }else{
            return (Authorization) crestProperties.get(Authorization.class.getName());
        }
    }

    private Authorization buildBasicAuthorization() {
        return new BasicAuthorization(username, password);
    }

    private Authorization buildOAuthorization(HttpChannelFactory channelFactory) {
        OAuthenticatorV1 authenticator ;
        try {
            authenticator = new OAuthenticatorV1(consumerOAuthToken);
        } catch (Exception e) {
            throw CRestException.handle(e);
        }
        OAuthApi oAuthApi = buildOAuthApi(channelFactory);
        if(oAuthApi == null) {
            return new OAuthorization(accessOAuthToken, authenticator);
        }else{
            return new OAuthorization(accessOAuthToken, authenticator, oAuthApi);
        }
    }

    private OAuthApi buildOAuthApi(HttpChannelFactory channelFactory) {
        if(accessTokenRefreshUrl == null) {
            return null;
        }

        int indexOfProtocol = accessTokenRefreshUrl.indexOf("://") + 3;
        int indexOfServer = accessTokenRefreshUrl.indexOf('/', indexOfProtocol);
        String endPoint = accessTokenRefreshUrl.substring(0, indexOfServer);
        String path = accessTokenRefreshUrl.substring(indexOfServer);

        return new OAuthApiV1Builder(consumerOAuthToken, endPoint)
                .refreshAccessTokenFrom(path)
                .using(channelFactory)
                .build();
    }

    /**
     * Overrides the default {@link org.codegist.common.reflect.JdkProxyFactory} proxy factory to use for building user Interfaces proxies.
     * @param proxyFactory proxy factory class to use
     * @return current builder
     * @see org.codegist.common.reflect.CglibProxyFactory
     * @see org.codegist.common.reflect.JdkProxyFactory
     */
    public CRestBuilder setProxyFactory(Class<? extends ProxyFactory> proxyFactory) {
        this.proxyFactoryClass = proxyFactory;
        return this;
    }

    /**
     * Overrides the default {@link org.codegist.common.reflect.JdkProxyFactory} proxy factory to use for building user Interfaces proxies.
     * @param proxyFactory proxy factory to use
     * @return current builder
     * @see org.codegist.common.reflect.CglibProxyFactory
     * @see org.codegist.common.reflect.JdkProxyFactory
     */
    public CRestBuilder setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
        return this;
    }

    /**
     * Overrides the default {@link org.codegist.crest.io.http.HttpURLConnectionHttpChannelFactory} channel factory to use initiating http requests.
     * @param httpChannelFactory http channel factory class to use
     * @return current builder
     * @see org.codegist.crest.io.http.HttpClientHttpChannelFactory
     * @see org.codegist.crest.io.http.HttpURLConnectionHttpChannelFactory
     */
    public CRestBuilder setHttpChannelFactory(Class<? extends HttpChannelFactory> httpChannelFactory) {
        this.httpChannelFactoryClass = httpChannelFactory;
        return this;
    }

    /**
     * Overrides the default {@link org.codegist.crest.io.http.HttpURLConnectionHttpChannelFactory} channel factory to use initiating http requests.
     * @param httpChannelFactory http channel factory to use
     * @return current builder
     * @see org.codegist.crest.io.http.HttpClientHttpChannelFactory
     * @see org.codegist.crest.io.http.HttpURLConnectionHttpChannelFactory
     */
    public CRestBuilder setHttpChannelFactory(HttpChannelFactory httpChannelFactory) {
        this.httpChannelFactory = httpChannelFactory;
        return this;
    }

    /**
     * Overrides the default concurrency level (default is 1).
     * @param concurrencyLevel Concurrency level various <b>CRest</b> should be able to deal with
     * @return current builder
     * @see CRestConfig#CREST_CONCURRENCY_LEVEL
     * @see CRestConfig#getConcurrencyLevel()
     */
    public CRestBuilder setConcurrencyLevel(int concurrencyLevel) {
        return property(CREST_CONCURRENCY_LEVEL, concurrencyLevel);
    }

    /**
     * Overrides the default date format for serialization (default is "yyyy-MM-dd'T'HH:mm:ssZ").
     * @param dateFormat new date format
     * @return current builder
     * @see CRestConfig#CREST_DATE_FORMAT
     * @see CRestConfig#getDateFormat()
     */
    public CRestBuilder dateFormat(String dateFormat) {
        return property(CREST_DATE_FORMAT, dateFormat);
    }

    /**
     * Overrides the default boolean format for serialization (default are "true" and "false").
     * @param trueFormat format for TRUE
     * @param falseFormat format for FALSE
     * @return current builder
     * @see CRestConfig#CREST_BOOLEAN_TRUE
     * @see CRestConfig#CREST_BOOLEAN_FALSE
     * @see CRestConfig#getBooleanTrue()
     * @see CRestConfig#getBooleanFalse()
     */
    public CRestBuilder booleanFormat(String trueFormat, String falseFormat) {
        return property(CREST_BOOLEAN_TRUE, trueFormat).property(CREST_BOOLEAN_FALSE, falseFormat);
    }

    /**
     * <p>Adds all given properties to the {@link org.codegist.crest.CRestConfig} that will be passed to all <b>CRest</b> components.</p>
     * <p>Note that these properties can be used to override defaut <b>CRest</b>'s MethodConfig and ParamConfig values when none are provided through annotations.</p>
     * @param crestProperties properties
     * @return current builder
     * @see org.codegist.crest.CRestConfig
     * @see org.codegist.crest.config.MethodConfig
     * @see org.codegist.crest.config.ParamConfig
     */
    public CRestBuilder addProperties(Map<String, Object> crestProperties) {
        this.crestProperties.putAll(crestProperties);
        return this;
    }

    /**
     * <p>Adds given property to the {@link org.codegist.crest.CRestConfig} that will be passed to all <b>CRest</b> components.</p>
     * <p>Note that this property can be used to override defaut <b>CRest</b>'s MethodConfig and ParamConfig values when none are provided through annotations.</p>
     * @param name property name
     * @param value property value
     * @return current builder
     * @see org.codegist.crest.CRestConfig
     * @see org.codegist.crest.config.MethodConfig
     * @see org.codegist.crest.config.ParamConfig
     */
    public CRestBuilder property(String name, Object value) {
        return addProperties(singletonMap(name, value));
    }

    /**
     * <p>Sets all given properties to the {@link org.codegist.crest.CRestConfig} that will be passed to all <b>CRest</b> components.</p>
     * <p>Note that these properties can be used to override defaut <b>CRest</b>'s MethodConfig and ParamConfig values when none are provided through annotations.</p>
     * @param crestProperties properties
     * @return current builder
     * @see org.codegist.crest.CRestConfig
     * @see org.codegist.crest.config.MethodConfig
     * @see org.codegist.crest.config.ParamConfig
     */
    public CRestBuilder setProperties(Map<String, Object> crestProperties) {
        this.crestProperties.clear();
        return addProperties(crestProperties);
    }

    /**
     * <p>Sets the default endpoint all interfaces build through the resulting <b>CRest</b> instance will point at.</p>
     * <p>By setting it, it is not required anymore to set the @EndPoint annotation to the interfaces passed to the resulting <b>CRest</b> instance.</p>
     * <p>Shortcut to:</p>
     * <code><pre>
     * CRestBuilder.property(MethodConfig.METHOD_CONFIG_DEFAULT_ENDPOINT, endpoint)
     * </pre></code>
     * @param endpoint end point to point at
     * @return current builder
     */
    public CRestBuilder endpoint(String endpoint) {
        return property(MethodConfig.METHOD_CONFIG_DEFAULT_ENDPOINT, endpoint);
    }

    /**
     * <p>Adds all given placeholders to the string-based annotations placeholders replacement map.</p>
     * <p>Expects a map with keys being the placeholder name (used in string-based annotations) and with values being the value to be used as replacement.<p>
     * <p>So for the given annotation:</p>
     * <code><pre>
     * &#64;EndPoint(&quot;http://{app.host}:{app.port}&quot;)
     * </pre></code>
     * <p>The placeholders map will be:</p>
     * <code><pre>
     * String server = ...;
     * String port = ...;
     * Map&lt;String,String&gt; placeholders = new HashMap&lt;String,String&gt;();
     * placeholders.put("app.host", server);
     * placeholders.put("app.port", port);
     * CRestBuilder builder = ...;
     *
     * builder.addPlaceholders(placeholders);
     * </pre></code>
     * @param placeholders placeholder map to use for string-based annotation placeholder replacement
     * @return current builder
     */
    public CRestBuilder addPlaceholders(Map<String,String> placeholders) {
        this.placeholders.putAll(placeholders);
        return this;
    }

    /**
     * <p>Adds the given placeholder to the string-based annotations placeholders replacement map.</p>
     * <p>So for the given annotation:</p>
     * <code><pre>
     * &#64;EndPoint(&quot;http://{app.host}:{app.port}&quot;)
     * </pre></code>
     * <p>The placeholders will be replace if the builder is configured as follow:</p>
     * <code><pre>
     * String server = ...;
     * String port = ...;
     * CRestBuilder builder = ...;
     *
     * builder.placeholder("app.host", server)
     *        .placeholder("app.port", port);
     * </pre></code>
     * @param placeholder the placeholder to be replaced
     * @param value the value to replace the placeholder with in string-based annotations
     * @return current builder
     */
    public CRestBuilder placeholder(String placeholder, String value) {
        return addPlaceholders(singletonMap(placeholder, value));
    }

    /**
     * <p>Sets all given placeholders to the string-based annotations placeholders replacement map.</p>
     * <p>Expects a map with keys being the placeholder name (used in string-based annotations) and with values being the value to be used as replacement.<p>
     * <p>So for the given annotation:</p>
     * <code><pre>
     * &#64;EndPoint(&quot;http://{app.host}:{app.port}&quot;)
     * </pre></code>
     * <p>The placeholders map will be:</p>
     * <code><pre>
     * String server = ...;
     * String port = ...;
     * Map&lt;String,String&gt; placeholders = new HashMap&lt;String,String&gt;();
     * placeholders.put("app.host", server);
     * placeholders.put("app.port", port);
     * CRestBuilder builder = ...;
     *
     * builder.setPlaceholders(placeholders);
     * </pre></code>
     * @param placeholders placeholder map to use for string-based annotation placeholder replacement
     * @return current builder
     */
    public CRestBuilder setPlaceholders(Map<String,String> placeholders) {
        this.placeholders.clear();
        return addPlaceholders(placeholders);
    }

    /**
     * <p>Binds an annotation handler for the given annotation.</p>
     * <p>Can be used to tell <b>CRest</b> how to handle user-defined annotation used in interfaces.</p>
     * @param handler The user-defined annotation handler
     * @param annotationCls The user-defined annotation
     * @param <A> Used-defined annotation type
     * @return current builder
     */
    public <A extends Annotation> CRestBuilder bindAnnotationHandler(Class<? extends AnnotationHandler<A>> handler, Class<A> annotationCls){
        return bindAnnotationHandler(handler, annotationCls, Collections.<String, Object>emptyMap());
    }

    /**
     * <p>Binds an annotation handler for the given annotation.</p>
     * <p>Can be used to tell <b>CRest</b> how to handle user-defined annotation used in interfaces.</p>
     * @param handler The user-defined annotation handler
     * @param annotationCls The user-defined annotation
     * @param config State that will be passed to the annotation handler along with the CRestConfig object if the handler has declared a single argument constructor with CRestConfig parameter type
     * @param <A> Used-defined annotation type
     * @return current builder
     * @see org.codegist.crest.CRestConfig
     */
    public <A extends Annotation> CRestBuilder bindAnnotationHandler(Class<? extends AnnotationHandler<A>> handler, Class<A> annotationCls, Map<String, Object> config){
        annotationHandlerBuilder.register(handler, new Class[]{annotationCls}, config);
        return this;
    }

    /**
     * <p>Binds a deserializer to a list of response Content-Type mime-types.</p>
     * <p>By default, <b>CRest</b> handle the following types:</p>
     * <ul>
     *   <li>application/xml, text/xml for Xml deserialization</li>
     *   <li>application/json, application/javascript, text/javascript, text/json for Json deserialization</li>
     * </ul>
     * @param deserializer Deserializer class to use for the given mime-types
     * @param mimeTypes Response Content-Types to bind deserializer to
     * @return current builder
     */
    public CRestBuilder bindDeserializer(Class<? extends Deserializer> deserializer, String... mimeTypes) {
        return bindDeserializer(deserializer, mimeTypes, Collections.<String, Object>emptyMap());
    }

    /**
     * <p>Binds a deserializer to a list of response Content-Type mime-types.</p>
     * <p>By default, <b>CRest</b> handle the following types:</p>
     * <ul>
     *   <li>application/xml, text/xml for Xml deserialization</li>
     *   <li>application/json, application/javascript, text/javascript, text/json for Json deserialization</li>
     * </ul>
     * @param deserializer Deserializer class to use for the given mime-types
     * @param mimeTypes Response Content-Types to bind deserializer to
     * @param config State that will be passed to the deserializer along with the CRestConfig object if the deserializer has declared a single argument constructor with CRestConfig parameter type
     * @return current builder
     * @see org.codegist.crest.CRestConfig
     */
    public CRestBuilder bindDeserializer(Class<? extends Deserializer> deserializer, String[] mimeTypes, Map<String, Object> config) {
        this.mimeDeserializerBuilder.register(deserializer, mimeTypes, config);
        return this;
    }

    /**
     * <p>Binds a deserializer to a list of interface method's return types.</p>
     * <p>By default, <b>CRest</b> handle the following types:</p>
     * <ul>
     *   <li>all primitives and wrapper types</li>
     *   <li>java.io.InputStream</li>
     *   <li>java.io.Reader</li>
     * </ul>
     * <p>Meaning that any interface method return type can be by default one of these types.</p>
     * @param deserializer Deserializer class to use for the given interface method's return types
     * @param classes Interface method's return types to bind deserializer to
     * @return current builder
     */
    public CRestBuilder bindDeserializer(Class<? extends Deserializer> deserializer, Class<?>... classes) {
        return bindDeserializer(deserializer, classes, Collections.<String, Object>emptyMap());
    }

    /**
     * <p>Binds a deserializer to a list of interface method's return types.</p>
     * <p>By default, <b>CRest</b> handle the following types:</p>
     * <ul>
     *   <li>all primitives and wrapper types</li>
     *   <li>java.io.InputStream</li>
     *   <li>java.io.Reader</li>
     * </ul>
     * <p>Meaning that any interface method return type can be by default one of these types.</p>
     * @param deserializer Deserializer class to use for the given interface method's return types
     * @param classes Interface method's return types to bind deserializer to
     * @param config State that will be passed to the deserializer along with the CRestConfig object if the deserializer has declared a single argument constructor with CRestConfig parameter type
     * @return current builder
     * @see org.codegist.crest.CRestConfig
     */
    public CRestBuilder bindDeserializer(Class<? extends Deserializer> deserializer, Class<?>[] classes, Map<String, Object> config) {
        this.classDeserializerBuilder.register(deserializer, classes, config);
        return this;
    }

    /**
     * <p>Binds a serializer to a list of interface method's parameter types</p>
     * <p>By default, <b>CRest</b> handle the following types:</p>
     * <ul>
     *   <li>java.util.Date</li>
     *   <li>java.lang.Boolean</li>
     *   <li>java.io.File</li>
     *   <li>java.io.InputStream</li>
     *   <li>java.io.Reader</li>
     * </ul>
     * <p>Meaning any interface method parameter type can be by default one of these types and be serialized properly.</p>
     * @param serializer Serializer class to use to serialize the given interface method's parameter types
     * @param classes Interface method's parameter types to bind the serializer to
     * @return current builder
     * @see CRestConfig#getDateFormat()
     * @see CRestConfig#getBooleanTrue()
     * @see CRestConfig#getBooleanFalse()
     */
    public CRestBuilder bindSerializer(Class<? extends Serializer> serializer, Class<?>... classes){
        return bindSerializer(serializer, classes, Collections.<String, Object>emptyMap());
    }

    /**
     * <p>Binds a serializer to a list of interface method's parameter types</p>
     * <p>By default, <b>CRest</b> handle the following types:</p>
     * <ul>
     *   <li>java.util.Date</li>
     *   <li>java.lang.Boolean</li>
     *   <li>java.io.File</li>
     *   <li>java.io.InputStream</li>
     *   <li>java.io.Reader</li>
     * </ul>
     * <p>Meaning any interface method parameter type can be by default one of these types and be serialized properly.</p>
     * @param serializer Serializer class to use to serialize the given interface method's parameter types
     * @param classes Interface method's parameter types to bind the serializer to
     * @param config State that will be passed to the serializer along with the CRestConfig object if the serializer has declared a single argument constructor with CRestConfig parameter type
     * @return current builder
     * @see CRestConfig#getDateFormat()
     * @see CRestConfig#getBooleanTrue()
     * @see CRestConfig#getBooleanFalse()
     */
    public CRestBuilder bindSerializer(Class<? extends Serializer> serializer, Class<?>[] classes, Map<String, Object> config){
        classSerializerBuilder.register(serializer, classes, config);
        return this;
    }

    /**
     * <p>Configures the resulting <b>CRest</b> instance to authenticate all requests using OAuth 1.0</p>
     * @param consumerKey consumer key to use
     * @param consumerSecret consumer secret to use
     * @param accessToken access token to use
     * @param accessTokenSecret access token secret to use
     * @return current builder
     */
    public CRestBuilder oauth(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        return oauth(consumerKey,consumerSecret,accessToken, accessTokenSecret, null, null);
    }

    /**
     * <p>Configures the resulting <b>CRest</b> instance to authenticate all requests using OAuth 1.0</p>
     * <p>When the end-point indicates the access token is expired, <b>CRest</b> will use the given session handle and access toklen refresh url to refresh the access token used as defined in <a href="http://oauth.googlecode.com/svn/spec/ext/session/1.0/drafts/1/spec.html">OAuth Session 1.0 Draft 1</a></p>
     * @param consumerKey consumer key to use
     * @param consumerSecret consumer secret to use
     * @param accessToken access token to use
     * @param accessTokenSecret access token secret to use
     * @param sessionHandle session handle to use to refresh an expired access token
     * @param accessTokenRefreshUrl url to use to refresh an expired access token
     * @return current builder
     * @see <a href="http://oauth.googlecode.com/svn/spec/ext/session/1.0/drafts/1/spec.html">OAuth Session 1.0 Draft 1</a>
     */
    public CRestBuilder oauth(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String sessionHandle, String accessTokenRefreshUrl) {
        this.auth = "oauth";
        if(sessionHandle != null) {
            this.accessOAuthToken = new OAuthToken(accessToken, accessTokenSecret, singletonMap("oauth_session_handle", sessionHandle));
        }else{
            this.accessOAuthToken = new OAuthToken(accessToken, accessTokenSecret);
        }
        if(accessTokenRefreshUrl != null) {
            property(MethodConfig.METHOD_CONFIG_DEFAULT_RETRY_HANDLER, RefreshAuthorizationRetryHandler.class);
            property(RefreshAuthorizationRetryHandler.UNAUTHORIZED_STATUS_CODE_PROP, HTTP_UNAUTHORIZED);
        }
        this.accessTokenRefreshUrl = accessTokenRefreshUrl;
        this.consumerOAuthToken = new OAuthToken(consumerKey, consumerSecret);
        return this;
    }

    /**
     * <p>Configures the resulting <b>CRest</b> instance to authenticate all requests using Basic Auth</p>
     * @param username user name to authenticate the requests with
     * @param password password to authenticate the requests with
     * @return current builder
     */
    public CRestBuilder basicAuth(String username, String password) {
        this.auth = "basic";
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * <p>Sets an entity parameters extractor for authenticated requests.</p>
     * <p>When <b>CRest</b> is configured to authenticate requests, it uses an {@link org.codegist.crest.io.http.EntityParamExtractor} to extract parameters from the request entity to include them in the authentication process.</p>
     * <p>A common use case is to deal with OAuth and multipart as some provider will use some multipart parameters and some not (binaries usually) in order to sign a request</p>
     * @param entityContentType Entity Content-Type to bind the extractor to
     * @param entityParamExtractor Extractor
     * @return current builder
     */
    public CRestBuilder extractsEntityAuthParamsWith(String entityContentType, EntityParamExtractor entityParamExtractor){
        this.httpEntityParamExtrators.put(entityContentType, entityParamExtractor);
        return this;
    }

    /**
     * <p>Adds the given list of response Content-Type mime-types to be consider as JSON mime-types and to be deserialized by the default JSON Deserializer</p>
     * <p>By default, <b>CRest</b> consider to be JSON mime-type the following response Content-Type:</p>
     * <ul>
     * <li>application/json</li>
     * <li>application/javascript</li>
     * <li>text/javascript</li>
     * <li>text/json</li>
     * </ul>
     * @param mimeTypes Response Content-Type mime-types to be consider as JSON mime-types
     * @return current builder
     */
    public CRestBuilder bindJsonDeserializerWith(String... mimeTypes) {
        this.jsonMimes.addAll(asSet(mimeTypes));
        return this;
    }

    /**
     * <p>Adds the given list of response Content-Type mime-types to be consider as XML mime-types and to be deserialized by the default XML Deserializer</p>
     * <p>By default, <b>CRest</b> consider to be XML mime-type the following response Content-Type:</p>
     * <ul>
     * <li>application/xml</li>
     * <li>text/xml</li>
     * </ul>
     * @param mimeTypes Response Content-Type mime-types to be consider as XML mime-types
     * @return current builder
     */
    public CRestBuilder bindXmlDeserializerWith(String... mimeTypes) {
        this.xmlMimes.addAll(asSet(mimeTypes));
        return this;
    }

    /**
     * <p>Adds the given list of response Content-Type mime-types to be consider as plain-text mime-types and to be deserialized to plain String</p>
     * <p>By default, <b>CRest</b> consider to be plain-text mime-type the following response Content-Type:</p>
     * <ul>
     * <li>plain/text</li>
     * </ul>
     * @param mimeTypes Response Content-Type mime-types to be consider as plain-text mime-types
     * @return current builder
     */
    public CRestBuilder bindPlainTextDeserializerWith(String... mimeTypes) {
        this.plainTextMimes.addAll(asSet(mimeTypes));
        return this;
    }

    /**
     * <p>Overrides the default {@link org.codegist.crest.serializer.jaxb.JaxbDeserializer} XML deserializer with the given one</p>
     * <p>By default, <b>CRest</b> will use this deserializer for the following response Content-Type:</p>
     * <ul>
     * <li>application/xml</li>
     * <li>text/xml</li>
     * </ul>
     * @param deserializer deserializer to use for XML response Content-Type requests
     * @return current builder
     * @see org.codegist.crest.serializer.jaxb.JaxbDeserializer
     * @see org.codegist.crest.serializer.simplexml.SimpleXmlDeserializer
     */
    public CRestBuilder deserializeXmlWith(Class<? extends Deserializer> deserializer) {
        return deserializeXmlWith(deserializer, Collections.<String, Object>emptyMap());
    }

    /**
     * <p>Overrides the default {@link org.codegist.crest.serializer.jaxb.JaxbDeserializer} XML deserializer with the given one</p>
     * <p>By default, <b>CRest</b> will use this deserializer for the following response Content-Type:</p>
     * <ul>
     * <li>application/xml</li>
     * <li>text/xml</li>
     * </ul>
     * @param deserializer deserializer to use for XML response Content-Type requests
     * @param config State that will be passed to the deserializer along with the CRestConfig object if the deserializer has declared a single argument constructor with CRestConfig parameter type
     * @return current builder
     * @see org.codegist.crest.serializer.jaxb.JaxbDeserializer
     * @see org.codegist.crest.serializer.simplexml.SimpleXmlDeserializer
     */
    public CRestBuilder deserializeXmlWith(Class<? extends Deserializer> deserializer, Map<String, Object> config) {
        this.xmlDeserializer = deserializer;
        this.xmlDeserializerConfig.clear();
        this.xmlDeserializerConfig.putAll(config);
        return this;
    }

    /**
     * <p>Overrides the default {@link org.codegist.crest.serializer.jackson.JacksonDeserializer} JSON deserializer with the given one</p>
     * <p>By default, <b>CRest</b> will use this deserializer for the following response Content-Type:</p>
     * <ul>
     * <li>application/json</li>
     * <li>application/javascript</li>
     * <li>text/javascript</li>
     * <li>text/json</li>
     * </ul>
     * @param deserializer deserializer to use for JSON response Content-Type requests
     * @return current builder
     * @see org.codegist.crest.serializer.jackson.JacksonDeserializer
     */
    public CRestBuilder deserializeJsonWith(Class<? extends Deserializer> deserializer) {
        return deserializeJsonWith(deserializer, Collections.<String, Object>emptyMap());
    }

    /**
     * <p>Overrides the default {@link org.codegist.crest.serializer.jackson.JacksonDeserializer} JSON deserializer with the given one</p>
     * <p>By default, <b>CRest</b> will use this deserializer for the following response Content-Type:</p>
     * <ul>
     * <li>application/json</li>
     * <li>application/javascript</li>
     * <li>text/javascript</li>
     * <li>text/json</li>
     * </ul>
     * @param deserializer deserializer to use for JSON response Content-Type requests
     * @param config State that will be passed to the deserializer along with the CRestConfig object if the deserializer has declared a single argument constructor with CRestConfig parameter type
     * @return current builder
     * @see org.codegist.crest.serializer.jackson.JacksonDeserializer
     */
    public CRestBuilder deserializeJsonWith(Class<? extends Deserializer> deserializer, Map<String, Object> config) {
        this.jsonDeserializer = deserializer;
        this.jsonDeserializerConfig.clear();
        this.jsonDeserializerConfig.putAll(config);
        return this;
    }

}