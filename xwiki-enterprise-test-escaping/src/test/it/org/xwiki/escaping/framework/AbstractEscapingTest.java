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

package org.xwiki.escaping.framework;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.xwiki.escaping.suite.FileTest;
import org.xwiki.validator.ValidationError;

/**
 * Abstract base class for escaping tests. Implements common initialization pattern and some utility methods
 * like URL escaping, retrieving page content by URL etc. Subclasses need to implement parsing
 * and custom tests.
 * <p>
 * Note: JUnit4 requires tests to have one public default constructor, subclasses will need to implement
 * it and pass pattern matcher to match file names they can handle.</p>
 * <p>
 * Starting and stopping XWiki server is handled transparently for all subclasses, tests can be run
 * alone using -Dtest=ClassName, a parent test suite should start XWiki server before running all
 * tests for efficiency using {@link SingleXWikiExecutor}.</p>
 * <p>
 * The following configuration properties are supported (set in maven):
 * <ul>
 * <li>pattern (optional): Additional pattern to select files to be tested (use -Dpattern="substring-regex").
 *                         Matches all files if empty.</li>
 * <li>filesProduceNoOutput (optional): List of files that are expected to produce empty response</li>
 * <li>patternExcludeFiles (optional): List of RegEx patterns to exclude files from the tests</li>
 * </ul></p>
 * 
 * @version $Id$
 * @since 2.5
 */
public abstract class AbstractEscapingTest implements FileTest
{
    /** Static part of the test URL. */
    private static final String URL_START = "http://127.0.0.1:8080/xwiki/bin/";

    /** HTTP client shared between all subclasses. */
    private static HttpClient client;

    /** File name of the template to use. */
    protected String name;

    /** User provided data found in the file. */
    protected Set<String> userInput;

    /** Pattern used to match files by name. */
    private Pattern namePattern;

    /**
     * Test fails if response is empty, but output is expected and vice versa.
     * To set to false, add file name to "filesProduceNoOutput" 
     */
    protected boolean shouldProduceOutput = true;

    /**
     * Start XWiki server if run alone.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void init() throws Exception
    {
        SingleXWikiExecutor.getExecutor().start();
    }

    /**
     * Stop XWiki server if run alone.
     * 
     * @throws Exception on errors
     */
    @AfterClass
    public static void shutdown() throws Exception
    {
        SingleXWikiExecutor.getExecutor().stop();
    }

    /**
     * Change multi-language mode. Note: XWiki server must already be started.
     * 
     * @param enabled enable the multi-language mode if true, disable otherwise
     */
    protected static void setMultiLanguageMode(boolean enabled)
    {
        String url = AbstractEscapingTest.URL_START + "save/XWiki/XWikiPreferences?";
        url += "XWiki.XWikiPreferences_0_languages=&XWiki.XWikiPreferences_0_multilingual=";
        AbstractEscapingTest.getUrlContent(url + (enabled ? 1 : 0));
    }

