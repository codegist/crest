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

package org.codegist.crest.param;

import org.codegist.crest.BaseCRestTest;
import org.codegist.crest.CRest;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.codegist.common.net.Urls.encode;
import static org.codegist.crest.param.BaseParams.assertSendsEquals;
import static org.junit.Assert.assertEquals;

public class HeadersTest extends BaseCRestTest<Headers> {

    public HeadersTest(CRest crest) {
        super(crest, Headers.class);
    }

    @Parameterized.Parameters
    public static Collection<CRest[]> getData() {
        return crest(byRestServices());
    }

    @Test
    public void testSend() {
        String p1 = UTF8_VALUE;
        int p2 = 1983;
        float[] p3 = new float[]{1.2f, 2.3f, 3.4f};

        assertSendsEquals(toTest, p1, p2, p3);
    }

    @Test
    public void testDefaultLists() {
        String p11 = "p1-val1", p12 = UTF8_VALUE;
        boolean p21 = true, p22 = false;
        Integer p31 = 31, p32 = 32;
        Long p41 = 41l, p42 = 42l;

        String[] p1 = {p11, p12};
        boolean[] p2 = {p21, p22};
        List<Integer> p3 = asList(p31, p32);
        Set<Long> p4 = new LinkedHashSet<Long>(asList(p41, p42));

        String actual = toTest.defaultLists(p1,p2,p3,p4);

        String expected = String.format("defaultLists() p1=%s p2=%s p3=%s p4=%s", Arrays.toString(p1),Arrays.toString(p2),p3,p4);
        assertEquals(expected, actual);
    }

    @Test
    public void testMergingLists() {
        String p1Sep = "(def)";
        String p2Sep = "(p2)";
        String p3Sep = "(p3)";
        String p4Sep = "(p4)";
        String p11 = "p1-val1", p12 = UTF8_VALUE;
        boolean p21 = true, p22 = false;
        Integer p31 = 31, p32 = 32;
        Long p41 = 41l, p42 = 42l;

        String[] p1 = {p11, p12};
        boolean[] p2 = {p21, p22};
        List<Integer> p3 = asList(p31, p32);
        Set<Long> p4 = new LinkedHashSet<Long>(asList(p41, p42));

        String actual = toTest.mergingLists(p1,p2,p3,p4);

        String expected = String.format("mergingLists() p1=%s%s%s p2=%s%s%s p3=%s%s%s p4=%s%s%s",
                p11,p1Sep,p12,
                p21,p2Sep,p22,
                p31,p3Sep,p32,
                p41,p4Sep,p42);
        assertEquals(expected, actual);
    }

    @Test
    public void testEncodings() {
        String p21 = "&";
        String p22 = UTF8_VALUE;
        String p1 = UTF8_VALUE;
        Collection<String> p2 = asList(p21, p22);
        String actual = toTest.encodings(p1, p2);

        String expected = String.format("encodings() p1=%s p2=%s", p1, p2);
        assertEquals(expected, actual);
    }

    @Test
    public void testPreEncoded() throws UnsupportedEncodingException {
        String p21 = encode("&", "utf-8");
        String p22 = encode(UTF8_VALUE, "utf-8");
        String p1 = encode(UTF8_VALUE, "utf-8");
        Collection<String> p2 = asList(p21, p22);
        String actual = toTest.preEncoded(p1, p2);

        String expected = String.format("preEncoded() p1=%s p2=%s", p1, p2);
        assertEquals(expected, actual);
    }


}