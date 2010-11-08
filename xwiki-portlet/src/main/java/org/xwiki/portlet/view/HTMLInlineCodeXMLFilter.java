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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xwiki.portlet.url.URLRewriter;

/**
 * An XML filter that transforms the code (e.g. CSS, JavaScript) in-lined in the HTML document.
 * 
 * @version $Id$
 */
public class HTMLInlineCodeXMLFilter extends XMLFilterImpl
{
    /**
     * The current stream filter. It depends on the current element.
     */
    private StreamFilter currentFilter;

    /**
     * The text to be filtered, i.e. the text of the current element if the current element has an associated filter.
     */
    private final StringBuilder filterInput = new StringBuilder();

    /**
     * The mapping between tag names and corresponding filters.
     */
    private final Map<String, StreamFilter> filters = new HashMap<String, StreamFilter>();

    /**
     * Creates a new HTML XML filter that rewrites the CSS and JavaScript code in-lined in the HTML document.
     * 
     * @param urlRewriter the object used to rewrite URLs
     * @param namespace the string used to name-space HTML element identifiers in the context of the portal page
     */
    public HTMLInlineCodeXMLFilter(URLRewriter urlRewriter, String namespace)
    {
        filters.put("style", new CSSStreamFilter(namespace, urlRewriter));
        filters.put("script", new JavaScriptStreamFilter(namespace));
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        currentFilter = filters.get(qName);
        super.startElement(uri, localName, qName, atts);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#endElement(String, String, String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (currentFilter != null) {
            StringWriter writer = new StringWriter();
            currentFilter.filter(new StringReader(filterInput.toString()), writer);
            filterInput.delete(0, filterInput.length());
            String result = writer.toString();
            super.characters(result.toCharArray(), 0, result.length());
        }
        super.endElement(uri, localName, qName);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (length > 0 && currentFilter != null) {
            filterInput.append(ch, start, length);
        } else {
            super.characters(ch, start, length);
        }
    }
}