    /**
     * Create new AbstractEscapingTest
     * 
     * @param fileNameMatcher regex pattern used to filter files by name
     */
    protected AbstractEscapingTest(Pattern fileNameMatcher)
    {
        namePattern = fileNameMatcher;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.escaping.suite.FileTest#initialize(java.lang.String, java.io.Reader)
     */
    public boolean initialize(String name, final Reader reader)
    {
        this.name = name;
        if (!fileNameMatches(name) || !patternMatches(name) || isExcludedFile(name)) {
            return false;
        }

        this.shouldProduceOutput = isOutputProducingFile(name);
        this.userInput = parse(reader);
        return !userInput.isEmpty();
    }

    /**
     * Check if the internal file name pattern matches the given file name.
     * 
     * @param fileName file name to check
     * @return true if the name matches, false otherwise
     */
    protected boolean fileNameMatches(String fileName)
    {
        return namePattern != null && namePattern.matcher(fileName).matches();
    }

    /**
     * Check if the system property "pattern" matches (substring regular expression) the file name.
     * Empty pattern matches everything.
     * 
     * @param fileName file nmae to check
     * @return true if the pattern matches, false otherwise
     */
    protected boolean patternMatches(String fileName)
    {
        String pattern = System.getProperty("pattern", "");
        if (pattern == null || pattern.equals("")) {
            return true;
        }
        return Pattern.matches(".*" + pattern + ".*", fileName);
    }

    /**
     * Check if the given file should be excluded from the tests. The default implementation checks
     * "patternExcludeFiles" property (set in maven build configuration).
     * 
     * @param fileName file name to check
     * @return true if the file should be excluded, false otherwise
     */
    protected boolean isExcludedFile(String fileName)
    {
        for (String pattern : System.getProperty("patternExcludeFiles", "").split("\\s+")) {
            Pattern exclude = Pattern.compile(pattern);
            if (exclude.matcher(fileName).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the given file name should produce output. The default implementation checks
     * "filesProduceNoOutput" property (set in maven build configuration).
     * 
     * @param fileName file name to check
     * @return true if the file is expected to produce some output when requested from the server, false otherwise
     */
    protected boolean isOutputProducingFile(String fileName)
    {
        for (String file : System.getProperty("filesProduceNoOutput", "").split("\\s+")) {
            if (file.trim().equals(fileName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parse the file and collect parameters controlled by the user.
     * 
     * @param reader the reader associated with the file
     * @return collection of user-controlled input parameters
     */
    protected abstract Set<String> parse(Reader reader);

    /**
     * Download a page from the server and return its content. Throws a {@link RuntimeException}
     * on connection problems etc.
     * 
     * @param url URL of the page
     * @return content of the page
     */
    protected static InputStream getUrlContent(String url)
    {
        GetMethod get = new GetMethod(url);
        get.setFollowRedirects(true);
        get.setDoAuthentication(true);
        get.addRequestHeader("Authorization", "Basic " + new String(Base64.encodeBase64("Admin:admin".getBytes())));

        // make the request
        HttpClient client = AbstractEscapingTest.getClient();
        try {
            int statusCode = client.executeMethod(get);
            // ignore 404 (the page is still rendered)
            if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NOT_FOUND) {
                throw new RuntimeException("HTTP GET request returned status " + statusCode + " for URL: " + url);
            }

            // get the data, converting to utf-8
            String str = get.getResponseBodyAsString();
            if (str == null) {
                return null;
            }
            return new ByteArrayInputStream(str.getBytes("utf-8"));
        } catch (IOException exception) {
            throw new RuntimeException("Error retrieving URL: " + url + "\n" + exception);
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * URL-escape given string.
     * 
     * @param str string to escape, "" is used if null
     * @return URL-escaped {@code str}
     */
    protected final String escapeUrl(String str)
    {
        try {
            return URLEncoder.encode(str == null ? "" : str, "utf-8");
        } catch (UnsupportedEncodingException exception) {
            // should not happen
            throw new RuntimeException("Should not happen: ", exception);
        }
    }

    /**
     * Get an instance of the HTTP client to use.
     * 
     * @return HTTP client initialized with admin credentials
     */
    protected static HttpClient getClient()
    {
        if (AbstractEscapingTest.client == null) {
            HttpClient adminClient = new HttpClient();
            Credentials defaultcreds = new UsernamePasswordCredentials("Admin", "admin");
            adminClient.getState().setCredentials(AuthScope.ANY, defaultcreds);
            HttpClientParams clientParams = new HttpClientParams();
            clientParams.setSoTimeout(2000);
            HttpConnectionManagerParams connectionParams = new HttpConnectionManagerParams();
            connectionParams.setConnectionTimeout(30000);
            adminClient.getHttpConnectionManager().setParams(connectionParams);
            AbstractEscapingTest.client = adminClient;
        }
        return AbstractEscapingTest.client;
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name + (shouldProduceOutput ? " " : " (NO OUTPUT) ") + userInput;
    }

    /**
     * Check for unescaped data in the given {@code content}. Throws {@link EscapingError} on failure,
     * {@link RuntimeException} on errors.
     * 
     * @param url URL used in the test
     * @param description description of the test
     */
    protected void checkUnderEscaping(String url, String description)
    {
        // TODO better use log4j
        System.out.println("Testing URL: " + url);

        InputStream content = AbstractEscapingTest.getUrlContent(url);
        String where = "  Template: " + name + "\n  URL: " + url;
        Assert.assertNotNull("Response is null\n" + where, content);
        XMLEscapingValidator validator = new XMLEscapingValidator();
        validator.setShouldBeEmpty(!this.shouldProduceOutput);
        validator.setDocument(content);
        List<ValidationError> errors;
        try {
            errors = validator.validate();
        } catch (EscapingError error) {
            // most probably false positive, generate an error instead of failing the test
            throw new RuntimeException(EscapingError.formatMessage(error.getMessage(), name, url, null));
        }
        if (!errors.isEmpty()) {
            throw new EscapingError("Escaping test failed.", name, url, errors);
        }
    }

    /**
     * Create the target URL from the given parameters. URL-escapes everything.
     * 
     * @param action action to use, "view" is used if null
     * @param space space name to use, "Main" is used if null
     * @param page page name to use, "WebHome" is used if null
     * @param parameters list of parameters with values, parameters are omitted if null, "" is used is a value is null
     * @return the resulting absolute URL
     */
    protected final String createUrl(String action, String space, String page, Map<String, String> parameters)
    {
        if (action == null) {
            action = "view";
        }
        if (space == null) {
            space = "Main";
        }
        if (page == null) {
            page = "WebHome";
        }
        String url = URL_START + escapeUrl(action) + "/" + escapeUrl(space) + "/" + escapeUrl(page);
        if (parameters == null) {
            return url;
        }
        String delimiter = "?";
        for (String parameter : parameters.keySet()) {
            if (parameter != null && !parameter.equals("")) {
                String value = parameters.get(parameter);
                url += delimiter + escapeUrl(parameter) + "=" + escapeUrl(value);
            }
            delimiter = "&";
        }
        return url;
    }
}
