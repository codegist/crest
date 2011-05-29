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

package org.codegist.crest.serializer;

import org.codegist.common.lang.Strings;
import org.codegist.crest.CRestProperty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class DateSerializer implements Serializer<Date> {

    public static final String DEFAULT_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    private final DateFormat formatter;
    private final FormatType formatType;

    public DateSerializer() {
        this(DEFAULT_DATEFORMAT);
    }
    public DateSerializer(Map<String,Object> customProperties) {
        this(Strings.defaultIfBlank((String) customProperties.get(CRestProperty.SERIALIZER_DATE_FORMAT), DateSerializer.DEFAULT_DATEFORMAT));
    }
    public DateSerializer(String dateFormat) {
        DateFormat formatter;
        FormatType formatType;
        try {
            formatType = FormatType.valueOf(dateFormat);
            formatter = null;
        } catch (IllegalArgumentException e) {
            formatType = null;
            formatter = new SimpleDateFormat(dateFormat);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        this.formatter = formatter;
        this.formatType = formatType;
    }

    public String serialize(Date value) {
        if(formatter != null) {
             synchronized (formatter) {
                 return formatter.format(value);
             }
        }else{
            return String.valueOf(formatType.format(value));
        }
    }


    private enum FormatType {
        Millis(1),
        Seconds(1000),
        Minutes(1000*60),
        Hours(1000*60*60),
        Days(1000*60*60*24);
        private final long div;

        FormatType(long div) {
            this.div = div;
        }

        long format(Date date) {
            return date.getTime() / div;
        }
    }
}
