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
package org.xwiki.portlet.view;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xwiki.portlet.model.RequestType;
import org.xwiki.portlet.url.URLRewriter;

/**
 * An XML filter that rewrites {@code <link>} HREFs inside Internet Explorer's specific conditional comments.
 * 
 * @version $Id$
 */
public class HTMLConditionalCommentsXMLFilter extends XMLFilterImpl implements LexicalHandler
{
    /**
     * The URI used to set the lexical handler.
     */
    private static final String LEXICAL_HANDLER_URI = "http://xml.org/sax/properties/lexical-handler";

    /**
     * The pattern used to capture link HREFs inside IE conditional comments.
     */
    private static final Pattern HREF_PATTERN = Pattern.compile("href=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);

    /**
     * The lexical handler.
     */
    private LexicalHandler lexicalHandler;

    /**
     * The object used to rewrite URLs.
     */
    private final URLRewriter urlRewriter;

    /**
     * Creates a new XML filter that rewrites link HREFs inside Internet Explorer's specific conditional comments.
     * 
     * @param urlRewriter the object used to rewrite URLs
     */
    public HTMLConditionalCommentsXMLFilter(URLRewriter urlRewriter)
    {
        this.urlRewriter = urlRewriter;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#setProperty(String, Object)
     */
    @Override
    public void setProperty(String uri, Object handler) throws SAXNotSupportedException, SAXNotRecognizedException
    {
        if (LEXICAL_HANDLER_URI.equals(uri)) {
            lexicalHandler = (LexicalHandler) handler;
        } else {
            super.setProperty(uri, handler);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#parse(InputSource)
     */
    @Override
    public void parse(InputSource in) throws SAXException, IOException
    {
        XMLReader parent = getParent();
        if (parent != null) {
            parent.setProperty(LEXICAL_HANDLER_URI, this);
        }
        super.parse(in);
    }

    /**
     * {@inheritDoc}
     * 
     * @see LexicalHandler#comment(char[], int, int)
     */
    public void comment(char[] ch, int start, int length) throws SAXException
    {
        if (lexicalHandler != null) {
            String comment = new String(ch, start, length);
            // Rewrite URLs inside IE conditional comments.
            if (comment.startsWith("[if IE") && comment.endsWith("<![endif]")) {
                StringBuilder result = new StringBuilder(comment);
                Matcher matcher = HREF_PATTERN.matcher(comment);
                int delta = 0;
                while (matcher.find()) {
                    String servletURL = matcher.group(1);
                    String portletURL = urlRewriter.rewrite(servletURL, RequestType.RESOURCE);
                    result.replace(matcher.start(1) + delta, matcher.end(1) + delta, portletURL);
                    delta += portletURL.length() - servletURL.length();
                }
                lexicalHandler.comment(result.toString().toCharArray(), 0, result.length());
            } else {
                lexicalHandler.comment(ch, start, length);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see LexicalHandler#endEntity(String)
     */
    public void endEntity(String name) throws SAXException
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see LexicalHandler#startCDATA()
     */
    public void startCDATA() throws SAXException
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see LexicalHandler#startDTD(String, String, String)
     */
    public void startDTD(String name, String publicId, String systemId) throws SAXException
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see LexicalHandler#startEntity(String)
     */
    public void startEntity(String name) throws SAXException
    {
    }
}
