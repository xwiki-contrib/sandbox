/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.portlet.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses URL query strings.
 * 
 * @version $Id$
 */
public class QueryStringParser
{
    /**
     * Parses the given query string without decoding the parameter names and values.
     * 
     * @param queryString the URL query string to be parsed
     * @return the parameter map
     */
    public Map<String, List<String>> parse(String queryString)
    {
        try {
            return parse(queryString, null);
        } catch (UnsupportedEncodingException e) {
            // We should never get here.
            return null;
        }
    }

    /**
     * Parses the given query string and decodes the parameter names and values using the specified encoding.
     * 
     * @param queryString the URL query string to be parsed
     * @param encoding the character encoding to be used for decoding the parameter names and values
     * @return the parameter map
     * @throws UnsupportedEncodingException if the specified encoding is not supported
     */
    public Map<String, List<String>> parse(String queryString, String encoding) throws UnsupportedEncodingException
    {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        String[] keyValuePairs = queryString.split("&");
        for (int i = 0; i < keyValuePairs.length; i++) {
            if (keyValuePairs[i].length() == 0) {
                continue;
            }
            String[] pair = keyValuePairs[i].split("=", 2);
            String name = pair[0];
            String value = pair.length == 1 ? "" : pair[1];
            if (encoding != null) {
                name = URLDecoder.decode(name, encoding);
                value = URLDecoder.decode(value, encoding);
            }
            List<String> values = parameters.get(name);
            if (values == null) {
                values = new ArrayList<String>();
                parameters.put(name, values);
            }
            values.add(value);
        }
        return parameters;
    }
}
