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
 *
 */
package org.xwiki.csrftoken;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.csrftoken.internal.DefaultCSRFToken;
import org.xwiki.model.ModelContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.test.AbstractMockingComponentTest;
import org.xwiki.test.annotation.ComponentTest;


/**
 * Tests for the {@link CSRFToken} component.
 * 
 * @version $Id: $
 * @since 2.4
 */
@ComponentTest(value = DefaultCSRFToken.class)
public class DefaultCSRFTokenTest extends AbstractMockingComponentTest
{
    /** Resubmission URL. */
    private static final String resubmitUrl = "http://host/xwiki/bin/view/XWiki/Resubmit";

    /** URL of the current document. */
    private static final String mockDocumentUrl = "http://host/xwiki/bin/save/Main/Test";

    /** Query part of the document URL. */
    private static final String mockQuery = "form_token=&a=b&form_token=xyz&c=d&form_token=xyz";

    /** Tested CSRF token component. */
    private CSRFToken csrf;

    /**
     * {@inheritDoc}
     * @see org.xwiki.test.AbstractMockingComponentTest#setUp()
     */
    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        // set up mocked dependencies

        // document access bridge
        final DocumentAccessBridge mockDocumentAccessBridge = getComponentManager().lookup(DocumentAccessBridge.class);
        final CopyStringMatcher someValue = new CopyStringMatcher(resubmitUrl + "?", "");
        getMockery().checking(new Expectations() {{
            allowing(mockDocumentAccessBridge).getCurrentUser();
                will(returnValue("XWiki.Admin"));
            allowing(mockDocumentAccessBridge).getDocumentURL(with(any(DocumentReference.class)), with("view"),
                    with(someValue), with(aNull(String.class)));
                will(someValue);
        }});
        // configuration
        final CSRFTokenConfiguration mockConfiguration = getComponentManager().lookup(CSRFTokenConfiguration.class);
        getMockery().checking(new Expectations() {{
            allowing(mockConfiguration).isEnabled();
                will(returnValue(true));
        }});
        // request
        final HttpServletRequest httpRequest = getMockery().mock(HttpServletRequest.class);
        final ServletRequest servletRequest = new ServletRequest(httpRequest);
        getMockery().checking(new Expectations() {{
            allowing(httpRequest).getRequestURL();
                will(returnValue(new StringBuffer(mockDocumentUrl)));
            allowing(httpRequest).getQueryString();
                will(returnValue(mockQuery));
        }});
        // container
        final Container mockContainer = getComponentManager().lookup(Container.class);
        getMockery().checking(new Expectations() {{
            allowing(mockContainer).getRequest();
                will(returnValue(servletRequest));
        }});
        // model
        final ModelContext mockModel = getComponentManager().lookup(ModelContext.class);
        getMockery().checking(new Expectations() {{
            allowing(mockModel).getCurrentEntityReference();
                will(returnValue(new WikiReference("wiki")));
        }});

        this.csrf = getComponentManager().lookup(CSRFToken.class);
    }

    /**
     * Test that the secret token is a non-empty string.
     */
    @Test
    public void testToken()
    {
        String token = csrf.getToken();
        Assert.assertNotNull("CSRF token is null", token);
        Assert.assertNotSame("CSRF token is empty string", "", token);
        Assert.assertTrue("CSRF token is too short: \"" + token + "\"", token.length() > 20);
    }

    /**
     * Test that the same secret token is returned on subsequent calls.
     */
    @Test
    public void testTokenTwice()
    {
        String token1 = csrf.getToken();
        String token2 = csrf.getToken();
        Assert.assertNotNull("CSRF token is null", token1);
        Assert.assertNotSame("CSRF token is empty string", "", token1);
        Assert.assertEquals("Subsequent calls returned different tokens", token1, token2);
    }

    /**
     * Test that the produced valid secret token is indeed valid.
     */
    @Test
    public void testTokenValidity()
    {
        String token = csrf.getToken();
        Assert.assertTrue("Valid token did not pass the check", csrf.isTokenValid(token));
    }

    /**
     * Test that null is not valid.
     */
    @Test
    public void testNullNotValid()
    {
        Assert.assertFalse("Null passed validity check", csrf.isTokenValid(null));
    }

    /**
     * Test that empty string is not valid.
     */
    @Test
    public void testEmptyNotValid()
    {
        Assert.assertFalse("Empty string passed validity check", csrf.isTokenValid(""));
    }

    /**
     * Test that the prefix of the valid token is not valid.
     */
    @Test
    public void testPrefixNotValid()
    {
        String token = csrf.getToken();
        if (token != null) {
            token = token.substring(0, token.length()-2);
        }
        Assert.assertFalse("Null passed validity check", csrf.isTokenValid(token));
    }

    /**
     * Test that the resubmission URL is correct.
     */
    @Test
    public void testResubmissionURL()
    {
        String url = csrf.getResubmissionURL();
        try {
            String redirect;
            redirect = URLEncoder.encode(mockDocumentUrl + "?a=b&c=d", "utf-8");
            String expected = resubmitUrl + "?xredirect=" + redirect;
            Assert.assertEquals("Invalid resubmission URL", expected, url);
        } catch (UnsupportedEncodingException exception) {
            Assert.fail("Should not happen: " + exception.getMessage());
        }
    }
}
