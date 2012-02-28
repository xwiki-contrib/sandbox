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
package org.xwiki.portlet.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.portlet.model.RequestType;
import org.xwiki.portlet.util.QueryStringParser;

/**
 * Maps servlet URLs to portlet request types.
 * 
 * @version $Id$
 */
public class URLRequestTypeMapper
{
    /**
     * Interface used to test if an URL matches some description.
     */
    private interface URLMatcher
    {
        /**
         * @param url an URL
         * @return {@code true} if the URL matches the description, {@code false} otherwise
         */
        boolean matches(String url);
    }

    /**
     * Matches URLs with a specified prefix.
     */
    private static class PrefixURLMatcher implements URLMatcher
    {
        /**
         * The URL prefix.
         */
        private final String prefix;

        /**
         * Creates a new matcher that matches URLs with the give prefix.
         * 
         * @param prefix the URL prefix
         */
        public PrefixURLMatcher(String prefix)
        {
            this.prefix = prefix;
        }

        @Override
        public boolean matches(String url)
        {
            return url.startsWith(prefix);
        }
    }

    /**
     * Matches URLs with a specified suffix.
     */
    private static class SuffixURLMatcher implements URLMatcher
    {
        /**
         * The URL suffix.
         */
        private final String suffix;

        /**
         * Creates a new matcher that matches URLs with the given suffix.
         * 
         * @param suffix the URL suffix
         */
        public SuffixURLMatcher(String suffix)
        {
            this.suffix = suffix;
        }

        @Override
        public boolean matches(String url)
        {
            return url.endsWith(suffix);
        }
    }

    /**
     * Matches URLs that have the specified query string parameters.
     */
    private static class QueryStringURLMatcher implements URLMatcher
    {
        /**
         * The object used to parse the query strings.
         */
        private final QueryStringParser queryStringParser = new QueryStringParser();

        /**
         * The expected query string parameters.
         */
        private final Map<String, List<String>> expectedParameters;

        /**
         * Creates a new matcher that matches URLs with the specified query string.
         * 
         * @param queryString the query string
         */
        public QueryStringURLMatcher(String queryString)
        {
            expectedParameters = queryStringParser.parse(queryString);
        }

        @Override
        public boolean matches(String url)
        {
            int queryStringPos = url.lastIndexOf('?');
            String queryString = queryStringPos < 0 ? "" : url.substring(queryStringPos + 1);
            Map<String, List<String>> actualParameters = queryStringParser.parse(queryString);
            // Test if the actual parameters include the expected parameters.
            for (Map.Entry<String, List<String>> entry : expectedParameters.entrySet()) {
                List<String> actualValues = actualParameters.get(entry.getKey());
                if (actualValues == null) {
                    return false;
                }
                for (String expectedValue : entry.getValue()) {
                    if (!actualValues.contains(expectedValue)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * The logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(URLRequestTypeMapper.class);

    /**
     * The list of matchers for each request type.
     */
    private final Map<RequestType, List<URLMatcher>> matchers = new HashMap<RequestType, List<URLMatcher>>();

    /**
     * Creates a new instance that maps URL to {@link RequestType} based on a configuration file.
     */
    public URLRequestTypeMapper()
    {
        InputStream is = URLRequestTypeMapper.class.getClassLoader().getResourceAsStream("urlmapping.cfg");
        if (is != null) {
            try {
                readURLMapping(is);
            } catch (IOException e) {
                LOGGER.error("Failed to read from the URL mapping file.", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close URL mapping file.", e);
                }
            }
        } else {
            LOGGER.warn("URL mapping file not found.");
        }
    }

    /**
     * Reads the URL mapping from the given input stream.
     * 
     * @param is the input stream to read the URL mapping from
     * @throws IOException if reading the URL mapping fails
     */
    private void readURLMapping(InputStream is) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        RequestType currentRequestType = null;
        do {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.endsWith("=")) {
                try {
                    currentRequestType = RequestType.valueOf(line.substring(0, line.length() - 1).toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Keep the previous action.
                }
            } else if (line.startsWith("/")) {
                addURLMatcher(currentRequestType, new PrefixURLMatcher(line));
            } else if (line.startsWith("?")) {
                addURLMatcher(currentRequestType, new QueryStringURLMatcher(line.substring(1)));
            } else {
                addURLMatcher(currentRequestType, new SuffixURLMatcher(line));
            }
        } while (true);
    }

    /**
     * Adds a new URL matcher.
     * 
     * @param type the request type
     * @param matcher the matcher to be added
     */
    private void addURLMatcher(RequestType type, URLMatcher matcher)
    {
        List<URLMatcher> matchersForType = matchers.get(type);
        if (matchersForType == null) {
            matchersForType = new ArrayList<URLMatcher>();
            matchers.put(type, matchersForType);
        }
        matchersForType.add(matcher);
    }

    /**
     * @param contextFreeURL a string representation of an URL without including the server information and context path
     * @return the request type associated with the given URL
     */
    public RequestType getType(String contextFreeURL)
    {
        for (Map.Entry<RequestType, List<URLMatcher>> entry : matchers.entrySet()) {
            for (URLMatcher matcher : entry.getValue()) {
                if (matcher.matches(contextFreeURL)) {
                    return entry.getKey();
                }
            }
        }
        return RequestType.RENDER;
    }
}
